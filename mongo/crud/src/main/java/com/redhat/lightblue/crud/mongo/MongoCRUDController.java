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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.redhat.lightblue.DataError;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;
import com.redhat.lightblue.eval.QueryEvaluator;
import com.redhat.lightblue.eval.Updater;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.mongo.MongoConfiguration;
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

    private static final String OP_INSERT = "insert";
    private static final String OP_SAVE = "save";
    private static final String OP_FIND = "find";
    private static final String OP_UPDATE = "update";
    private static final String OP_DELETE = "delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCRUDController.class);

    private static final Projection ID_PROJECTION=new FieldProjection(new Path(ID_STR),true,false);

    private final JsonNodeFactory nodeFactory;
    private final DBResolver dbResolver;

    public static MongoCRUDController create(final MongoConfiguration config) {
        DBResolver r = new DBResolver() {
            @Override
            public DB get(MongoDataStore store) {
                try {
                    // TODO this should really be something that comes from the metadata for the given entity
                    // but we haven't thought about that enough.
                    return config.getDB();
                } catch (UnknownHostException ex) {
                    throw Error.get(MongoCrudConstants.ERR_CONNECTION_ERROR, ex.getMessage());
                }
            }
        };
        
        return new MongoCRUDController(r);
    }

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
        int n=saveOrInsert(ctx, false, projection, OP_INSERT);
        response.setNumInserted(n);
        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx,
                                 boolean upsert,
                                 Projection projection) {
        LOGGER.debug("save() start");
        CRUDSaveResponse response = new CRUDSaveResponse();
        int n=saveOrInsert(ctx, upsert, projection, OP_SAVE);
        response.setNumSaved(n);
        return response;
    }

    private int saveOrInsert(CRUDOperationContext ctx,
                             boolean upsert,
                             Projection projection,
                             String operation) {
        int ret=0;
        List<DocCtx> documents=ctx.getDocumentsWithoutErrors();
        if (documents == null || documents.isEmpty()) {
            return ret;
        }
        LOGGER.debug("saveOrInsert() start");
        Error.push(operation);
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            LOGGER.debug("saveOrInsert: Translating docs");
            EntityMetadata md = ctx.getEntityMetadata(ctx.getEntityName());
            DBObject[] dbObjects = translator.toBson(documents);
            // dbObjects[i] is the translation of documents.get(i)
            if (dbObjects != null) {
                LOGGER.debug("saveOrInsert: {} docs translated to bson", dbObjects.length);

                MongoDataStore store=(MongoDataStore) md.getDataStore();
                DB db = dbResolver.get(store);
                DBCollection collection = db.getCollection(store.getCollectionName());
 
                Projector projector;
                if (projection != null) {
                    projector = Projector.getInstance(projection, md);
                } else {
                    projector=null;
                }
                LOGGER.debug("projector {} ", projector);
                
                for(int docIndex=0;docIndex<dbObjects.length;docIndex++) {
                    DBObject dbObject=dbObjects[docIndex];
                    DocCtx inputDoc=documents.get(docIndex);
                    try {
                        saveDoc(ctx,collection,md,operation,dbObject,inputDoc,upsert);
                    } catch (MongoException.DuplicateKey dke) {
                        inputDoc.addError(Error.get(operation,MongoCrudConstants.ERR_DUPLICATE,dke.toString()));
                    } catch (Exception e) {
                        inputDoc.addError(Error.get(operation, MongoCrudConstants.ERR_SAVE_ERROR, e.toString()));
                    }
                    inputDoc.setOutputDocument(translateAndProject(dbObject,translator,projector));
                    if(!inputDoc.hasErrors())
                        ret++;
                }
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("saveOrInsert() end: {} docs requested, {} saved", documents.size(), ret);
        return ret;
    }

    private void saveDoc(CRUDOperationContext ctx,
                         DBCollection collection,
                         EntityMetadata md,
                         String operation,
                         DBObject dbObject,
                         DocCtx inputDoc,
                         boolean upsert) {
        WriteResult result=null;
        String error=null;
        boolean insertAccess=md.getAccess().getInsert().hasAccess(ctx.getCallerRoles());
        boolean updateAccess=md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles());
        if (operation.equals(OP_INSERT)) {
            if(!insertAccess) {
                inputDoc.addError(Error.get(operation,MongoCrudConstants.ERR_NO_ACCESS,operation+":"+md.getName()));
            } else {
                result = collection.insert(dbObject, WriteConcern.SAFE);
            }
        } else {
            Object x = dbObject.get(ID_STR);
            if (x == null&&upsert) {
                if(!insertAccess) {
                    inputDoc.addError(Error.get(operation,MongoCrudConstants.ERR_NO_ACCESS,operation+":"+md.getName()));
                } else {
                    result = collection.insert(dbObject, WriteConcern.SAFE);
                }
            } else if(x!=null) {
                if (canModify(insertAccess, updateAccess, upsert)) {
                    BasicDBObject q=new BasicDBObject(ID_STR,new ObjectId(x.toString()));
                    LOGGER.debug("update query: {}",q);
                    result = collection.update(q, dbObject, upsert, false, WriteConcern.SAFE);
                } else {
                    inputDoc.addError(Error.get(operation,MongoCrudConstants.ERR_NO_ACCESS,operation+":"+md.getName()));
                }

            }
        }
        LOGGER.debug("Write result {}",result);
        if(result!=null) {
            if(error==null) {
                error = result.getError();
            }
            if (error != null) {
                inputDoc.addError(Error.get(operation, MongoCrudConstants.ERR_SAVE_ERROR, error));
            } 
        }
    }


    private boolean canModify(boolean insertAccess, boolean updateAccess, boolean upsertAccess) {
        if(upsertAccess) {
            return upsertAccess && insertAccess && updateAccess;
        } else {
            return !upsertAccess & updateAccess;
        }
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
            if(md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles())) {
                ConstraintValidator validator=ctx.getFactory().getConstraintValidator(md);
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                Projector projector;
                if(projection!=null) {
                    projector = Projector.getInstance(projection, md);
                } else {
                    projector = null;
                }
                Updater updater=Updater.getInstance(nodeFactory,md,update);
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                Projector errorProjector;
                if(projector==null) {
                    errorProjector=Projector.getInstance(ID_PROJECTION,md);
                } else {
                    errorProjector=projector;
                }   
                iterateUpdate(ctx,coll,validator,translator,md,response,mongoQuery,updater,projector,errorProjector);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS,"update:"+ctx.getEntityName()));
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("update end: updated: {}, failed: {}", response.getNumUpdated(), response.getNumFailed());
        return response;
    }

    private void iterateUpdate(CRUDOperationContext ctx,
                               DBCollection collection,
                               ConstraintValidator validator,
                               Translator translator,
                               EntityMetadata md,
                               CRUDUpdateResponse response,
                               DBObject query,
                               Updater updater,
                               Projector projector,
                               Projector errorProjector) {
        LOGGER.debug("iterateUpdate: start");
        LOGGER.debug("Computing the result set for {}",query);
        DBCursor cursor=null;
        int docIndex=0;
        int numFailed=0;
        try {
            cursor=collection.find(query);
            LOGGER.debug("Found {} documents",cursor.count());
            // read-update-write
            while(cursor.hasNext()) {
                DBObject document=cursor.next();
                boolean hasErrors=false;
                LOGGER.debug("Retrieved doc {}",docIndex);
                JsonDoc jsonDocument=translator.toJson(document);
                QueryEvaluationContext qctx=new QueryEvaluationContext(jsonDocument.getRoot());
                if(updater.update(jsonDocument,md.getFieldTreeRoot(),Path.EMPTY)) {
                    LOGGER.debug("Document {} modified, updating",docIndex);
                    PredefinedFields.updateArraySizes(nodeFactory,jsonDocument);
                    LOGGER.debug("Running constraint validations");
                    validator.clearErrors();
                    validator.validateDoc(jsonDocument);
                    List<Error> errors=validator.getErrors();
                    if(errors!=null&&!errors.isEmpty()) {
                        ctx.addErrors(errors);
                        hasErrors=true;
                    }
                    errors=validator.getDocErrors().get(jsonDocument);
                    if(errors!=null&&!errors.isEmpty()) {
                        DocCtx doc=ctx.addDocument(errorProjector.project(jsonDocument,nodeFactory,qctx));
                        doc.addErrors(errors);
                        hasErrors=true;
                    }
                    if(!hasErrors) {
                        try {
                            DBObject updatedObject=translator.toBson(jsonDocument);
                            WriteResult result=collection.save(updatedObject);                    
                            LOGGER.debug("Number of rows affected : ", result.getN());
                        } catch (Exception e) {
                            LOGGER.warn("Update exception for document {}: {}",docIndex,e);
                            ctx.addError(Error.get(MongoCrudConstants.ERR_UPDATE_ERROR,e.toString()));
                            hasErrors=true;
                        }
                    }
                } else {
                    LOGGER.debug("Document {} was not modified",docIndex);
                }
                if(hasErrors) {
                    LOGGER.debug("Document {} has errors",docIndex);
                    numFailed++;
                } else {
                    if(projector!=null) {
                        LOGGER.debug("Projecting document {}",docIndex);
                        jsonDocument=projector.project(jsonDocument,nodeFactory,qctx);
                        ctx.addDocument(jsonDocument);
                    }
                }
                docIndex++;
            }
        } finally {
            if(cursor!=null) {
                cursor.close();
            }
        }
        response.setNumUpdated(docIndex);
        response.setNumFailed(numFailed);
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
            if(md.getAccess().getDelete().hasAccess(ctx.getCallerRoles())) {
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                LOGGER.debug("Removing docs");
                WriteResult result=coll.remove(mongoQuery);
                LOGGER.debug("Removal complete, write result={}",result);
                response.setNumDeleted(result.getN());
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS,"delete:"+ctx.getEntityName()));
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
            if(md.getAccess().getFind().hasAccess(ctx.getCallerRoles())) {
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                LOGGER.debug("Retrieve db collection:" + coll);
                LOGGER.debug("Submitting query");
                DBCursor cursor = coll.find(mongoQuery);
                LOGGER.debug("Query evaluated");
                if (sort != null) {
                    LOGGER.debug("Translating sort {}", sort);
                    DBObject mongoSort = translator.translate(sort);
                    LOGGER.debug("Translated sort {}", mongoSort);
                    cursor = cursor.sort(mongoSort);
                    LOGGER.debug("Result set sorted");
                }
                LOGGER.debug("Applying limits: {} - {}", from, to);
                response.setSize(cursor.size());
                if (from != null) {
                    cursor.skip(from.intValue());
                }
                if (to != null) {
                    cursor.limit(to.intValue() - (from == null ? 0 : from.intValue()) + 1);
                }
                LOGGER.debug("Retrieving results");
                List<DBObject> mongoResults = cursor.toArray();
                LOGGER.debug("Retrieved {} results", mongoResults.size());
                List<JsonDoc> jsonDocs = translator.toJson(mongoResults);
                LOGGER.debug("Translated DBObjects to json");
                // Project results
                Projector projector = Projector.getInstance(projection, md);
                QueryEvaluator qeval = QueryEvaluator.getInstance(query, md);
                List<JsonDoc> results = new ArrayList<JsonDoc>(jsonDocs.size());
                for (JsonDoc document : jsonDocs) {
                    QueryEvaluationContext qctx = qeval.evaluate(document);
                    results.add(projector.project(document, nodeFactory, qctx));
                }
                response.setResults(results);
            } else {
                ctx.addError(Error.get(MongoCrudConstants.ERR_NO_ACCESS,"find:"+ctx.getEntityName()));
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("find end: query: {} results: {}", response.getResults().size());
        return response;
    }

    private JsonDoc translateAndProject(DBObject doc,
                                        Translator translator,
                                        Projector projector) {
        JsonDoc jsonDoc = translator.toJson(doc);
        LOGGER.debug("Translated doc: {}", jsonDoc);
        JsonDoc result = projector.project(jsonDoc, nodeFactory, null);
        LOGGER.debug("projected doc: {}", result);
        return result;
    }


    private void addErrorToMap(Map<DBObject, List<Error>> map,
                               DBObject obj,
                               String context,
                               String errorCode,
                               String msg) {
        Error error = Error.get(context, errorCode, msg);
        addErrorToMap(map, obj, error);
    }

    /**
     * Adds a new error for the document to the errormap
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void addErrorToMap(Map<DBObject, List<Error>> map, DBObject object, Error error) {
        List l = map.get(object);
        if (l == null) {
            l = new ArrayList<>();
            map.put(object, l);
        }
        l.add(error);
    }
}
