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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.MongoException;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.mongo.MongoDataStore;

import com.redhat.lightblue.mediator.OperationContext;

public class MongoCRUDController {

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
        
    public int insert(OperationContext ctx) {
        logger.debug("insert() start");
        Error.push("insert");
        Translator translator=new Translator(ctx,factory);
        int n=0;
        try {
            logger.debug("insert: Translating docs");
            DBObject[] dbObjects=translator.toBson(ctx.getDocs());
            if(dbObjects!=null) {
                logger.debug("insert: {} docs translated to bson",dbObjects.length);
                logger.debug("insert: inserting docs");
                HashMap<DBObject,Error> errorMap=new HashMap<DBObject,Error>();
                // Group docs by collection
                DocIndex index=new DocIndex(ctx,dbResolver);
                for(DBObject obj:dbObjects) 
                    index.addDoc(obj);
                Set<MongoDataStore> stores=index.getDataStores();
                for(MongoDataStore store:stores) {
                    List<DBObject> docs=index.getDocs(store);
                    logger.debug("inserting {} docs into collection {}",docs.size(),store);
                    DBCollection collection=index.getDBCollection(store);
                    for(DBObject doc:docs) {
                        try {
                            WriteResult result=collection.insert(doc,WriteConcern.SAFE);
                            String error=result.getError();
                            if(error!=null) {
                                Error err=new Error();
                                err.pushContext("insert");
                                err.setErrorCode(ERR_INSERTION_ERROR);
                                err.setMsg(error);
                                errorMap.put(doc,err);
                            } else
                                n++;
                        } catch(MongoException.DuplicateKey dke) {
                            Error err=new Error();
                            err.pushContext("insert");
                            err.setErrorCode(ERR_DUPLICATE);
                            err.setMsg(dke.toString());
                            errorMap.put(doc,err);
                        } catch(Exception e) {
                            Error err=new Error();
                            err.pushContext("insert");
                            err.setErrorCode(ERR_INSERTION_ERROR);
                            err.setMsg(e.toString());
                            errorMap.put(doc,err);
                        } 
                    }
                }
                // Set operation context
                // Put errors back into the operation context
                
                // Insertion complete, now project
            }
        } finally {
            Error.pop();
        }
        logger.debug("insert() end: {} docs requested, {} inserted",ctx.getDocs().size(),n);
        return n;
    }
}

