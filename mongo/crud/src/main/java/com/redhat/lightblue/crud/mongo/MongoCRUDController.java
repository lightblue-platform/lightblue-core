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
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.eval.QueryEvaluationContext;
import com.redhat.lightblue.eval.QueryEvaluator;
import com.redhat.lightblue.eval.Updater;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.mongo.MongoConfiguration;
import com.redhat.lightblue.query.Projection;
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

    public static final String ID_STR = "_id";

    private static final String OP_INSERT = "insert";
    private static final String OP_SAVE = "save";
    private static final String OP_FIND = "find";
    private static final String OP_UPDATE = "update";
    private static final String OP_DELETE = "delete";

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCRUDController.class);

    private final JsonNodeFactory factory;
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
        this.factory = factory;
        this.dbResolver = dbResolver;
    }

    /**
     * Insertion operation for mongo
     */
    @Override
    public CRUDInsertionResponse insert(MetadataResolver resolver,
                                        List<JsonDoc> documents,
                                        Projection projection) {
        LOGGER.debug("insert() start");
        CRUDInsertionResponse response = new CRUDInsertionResponse();
        saveOrInsert(resolver, documents, false, projection, response, OP_INSERT);
        return response;
    }

    @Override
    public CRUDSaveResponse save(MetadataResolver resolver,
                                 List<JsonDoc> documents,
                                 boolean upsert,
                                 Projection projection) {
        LOGGER.debug("save() start");
        CRUDSaveResponse response = new CRUDSaveResponse();
        saveOrInsert(resolver, documents, upsert, projection, response, OP_SAVE);
        return response;
    }

    private void saveOrInsert(MetadataResolver resolver,
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
        Translator translator = new Translator(resolver, factory);
        try {
            LOGGER.debug("saveOrInsert: Translating docs");
            DBObject[] dbObjects = translator.toBson(documents);
            if (dbObjects != null) {
                LOGGER.debug("saveOrInsert: {} docs translated to bson", dbObjects.length);
                Map<DBObject, List<Error>> errorMap = new HashMap<DBObject, List<Error>>();
                List<DBObject> successfulUpdates = new ArrayList<DBObject>(documents.size());
                DocIndex index = new DocIndex(resolver, dbResolver);
                saveDocs(resolver, dbObjects, documents, operation, upsert, index, errorMap, successfulUpdates);
                // Build projectors and translate docs
                Map<String, Projector> projectorMap = buildProjectorsAndTranslateDocs(resolver, projection, index, translator, response, successfulUpdates);
                // Reorganize errors
                reorganizeErrors(errorMap, projection, translator, projectorMap, response);
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("saveOrInsert() end: {} docs requested, {} saved", documents.size(), response.getDocuments().size());
    }

    private void saveDocs(MetadataResolver resolver, DBObject[] dbObjects, List<JsonDoc> documents, String operation, boolean upsert, DocIndex index, Map<DBObject, List<Error>> errorMap, List<DBObject> successfulUpdates) {
        LOGGER.debug("saveOrInsert: saving docs");
        
        // Group docs by collection
        
        for (DBObject obj : dbObjects) {
            index.addDoc(obj);
        }
        Set<MongoDataStore> stores = index.getDataStores();
        
        for (MongoDataStore store : stores) {
            List<DBObject> docs = index.getDocs(store);
            LOGGER.debug("saving {} docs into collection {}", docs.size(), store);
            DBCollection collection = index.getDBCollection(store);
            for (DBObject doc : docs) {
                try {
                    WriteResult result;
                    if (operation.equals(OP_INSERT)) {
                        result = collection.insert(doc, WriteConcern.SAFE);
                    } else {
                        Object x = doc.get(ID_STR);
                        if (x == null) {
                            result = collection.insert(doc, WriteConcern.SAFE);
                        } else {
                            BasicDBObject q=new BasicDBObject(ID_STR,new ObjectId(x.toString()));
                            LOGGER.debug("update query: {}",q);
                            result = collection.update(q, doc, upsert, false, WriteConcern.SAFE);
                        }
                    }
                    LOGGER.debug("Write result {}",result);
                    String error = result.getError();
                    if (error != null) {
                        addErrorToMap(errorMap, doc, operation, ERR_SAVE_ERROR, error);
                    } else {
                        successfulUpdates.add(doc);
                    }
                } catch (MongoException.DuplicateKey dke) {
                    addErrorToMap(errorMap, doc, operation, ERR_DUPLICATE, dke.toString());
                } catch (Exception e) {
                    addErrorToMap(errorMap, doc, operation, ERR_SAVE_ERROR, e.toString());
                }
            }
        }
        LOGGER.debug("saveOrInsert complete, {} sucessful updates", successfulUpdates.size());
    }
    
    
    private Map<String, Projector> buildProjectorsAndTranslateDocs(MetadataResolver resolver, Projection projection, DocIndex index, Translator translator, AbstractCRUDUpdateResponse response, List<DBObject> successfulUpdates) {
        Map<String, Projector> projectorMap = buildProjectorMap(projection, index.getObjectTypes(), resolver);
        if (!successfulUpdates.isEmpty()) {
            ArrayList<JsonDoc> resultDocs = new ArrayList<JsonDoc>(successfulUpdates.size());
            for (DBObject doc : successfulUpdates) {
                resultDocs.add(translateAndProject(doc, translator, projectorMap));
            }
            response.setDocuments(resultDocs);
        }
        return projectorMap;
    }
    
    private void reorganizeErrors(Map<DBObject, List<Error>> errorMap, Projection projection, Translator translator, Map<String, Projector> projectorMap, AbstractCRUDUpdateResponse response) {
        List<DataError> dataErrors = new ArrayList<DataError>();
        for (Map.Entry<DBObject, List<Error>> entry : errorMap.entrySet()) {
            JsonDoc errorDoc;
            if (projection != null) {
                errorDoc = translateAndProject(entry.getKey(), translator, projectorMap);
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
    public CRUDUpdateResponse update(MetadataResolver resolver,
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
        Translator translator = new Translator(resolver, factory);
        try {
            EntityMetadata md = resolver.getEntityMetadata(entity);
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
            Updater updater=Updater.getInstance(factory,md,update);
            DB db = dbResolver.get((MongoDataStore) md.getDataStore());
            DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
            iterateUpdate(coll,translator,md,response,mongoQuery,updater,projector);
            
        } finally {
            Error.pop();
        }
        LOGGER.debug("update end: updated: {}, failed: {}", response.getNumUpdated(), response.getNumFailed());
        return response;
    }

    private void iterateUpdate(DBCollection collection,
                               Translator translator,
                               EntityMetadata md,
                               CRUDUpdateResponse response,
                               DBObject query,
                               Updater updater,
                               Projector projector) {
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
                LOGGER.debug("Retrieved doc {}",docIndex);
                JsonDoc jsonDocument=translator.toJson(document);
                QueryEvaluationContext ctx=new QueryEvaluationContext(jsonDocument.getRoot());
                if(updater.update(jsonDocument,md.getFieldTreeRoot(),Path.EMPTY)) {
                    LOGGER.debug("Document {} modified, updating",docIndex);
                    DBObject updatedObject=translator.toBson(jsonDocument);
                    WriteResult result=collection.save(updatedObject);                    
                    LOGGER.debug("Number of rows affected : ", result.getN());
                } else {
                    LOGGER.debug("Document {} was not modified",docIndex);
                }
                if(projector!=null) {
                    LOGGER.debug("Projecting document {}",docIndex);
                    jsonDocument=projector.project(jsonDocument,factory,ctx);
                    response.getDocuments().add(jsonDocument);
                }
                docIndex++;
                // TODO: errors and docErrors
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
    public CRUDDeleteResponse delete(MetadataResolver resolver,
                                     String entity,
                                     QueryExpression query) {
        if (query == null) {
            throw new IllegalArgumentException("Null query");
        }
        LOGGER.debug("delete start: q:{}", query);
        Error.push(OP_DELETE);
        CRUDDeleteResponse response = new CRUDDeleteResponse();
        response.setErrors(new ArrayList<Error>());
        Translator translator = new Translator(resolver, factory);
        try {
            EntityMetadata md = resolver.getEntityMetadata(entity);
            LOGGER.debug("Translating query {}", query);
            DBObject mongoQuery = translator.translate(md, query);
            LOGGER.debug("Translated query {}", mongoQuery);
            DB db = dbResolver.get((MongoDataStore) md.getDataStore());
            DBCollection coll = db.getCollection(((MongoDataStore) md.getDataStore()).getCollectionName());
            LOGGER.debug("Removing docs");
            WriteResult result=coll.remove(mongoQuery);
            LOGGER.debug("Removal complete, write result={}",result);
            response.setNumDeleted(result.getN());
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
    public CRUDFindResponse find(MetadataResolver resolver,
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
        Translator translator = new Translator(resolver, factory);
        try {
            EntityMetadata md = resolver.getEntityMetadata(entity);
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
                QueryEvaluationContext ctx = qeval.evaluate(document);
                results.add(projector.project(document, factory, ctx));
            }
            response.setResults(results);
        } finally {
            Error.pop();
        }
        LOGGER.debug("find end: query: {} results: {}", response.getResults().size());
        return response;
    }

    private JsonDoc translateAndProject(DBObject doc,
                                        Translator translator,
                                        Map<String, Projector> projectorMap) {
        JsonDoc jsonDoc = translator.toJson(doc);
        LOGGER.debug("Translated doc: {}", jsonDoc);
        String objectType = jsonDoc.get(Translator.OBJECT_TYPE).asText();
        Projector projector = projectorMap.get(objectType);
        JsonDoc result = projector.project(jsonDoc, factory, null);
        LOGGER.debug("projected doc: {}", result);
        return result;
    }

    /**
     * Builds a map of objectType->DocProjector for all the object types given
     */
    private Map<String, Projector> buildProjectorMap(Projection projection,
                                                     Set<String> objectTypes,
                                                     MetadataResolver resolver) {
        Map<String, Projector> projectorMap = new HashMap<String, Projector>();
        if (projection != null) {
            for (String objectType : objectTypes) {
                EntityMetadata md = resolver.getEntityMetadata(objectType);
                Projector projector = Projector.getInstance(projection, md);
                LOGGER.debug("projector {} for {}", projector, objectType);
                projectorMap.put(objectType, projector);
            }
        }
        return projectorMap;
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
    private void addErrorToMap(Map<DBObject, List<Error>> map, DBObject object, Error error) {
        List l = map.get(object);
        if (l == null) {
            l = new ArrayList<>();
            map.put(object, l);
        }
        l.add(error);
    }
}
