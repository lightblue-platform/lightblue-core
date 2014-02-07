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
import java.util.Set;

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
import com.redhat.lightblue.crud.AbstractCRUDUpdateResponse;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.MetadataResolver;
import com.redhat.lightblue.crud.ConstraintValidator;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;
import com.redhat.lightblue.eval.QueryEvaluator;
import com.redhat.lightblue.eval.Updater;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.mongo.MongoConfiguration;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class MongoCRUDController implements CRUDController {

    public static final String ERR_INVALID_OBJECT = "INVALID_OBJECT";
    public static final String ERR_DUPLICATE = "DUPLICATE";
    public static final String ERR_INSERTION_ERROR = "INSERTION_ERROR";
    public static final String ERR_SAVE_ERROR = "SAVE_ERROR";
    public static final String ERR_UPDATE_ERROR = "UPDATE_ERROR";
    public static final String ERR_NO_ACCESS = "NO_ACCESS";

    public static final String ID_STR = "_id";

    private static final String OP_INSERT = "insert";
    private static final String OP_SAVE = "save";
    private static final String OP_FIND = "find";
    private static final String OP_UPDATE = "update";
    private static final String OP_DELETE = "delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCRUDController.class);

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
                    throw Error.get("CONNECTION_ERROR", ex.getMessage());
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
                                        String entity,
                                        List<JsonDoc> documents,
                                        Projection projection) {
        LOGGER.debug("insert() start");
        CRUDInsertionResponse response = new CRUDInsertionResponse();
        saveOrInsert(ctx, entity,documents, false, projection, response, OP_INSERT);
        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx,
                                 String entity,
                                 List<JsonDoc> documents,
                                 boolean upsert,
                                 Projection projection) {
        LOGGER.debug("save() start");
        CRUDSaveResponse response = new CRUDSaveResponse();
        saveOrInsert(ctx, entity, documents, upsert, projection, response, OP_SAVE);
        return response;
    }

    private void saveOrInsert(CRUDOperationContext ctx,
                              String entity,
                              List<JsonDoc> documents,
                              boolean upsert,
                              Projection projection,
                              AbstractCRUDUpdateResponse response,
                              String operation) {
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("Empty documents");
        }
        LOGGER.debug("saveOrInsert() start");
        Error.push(operation);
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            LOGGER.debug("saveOrInsert: Translating docs");
            EntityMetadata md = ctx.getEntityMetadata(entity);
            DBObject[] dbObjects = translator.toBson(documents);
            if (dbObjects != null) {
                LOGGER.debug("saveOrInsert: {} docs translated to bson", dbObjects.length);
                Map<DBObject, List<Error>> errorMap = new HashMap<DBObject, List<Error>>();
                List<DBObject> successfulUpdates = new ArrayList<DBObject>(documents.size());
                saveDocs(ctx, md, dbObjects, operation, upsert, errorMap, successfulUpdates);
                // Build projectors and translate docs
                Projector projector;
                if (projection != null) {
                    projector = Projector.getInstance(projection, md);
                    LOGGER.debug("projector {} ", projector);
                } else
                    projector=null;
                if (!successfulUpdates.isEmpty()) {
                    ArrayList<JsonDoc> resultDocs = new ArrayList<JsonDoc>(successfulUpdates.size());
                    for (DBObject doc : successfulUpdates) {
                        resultDocs.add(translateAndProject(doc, translator, projector));
                    }
                    response.setDocuments(resultDocs);
                }
                // Reorganize errors
                reorganizeErrors(errorMap, translator, projector, response);
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("saveOrInsert() end: {} docs requested, {} saved", documents.size(), response.getDocuments()!=null?response.getDocuments().size():0);
    }

    private void saveDocs(CRUDOperationContext ctx,
                          EntityMetadata md,
                          DBObject[] dbObjects, 
                          String operation, 
                          boolean upsert, 
                          Map<DBObject, List<Error>> errorMap, 
                          List<DBObject> successfulUpdates) {
        LOGGER.debug("saveOrInsert: saving {} docs",dbObjects.length);
        
        // Group docs by collection
        MongoDataStore store=(MongoDataStore) md.getDataStore();
        DB db = dbResolver.get(store);
        DBCollection collection = db.getCollection(store.getCollectionName());
        
        for (DBObject doc : dbObjects) {
            try {
                WriteResult result=null;
                String error=null;
                boolean insertAccess=md.getAccess().getInsert().hasAccess(ctx.getCallerRoles());
                boolean updateAccess=md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles());
                if (operation.equals(OP_INSERT)) {
                    if(!insertAccess) {
                        addErrorToMap(errorMap,doc,operation,ERR_NO_ACCESS,operation+":"+md.getName());
                    } else {
                        result = collection.insert(doc, WriteConcern.SAFE);
                    }
                } else {
                    Object x = doc.get(ID_STR);
                    if (x == null&&upsert) {
                        if(!insertAccess) {
                            addErrorToMap(errorMap,doc,operation,ERR_NO_ACCESS,operation+":"+md.getName());
                        } else {
                            result = collection.insert(doc, WriteConcern.SAFE);
                        }
                    } else if(x!=null) {
                        if( (upsert&&insertAccess&&updateAccess) ||
                            (!upsert&updateAccess)) {
                            BasicDBObject q=new BasicDBObject(ID_STR,new ObjectId(x.toString()));
                            LOGGER.debug("update query: {}",q);
                            result = collection.update(q, doc, upsert, false, WriteConcern.SAFE);
                        } else {
                            addErrorToMap(errorMap,doc,operation,ERR_NO_ACCESS,operation+":"+md.getName());
                        }
                    }
                }
                LOGGER.debug("Write result {}",result);
                if(result!=null) {
                    if(error==null) {
                        error = result.getError();
                    }
                    if (error != null) {
                        addErrorToMap(errorMap, doc, operation, ERR_SAVE_ERROR, error);
                    } else {
                        successfulUpdates.add(doc);
                    }
                }
            } catch (MongoException.DuplicateKey dke) {
                addErrorToMap(errorMap, doc, operation, ERR_DUPLICATE, dke.toString());
            } catch (Exception e) {
                addErrorToMap(errorMap, doc, operation, ERR_SAVE_ERROR, e.toString());
            }
        }
        LOGGER.debug("saveOrInsert complete, {} sucessful updates", successfulUpdates.size());
    }
    
    
    private void reorganizeErrors(Map<DBObject, List<Error>> errorMap, Translator translator, Projector projector, AbstractCRUDUpdateResponse response) {
        List<DataError> dataErrors = new ArrayList<DataError>();
        for (Map.Entry<DBObject, List<Error>> entry : errorMap.entrySet()) {
            JsonDoc errorDoc;
            if (projector != null) {
                errorDoc = translateAndProject(entry.getKey(), translator, projector);
            } else {
                errorDoc = null;
            }
            DataError error = new DataError();
            if (errorDoc != null) {
                error.setEntityData(errorDoc.getRoot());
            }
            error.setErrors(entry.getValue());
        }
        response.setDataErrors(dataErrors);
    }
    
    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                     String entity,
                                     QueryExpression query,
                                     UpdateExpression update,
                                     Projection projection) {
        if (query == null) {
            throw new IllegalArgumentException("Null query");
        }
        LOGGER.debug("update start: q:{} u:{} p:{}", query, update, projection);
        Error.push(OP_UPDATE);
        CRUDUpdateResponse response = new CRUDUpdateResponse();
        response.setErrors(new ArrayList<Error>());
        response.setDataErrors(new ArrayList<DataError>());
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            EntityMetadata md = ctx.getEntityMetadata(entity);
            if(md.getAccess().getUpdate().hasAccess(ctx.getCallerRoles())) {
                ConstraintValidator validator=ctx.getFactory().getConstraintValidator(md);
                LOGGER.debug("Translating query {}", query);
                DBObject mongoQuery = translator.translate(md, query);
                LOGGER.debug("Translated query {}", mongoQuery);
                Projector projector;
                if(projection!=null) {
                    projector = Projector.getInstance(projection, md);
                    response.setDocuments(new ArrayList<JsonDoc>());
                } else {
                    projector = null;
                }
                Updater updater=Updater.getInstance(nodeFactory,md,update);
                DB db = dbResolver.get((MongoDataStore) md.getDataStore());
                DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
                Projector errorProjector;
                if(projector==null)
                    errorProjector=Projector.getInstance(new FieldProjection(new Path(ID_STR),true,false),md);
                else
                    errorProjector=projector;
                iterateUpdate(coll,validator,translator,md,response,mongoQuery,updater,projector,errorProjector);
            } else {
                response.getErrors().add(Error.get(ERR_NO_ACCESS,"update:"+entity));
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("update end: updated: {}, failed: {}", response.getNumUpdated(), response.getNumFailed());
        return response;
    }

    private void iterateUpdate(DBCollection collection,
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
                QueryEvaluationContext ctx=new QueryEvaluationContext(jsonDocument.getRoot());
                if(updater.update(jsonDocument,md.getFieldTreeRoot(),Path.EMPTY)) {
                    LOGGER.debug("Document {} modified, updating",docIndex);
                    PredefinedFields.updateArraySizes(nodeFactory,jsonDocument);
                    LOGGER.debug("Running constraint validations");
                    validator.clearErrors();
                    validator.validateDoc(jsonDocument);
                    List<Error> errors=validator.getErrors();
                    if(errors!=null&&!errors.isEmpty()) {
                        response.getErrors().addAll(errors);
                        hasErrors=true;
                    }
                    errors=validator.getDocErrors().get(jsonDocument);
                    if(errors!=null&&!errors.isEmpty()) {
                        response.getDataErrors().add(new DataError(errorProjector.project(jsonDocument,nodeFactory,ctx).getRoot(),errors));
                        hasErrors=true;
                    }
                    if(!hasErrors) {
                        try {
                            DBObject updatedObject=translator.toBson(jsonDocument);
                            WriteResult result=collection.save(updatedObject);                    
                            LOGGER.debug("Number of rows affected : ", result.getN());
                        } catch (Exception e) {
                            LOGGER.warn("Update exception for document {}: {}",docIndex,e);
                            response.getErrors().add(Error.get(ERR_UPDATE_ERROR,e.toString()));
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
                        jsonDocument=projector.project(jsonDocument,nodeFactory,ctx);
                        response.getDocuments().add(jsonDocument);
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
                                     String entity,
                                     QueryExpression query) {
        if (query == null) {
            throw new IllegalArgumentException("Null query");
        }
        LOGGER.debug("delete start: q:{}", query);
        Error.push(OP_DELETE);
        CRUDDeleteResponse response = new CRUDDeleteResponse();
        response.setErrors(new ArrayList<Error>());
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            EntityMetadata md = ctx.getEntityMetadata(entity);
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
                response.getErrors().add(Error.get(ERR_NO_ACCESS,"delete:"+entity));
            }
        } catch (Exception e) {
            response.getErrors().add(Error.get(e.toString()));
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
                                 String entity,
                                 QueryExpression query,
                                 Projection projection,
                                 Sort sort,
                                 Long from,
                                 Long to) {
        if (query == null) {
            throw new IllegalArgumentException("Null query");
        }
        if (projection == null) {
            throw new IllegalArgumentException("Null projection");
        }
        LOGGER.debug("find start: q:{} p:{} sort:{} from:{} to:{}", query, projection, sort, from, to);
        Error.push(OP_FIND);
        CRUDFindResponse response = new CRUDFindResponse();
        response.setErrors(new ArrayList<Error>());
        Translator translator = new Translator(ctx, nodeFactory);
        try {
            EntityMetadata md = ctx.getEntityMetadata(entity);
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
                response.getErrors().add(Error.get(ERR_NO_ACCESS,"find:"+entity));
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
