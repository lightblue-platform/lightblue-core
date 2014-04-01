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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import com.redhat.lightblue.crud.mongo.DBResolver;
import com.redhat.lightblue.metadata.mongo.MongoDataStore;

public class MongoDBResolver implements DBResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBResolver.class);

    private final MongoClient mongoClient;

    public MongoDBResolver(MongoClient mongoClient) {
        this.mongoClient=mongoClient;
    }

    @Override
    public DB get(MongoDataStore store) {
        LOGGER.debug("Returning DB for :"+store.getDatabaseName());
        return mongoClient.getDB(store.getDatabaseName());
    }
}
