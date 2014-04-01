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
package com.redhat.lightblue.mongo.config;

import java.net.UnknownHostException;
import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.util.JsonInitializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import org.bson.BSONObject;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.DefaultTypes;

public class MongoMetadataConfiguration extends MongoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoMetadataConfiguration.class);

    private String name;
    private String collection;

    public static MongoMetadata create(MongoMetadataConfiguration configuration) throws UnknownHostException {
        DB db = configuration.getDB();
        Extensions<BSONObject> parserExtensions = new Extensions<>();
        parserExtensions.addDefaultExtensions();
        parserExtensions.registerDataStoreParser("mongo", new MongoDataStoreParser<BSONObject>());
        DefaultTypes typeResolver = new DefaultTypes();
        return new MongoMetadata(db, parserExtensions, typeResolver);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the collection
     */
    public String getCollection() {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    public DB getDB() throws UnknownHostException {
        return getDB(name);
    }

    public String toString() {
        return super.toString()+"\n"+
            "collection:"+collection;
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        super.initializeFromJson(node);
        if (node != null) {
            JsonNode x=node.get("name");
            if(x!=null)
                name=x.asText();
            x=node.get("collection");
            if(x!=null)
                collection=x.asText();
        }
    }
}
