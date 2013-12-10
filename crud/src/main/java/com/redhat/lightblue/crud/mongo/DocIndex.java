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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;

import com.redhat.lightblue.mediator.MetadataResolver;

public class DocIndex {

    private final Map<MongoDataStore,DocGroup> storeToGroupIndex=new HashMap<MongoDataStore,DocGroup>();
    private final Map<String,MongoDataStore> objectTypeToStoreIndex=new HashMap<String,MongoDataStore>();

    private final MetadataResolver mdResolver;
    private final DBResolver dbResolver;

    private static final class DocGroup {
        final String objectType;
        final DB db;
        final DBCollection collection;
        final List<DBObject> docs=new ArrayList<DBObject>();

        public DocGroup(String objectType,
                        DB db,
                        DBCollection collection) {
            this.objectType=objectType;
            this.db=db;
            this.collection=collection;
        }
    }

    public DocIndex(MetadataResolver mdResolver,
                    DBResolver dbResolver) {
        this.mdResolver=mdResolver;
        this.dbResolver=dbResolver;
    }

    public void addDoc(DBObject doc) {
        String objectType=(String)doc.get(Translator.OBJECT_TYPE_STR);
        MongoDataStore store=objectTypeToStoreIndex.get(objectType);
        if(store==null) {
            EntityMetadata md=mdResolver.getEntityMetadata(objectType);
            store=(MongoDataStore)md.getDataStore();
            objectTypeToStoreIndex.put(objectType,store);
        }
        DocGroup group=storeToGroupIndex.get(store);
        if(group==null) {
            DB db=dbResolver.get(store);
            DBCollection coll=db.getCollection(store.getCollectionName());
            group=new DocGroup(objectType,db,coll);
            storeToGroupIndex.put(store,group);
        }
    }

    public Set<MongoDataStore> getDataStores() {
        return storeToGroupIndex.keySet();
    }

    public DBCollection getDBCollection(MongoDataStore store) {
        DocGroup group=storeToGroupIndex.get(store);
        if(group!=null)
            return group.collection;
        else
            return null;
    }

    public List<DBObject> getDocs(MongoDataStore store) {
        DocGroup group=storeToGroupIndex.get(store);
        if(group!=null)
            return group.docs;
        else
            return null;
    }
    
}
