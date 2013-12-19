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

import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.MongoException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.DataError;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.metadata.mongo.MongoDataStore;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.mediator.MetadataResolver;

import com.redhat.lightblue.eval.Projector;

import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDController;

public class MongoCRUDController implements CRUDController {

    public static final String ERR_INVALID_OBJECT="INVALID_OBJECT";
    public static final String ERR_DUPLICATE="DUPLICATE";
    public static final String ERR_INSERTION_ERROR="INSERTION_ERROR";

    private static final Logger logger=LoggerFactory.getLogger(MongoCRUDController.class);

    private final JsonNodeFactory factory;
    private final DBResolver dbResolver;

    public MongoCRUDController(JsonNodeFactory factory,
                               DBResolver dbResolver) {
        this.factory=factory;
        this.dbResolver=dbResolver;
    }
        
    /**
     * Insertion operation for mongo
     */
    @Override
    public CRUDInsertionResponse insert(MetadataResolver resolver,
                                        List<JsonDoc> documents,
                                        Projection projection) {
        if(documents==null||documents.isEmpty())
            throw new IllegalArgumentException("Empty documents");
        logger.debug("insert() start");
        Error.push("insert");
        Translator translator=new Translator(resolver,factory);
        CRUDInsertionResponse response=new CRUDInsertionResponse();
        try {
            logger.debug("insert: Translating docs");
            DBObject[] dbObjects=translator.toBson(documents);
            if(dbObjects!=null) {
                logger.debug("insert: {} docs translated to bson",dbObjects.length);
                logger.debug("insert: inserting docs");
                Map<DBObject,List<Error>> errorMap=new HashMap<DBObject,List<Error>>();
                // Group docs by collection
                DocIndex index=new DocIndex(resolver,dbResolver);
                for(DBObject obj:dbObjects) 
                    index.addDoc(obj);
                Set<MongoDataStore> stores=index.getDataStores();
                List<DBObject> successfulInsertions=new ArrayList<DBObject>(documents.size());
                for(MongoDataStore store:stores) {
                    List<DBObject> docs=index.getDocs(store);
                    logger.debug("inserting {} docs into collection {}",docs.size(),store);
                    DBCollection collection=index.getDBCollection(store);
                    for(DBObject doc:docs) {
                        try {
                            WriteResult result=collection.insert(doc,WriteConcern.SAFE);
                            String error=result.getError();
                            if(error!=null) {
                                addErrorToMap(errorMap,doc,"insert",ERR_INSERTION_ERROR,error);
                            } else
                                successfulInsertions.add(doc);
                        } catch(MongoException.DuplicateKey dke) {
                            addErrorToMap(errorMap,doc,"insert",ERR_DUPLICATE,dke.toString());
                        } catch(Exception e) {
                            addErrorToMap(errorMap,doc,"insert",ERR_INSERTION_ERROR,e.toString());
                        } 
                    }
                }
                logger.debug("insertion comlete, {} sucessful insertions",successfulInsertions.size());
                // Build projectors and translate docs
                Map<String,Projector> projectorMap=buildProjectorMap(projection,
                                                                     index.getObjectTypes(),
                                                                     resolver);
                if(!successfulInsertions.isEmpty()) {
                    ArrayList<JsonDoc> resultDocs=new ArrayList<JsonDoc>(successfulInsertions.size());
                    for(DBObject doc:successfulInsertions) { 
                        resultDocs.add(translateAndProject(doc,translator,projectorMap));
                    }
                    response.setDocuments(resultDocs);
                }
                // Reorganize errors
                List<DataError> dataErrors=new ArrayList<DataError>();
                for(Map.Entry<DBObject,List<Error>> entry:errorMap.entrySet()) {
                    JsonDoc errorDoc;
                    if(projection!=null) {
                        errorDoc=translateAndProject(entry.getKey(),translator,projectorMap);
                    } else
                        errorDoc=null;
                    DataError error=new DataError();
                    if(errorDoc!=null)
                        error.setEntityData(errorDoc.getRoot());
                    error.setErrors(entry.getValue());
                }
                response.setDataErrors(dataErrors);
            }
        } finally {
            Error.pop();
        }
        logger.debug("insert() end: {} docs requested, {} inserted",documents.size(),response.getDocuments().size());
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
        if(query==null)
            throw new IllegalArgumentException("Null query");
        if(projection==null)
            throw new IllegalArgumentException("Null projection");
        logger.debug("find start: q:{} p:{} sort:{} from:{} to:{}",query,projection,sort,from,to);
        Error.push("find");
        CRUDFindResponse response=new CRUDFindResponse();
        Translator translator=new Translator(resolver,factory);
        try {
            logger.debug("Translating query");
            DBObject mongoQuery=translator.translate(resolver.getEntityMetadata(entity),query);
            logger.debug("Translated query:{}"+mongoQuery);
            
        } finally {
            Error.pop();
        }
        logger.debug("find end: query: {} results: {}",response.getResults().size());
        return response;
    }


    private JsonDoc translateAndProject(DBObject doc,
                                        Translator translator,
                                        Map<String,Projector> projectorMap) {
        JsonDoc jsonDoc=translator.toJson(doc);
        logger.debug("Translated doc: {}",jsonDoc);
        String objectType=jsonDoc.get(Translator.OBJECT_TYPE).asText();
        Projector projector=projectorMap.get(objectType);
        JsonDoc result=projector.project(jsonDoc,factory,null);
        logger.debug("projected doc: {}",result);
        return result;
     }

    /**
     * Builds a map of objectType->DocProjector for all the object types given
     */
    private Map<String,Projector> buildProjectorMap(Projection projection,
                                                    Set<String> objectTypes,
                                                    MetadataResolver resolver) {
        Map<String,Projector> projectorMap=new HashMap<String,Projector>();
        if(projection!=null) {
            for(String objectType:objectTypes) {
                EntityMetadata md=resolver.getEntityMetadata(objectType);
                Projector projector=Projector.getInstance(projection,md);
                logger.debug("projector {} for {}",projector,objectType);
                projectorMap.put(objectType,projector);
            }
        }
        return projectorMap;
    }

    private void addErrorToMap(Map<DBObject,List<Error>> map,
                               DBObject obj,
                               String context,
                               String errorCode,
                               String msg) {
        Error error=new Error();
        error.pushContext(context);
        error.setErrorCode(errorCode);
        error.setMsg(msg);
        addErrorToMap(map,obj,error);
    }

    /**
     * Adds a new error for the document to the errormap
     */
    private void addErrorToMap(Map<DBObject,List<Error>> map,DBObject object,Error error) {
        List l=map.get(object);
        if(l==null)
            map.put(object,l=new ArrayList<Error>());
        l.add(error);
    }
}

