/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.crud.mongo;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bson.types.ObjectId;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.BasicDBObject;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.mongo.hystrix.FindOneCommand;
import com.redhat.lightblue.mongo.hystrix.InsertCommand;
import com.redhat.lightblue.mongo.hystrix.UpdateCommand;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

/**
 * Basic doc saver with no transaction support
 */
public class BasicDocSaver implements DocSaver {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDocSaver.class);

    private final FieldAccessRoleEvaluator roleEval;
    private final Translator translator;

    /**
     * Creates a doc saver with the given translator and role evaluator
     */
    public BasicDocSaver(Translator translator,
                         FieldAccessRoleEvaluator roleEval) {
        this.translator = translator;
        this.roleEval = roleEval;
    }

    @Override
    public void saveDoc(CRUDOperationContext ctx,
                        Op op,
                        boolean upsert,
                        DBCollection collection,
                        EntityMetadata md,
                        DBObject dbObject,
                        DocCtx inputDoc) {

        WriteResult result = null;
        String error = null;

        Object id = dbObject.get(MongoCRUDController.ID_STR);
        if (op == DocSaver.Op.insert
                || (id == null && upsert)) {
            // Inserting
            result = insertDoc(ctx, collection, md, dbObject, inputDoc);
        } else if (op == DocSaver.Op.save && id != null) {
            // Updating
            LOGGER.debug("Updating doc {}" + id);
            BasicDBObject q = new BasicDBObject(MongoCRUDController.ID_STR, new ObjectId(id.toString()));
            DBObject oldDBObject = new FindOneCommand(null, collection, q).execute();
            if (oldDBObject != null) {
                if (md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles())) {
                    JsonDoc oldDoc = translator.toJson(oldDBObject);
                    inputDoc.setOriginalDocument(oldDoc);
                    List<Path> paths = roleEval.getInaccessibleFields_Update(inputDoc, oldDoc);
                    if (paths == null || paths.isEmpty()) {
                        translator.addInvisibleFields(oldDBObject, dbObject, md);
                        result = new UpdateCommand(null, collection, q, dbObject, upsert, upsert, WriteConcern.SAFE).execute();
                        inputDoc.setOperationPerformed(Operation.UPDATE);
                    } else {
                        inputDoc.addError(Error.get("update",
                                CrudConstants.ERR_NO_FIELD_UPDATE_ACCESS, paths.toString()));
                    }
                } else {
                    inputDoc.addError(Error.get("update",
                            CrudConstants.ERR_NO_ACCESS, "update:" + md.getName()));
                }
            } else {
                // Cannot update, doc does not exist, insert
                result = insertDoc(ctx, collection, md, dbObject, inputDoc);
            }
        } else {
            // Error, invalid request
            LOGGER.warn("Invalid request, cannot update or insert");
            inputDoc.addError(Error.get(op.toString(), MongoCrudConstants.ERR_SAVE_ERROR, "Invalid request"));
        }

        LOGGER.debug("Write result {}", result);
        if (result != null) {
            if (error == null) {
                error = result.getError();
            }
            if (error != null) {
                inputDoc.addError(Error.get(op.toString(), MongoCrudConstants.ERR_SAVE_ERROR, error));
            }
        }
    }

    private WriteResult insertDoc(CRUDOperationContext ctx,
                                  DBCollection collection,
                                  EntityMetadata md,
                                  DBObject dbObject,
                                  DocCtx inputDoc) {
        LOGGER.debug("Inserting doc");
        if (!md.getAccess().getInsert().hasAccess(ctx.getCallerRoles())) {
            inputDoc.addError(Error.get("insert",
                    MongoCrudConstants.ERR_NO_ACCESS,
                    "insert:" + md.getName()));
        } else {
            List<Path> paths = roleEval.getInaccessibleFields_Insert(inputDoc);
            LOGGER.debug("Inaccessible fields:{}", paths);
            if (paths == null || paths.isEmpty()) {
                try {
                    WriteResult r = new InsertCommand(null, collection, dbObject, WriteConcern.SAFE).execute();
                    inputDoc.setOperationPerformed(Operation.INSERT);
                    return r;
                } catch (MongoException.DuplicateKey dke) {
                    LOGGER.error("saveOrInsert failed: {}", dke);
                    inputDoc.addError(Error.get("insert", MongoCrudConstants.ERR_DUPLICATE, dke.toString()));
                }
            } else {
                inputDoc.addError(Error.get("insert", CrudConstants.ERR_NO_FIELD_INSERT_ACCESS, paths.toString()));
            }
        }
        return null;
    }
}
