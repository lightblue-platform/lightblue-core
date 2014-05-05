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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.redhat.lightblue.common.mongo.DBResolver;
import com.redhat.lightblue.common.mongo.MongoDataStore;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;
import com.redhat.lightblue.eval.QueryEvaluator;
import com.redhat.lightblue.eval.Updater;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class MongoCRUDController implements CRUDController {

    public static final String ID_STR = "_id";

    /**
     * Name of the property for the operation context that keeps the last saver class instance used
     */
    public static final String PROP_SAVER = "MongoCRUDController:saver";

    /**
     * Name of the property for the operation context that keeps the last updater class instance used
     */
    public static final String PROP_UPDATER = "MongoCRUDController:updater";

    /**
     * Name of the property for the operation context that keeps the last deleter class instance used
     */
    public static final String PROP_DELETER = "MongoCRUDController:deleter";

    /**
     * Name of the property for the operation context that keeps the last finder class instance used
     */
    public static final String PROP_FINDER = "MongoCRUDController:finder";

    public static final String OP_INSERT = "insert";
    public static final String OP_SAVE = "save";
    public static final String OP_FIND = "find";
    public static final String OP_UPDATE = "update";
    public static final String OP_DELETE = "delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCRUDController.class);

    private static final Projection ID_PROJECTION = new FieldProjection(new Path(ID_STR), true, false);

    private final JsonNodeFactory nodeFactory;
    private final DBResolver dbResolver;

    public MongoCRUDController(DBResolver dbResolver) {
        this(JsonNodeFactory.withExactBigDecimals(true), dbResolver);
    }

    public MongoCRUDController(JsonNodeFactory factory,
                               DBResolver dbResolver) {
        this.nodeFactory = factory;
        this.dbResolver = dbResolver;
    }

    /**
     * Insertion operation for mongo
     */
    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                        Projection projection) {
        LOGGER.debug("insert() start");
        CRUDInsertionResponse response = new CRUDInsertionResponse();
        int n = saveOrInsert(ctx, false, projection, OP_INSERT);
        response.setNumInserted(n);
        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx,
                                 boolean upsert,
                                 Projection projection) {
        LOGGER.debug("save() start");
        CRUDSaveResponse response = new CRUDSaveResponse();
        int n = saveOrInsert(ctx, upsert, projection, OP_SAVE);
        response.setNumSaved(n);
        return response;
    }

    private int saveOrInsert(CRUDOperationContext ctx,
                             boolean upsert,
                             Projection projection,
                             String operation) {
        int ret = 0;
        List<DocCtx> documents = ctx.getDocumentsWithoutErrors();
        if (documents == null || documents.isEmpty()) {
            return ret;
        }
        for (DocCtx doc : documents) {
            doc.setOriginalDocument(doc);
        }
        LOGGER.debug("saveOrInsert() start");
        Error.push(operation);
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            FieldAccessRoleEvaluator roleEval
                    = new FieldAccessRoleEvaluator(ctx.getEntityMetadata(ctx.getEntityName()),
                            ctx.getCallerRoles());
            LOGGER.debug("saveOrInsert: Translating docs");
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            DBObject[] dbObjects = translator.toBson(documents);
            // dbObjects[i] is the translation of documents.get(i)
            if (dbObjects != null) {
                LOGGER.debug("saveOrInsert: {} docs translated to bson", dbObjects.length);

                MongoDataStore store = (MongoDataStore) md.getDataStore();
                DB db = dbResolver.get(store);
                DBCollection collection = db.getCollection(store.getCollectionName());

                Projection combinedProjection = Projection.add(projection, roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.find));

                Projector projector;
                if (combinedProjection != null) {
                    projector = Projector.getInstance(combinedProjection, md);
                } else {
                    projector = null;
                }
                DocSaver saver = new BasicDocSaver(translator, roleEval);
                ctx.setProperty(PROP_SAVER, saver);
                for (int docIndex = 0; docIndex < dbObjects.length; docIndex++) {
                    DBObject dbObject = dbObjects[docIndex];
                    DocCtx inputDoc = documents.get(docIndex);
                    try {
                        saver.saveDoc(ctx, operation.equals(OP_INSERT) ? DocSaver.Op.insert : DocSaver.Op.save,
                                upsert, collection, md, dbObject, inputDoc);
                        ctx.getHookManager().queueHooks(ctx);
                    } catch (Exception e) {
                        LOGGER.error("saveOrInsert failed: {}", e);
                        inputDoc.addError(Error.get(operation, MongoCrudConstants.ERR_SAVE_ERROR, e.toString()));
                    }
                    if (projector != null) {
                        JsonDoc jsonDoc = translator.toJson(dbObject);
                        LOGGER.debug("Translated doc: {}", jsonDoc);
                        inputDoc.setOutputDocument(projector.project(jsonDoc, nodeFactory, null));
                    } else {
                        inputDoc.setOutputDocument(null);
                    }
                    LOGGER.debug("projected doc: {}", inputDoc.getOutputDocument());
                    if (!inputDoc.hasErrors()) {
                        ret++;
                    }
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("Error during insert: {}", e);
            throw e;
        } finally {
            Error.pop();
        }
        LOGGER.debug("saveOrInsert() end: {} docs requested, {} saved", documents.size(), ret);
        return ret;
    }

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     UpdateExpression update,
                                     Projection projection) {
        if (query == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_QUERY);
        }
        LOGGER.debug("update start: q:{} u:{} p:{}", query, update, projection);
        Error.push(OP_UPDATE);
        CRUDUpdateResponse response = new CRUDUpdateResponse();
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            if (md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles())) {
                ConstraintValidator validator = ctx.getFactory().getConstraintValidator(md);
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());

                Projector projector;
                if (projection != null) {
                    Projection x = Projection.add(projection, roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.find));
                    LOGGER.debug("Projection={}", x);
                    projector = Projector.getInstance(x, md);
                } else {
                    projector = null;
                }
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                Projector errorProjector;
                if (projector == null) {
                    errorProjector = Projector.getInstance(ID_PROJECTION, md);
                } else {
                    errorProjector = projector;
                }

                // If there are any constraints for updated fields, we have to use iterate-update
                Updater updater = Updater.getInstance(nodeFactory, md, update);
                Set<Path> updatedFields = updater.getUpdateFields();
                LOGGER.debug("Fields to be updated:{}", updatedFields);
                boolean constrainedFieldUpdated = false;
                for (Path x : updatedFields) {
                    FieldTreeNode ftn = md.resolve(x);
                    if (ftn instanceof Field && !((Field) ftn).getConstraints().isEmpty()) {
                        LOGGER.debug("Field {} has constraints, can't run direct mongo update", ftn);
                        constrainedFieldUpdated = true;
                        break;
                    }
                }
                // See if we can translate the update expression
                DBObject mongoUpdateExpr;
                try {
                    mongoUpdateExpr = translator.translate(md, update);
                } catch (CannotTranslateException e) {
                    LOGGER.debug("Cannot translate update expression {}", e);
                    mongoUpdateExpr = null;
                }
                // if we can translate mongo update expression, and if
                // there are no constrained fields, we can use one of
                // the mongo updaters
                DocUpdater docUpdater;
                if (mongoUpdateExpr != null && !constrainedFieldUpdated) {
                    docUpdater = new AtomicIterateUpdate(nodeFactory, roleEval, translator,
                            mongoUpdateExpr, projector, updatedFields);
                } else {
                    docUpdater = new IterateAndUpdate(nodeFactory, validator, roleEval, translator, updater,
                            projector, errorProjector);
                }
                ctx.setProperty(PROP_UPDATER, docUpdater);
                docUpdater.update(ctx, coll, md, response, mongoQuery);
                ctx.getHookManager().queueHooks(ctx);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS, "update:" + ctx.getEntityName()));
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("update end: updated: {}, failed: {}", response.getNumUpdated(), response.getNumFailed());
        return response;
    }

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                     QueryExpression query) {
        if (query == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_QUERY);
        }
        LOGGER.debug("delete start: q:{}", query);
        Error.push(OP_DELETE);
        CRUDDeleteResponse response = new CRUDDeleteResponse();
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            if (md.getAccess().getDelete().hasAccess(ctx.getCallerRoles())) {
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                DocDeleter deleter = new IterateDeleter(translator);
                ctx.setProperty(PROP_DELETER, deleter);
                deleter.delete(ctx, coll, mongoQuery, response);
                ctx.getHookManager().queueHooks(ctx);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS, "delete:" + ctx.getEntityName()));
            }
        } catch (Exception e) {
            ctx.addError(Error.get(e.toString()));
        } finally {
            Error.pop();
        }
        LOGGER.debug("delete end: deleted: {}}", response.getNumDeleted());
        return response;
    }

    /**
     * Search implementation for mongo
     */
    @Override
    public CRUDFindResponse find(CRUDOperationContext ctx,
                                 QueryExpression query,
                                 Projection projection,
                                 Sort sort,
                                 Long from,
                                 Long to) {
        if (query == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_QUERY);
        }
        if (projection == null) {
            throw new IllegalArgumentException(MongoCrudConstants.ERR_NULL_PROJECTION);
        }
        LOGGER.debug("find start: q:{} p:{} sort:{} from:{} to:{}", query, projection, sort, from, to);
        Error.push(OP_FIND);
        CRUDFindResponse response = new CRUDFindResponse();
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            if (md.getAccess().getFind().hasAccess(ctx.getCallerRoles())) {
                FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, ctx.getCallerRoles());
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                DBObject mongoSort;
                if (sort != null) {
                    LOGGER.debug("Translating sort {}", sort);
                    mongoSort = translator.translate(sort);
                    LOGGER.debug("Translated sort {}", mongoSort);
                } else {
                    mongoSort = null;
                }
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                LOGGER.debug("Retrieve db collection:" + coll);
                DocFinder finder = new BasicDocFinder(translator);
                ctx.setProperty(PROP_FINDER, finder);
                response.setSize(finder.find(ctx, coll, mongoQuery, mongoSort, from, to));
                // Project results
                Projector projector = Projector.getInstance(Projection.add(projection, roleEval.getExcludedFields(FieldAccessRoleEvaluator.Operation.find)), md);
                QueryEvaluator qeval = QueryEvaluator.getInstance(query, md);
                for (DocCtx document : ctx.getDocuments()) {
                    QueryEvaluationContext qctx = qeval.evaluate(document);
                    document.setOutputDocument(projector.project(document, nodeFactory, qctx));
                }
                ctx.getHookManager().queueHooks(ctx);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS, "find:" + ctx.getEntityName()));
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("find end: query: {} results: {}", response.getSize());
        return response;
    }
}
