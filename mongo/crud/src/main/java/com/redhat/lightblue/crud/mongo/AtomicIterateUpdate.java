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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.CrudConstants;

import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Atomic updater that evaluates the query, and updates the documents one by one using atomic updates
 */
public class AtomicIterateUpdate implements DocUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtomicIterateUpdate.class);
    
    private final JsonNodeFactory nodeFactory;
    private final FieldAccessRoleEvaluator roleEval;
    private final Translator translator;
    private final DBObject mongoUpdateExpr;
    private final Projector projector;
    private final Set<Path> updatedFields;

    public AtomicIterateUpdate(JsonNodeFactory nodeFactory,
                               FieldAccessRoleEvaluator roleEval,
                               Translator translator,
                               DBObject mongoUpdateExpr,
                               Projector projector,
                               Set<Path> updatedFields) {
        this.nodeFactory=nodeFactory;
        this.roleEval=roleEval;
        this.translator=translator;
        this.mongoUpdateExpr=mongoUpdateExpr;
        this.projector=projector;
        this.updatedFields=updatedFields;
    }

    @Override
    public void update(CRUDOperationContext ctx,
                       DBCollection collection,
                       EntityMetadata md,
                       CRUDUpdateResponse response,
                       DBObject query) {
        LOGGER.debug("atomicIterateUpdate: start");
        Set<Path> inaccessibleFields=roleEval.getInaccessibleFields(FieldAccessRoleEvaluator.Operation.update);
        for(Path x:inaccessibleFields) {
            if(updatedFields.contains(x)) {
                ctx.addError(Error.get("update",CrudConstants.ERR_NO_FIELD_UPDATE_ACCESS,x.toString()));
            }
        }
        int numFailed=0;
        int numUpdated=0;
        if(!ctx.hasErrors()) {
            LOGGER.debug("Computing the result set for {}",query);
            DBCursor cursor=null;
            int docIndex=0;
            try {
                // Find docs
                cursor=collection.find(query);
                LOGGER.debug("Found {} documents",cursor.count());
                // read-update
                while(cursor.hasNext()) {
                    DBObject document=cursor.next();
                    // Add the doc to context
                    DocCtx doc=ctx.addDocument(translator.toJson(document));
                    try {
                        QueryEvaluationContext qctx=new QueryEvaluationContext(doc.getRoot());
                        Object id=document.get("_id");
                        LOGGER.debug("Retrieved doc {} id={}",docIndex,id);
                        // Update doc
                        DBObject modifiedDoc=collection.findAndModify(new BasicDBObject("_id",id),
                                                                      null,
                                                                      null,
                                                                      false,
                                                                      mongoUpdateExpr,
                                                                      true,
                                                                      false);
                        if(projector!=null) {
                            LOGGER.debug("Projecting document {}",docIndex);
                            doc.setOutputDocument(projector.project(translator.toJson(modifiedDoc),nodeFactory,qctx));
                        }
                        numUpdated++;
                    } catch (MongoException e) {
                        LOGGER.warn("Update exception for document {}: {}",docIndex,e);
                        doc.addError(Error.get(MongoCrudConstants.ERR_UPDATE_ERROR,e.toString()));
                        numFailed++;
                    }
                    docIndex++;
                }
            } finally {
                if(cursor!=null) {
                    cursor.close();
                }
            }
        }
        response.setNumUpdated(numUpdated);
        response.setNumFailed(numFailed);
    }
}
