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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.redhat.lightblue.common.mongo.DBResolver;
import com.redhat.lightblue.common.mongo.MongoDataStore;
import com.redhat.lightblue.config.common.DataSourceConfiguration;
import com.redhat.lightblue.config.common.DataSourcesConfiguration;

public class MongoDBResolver implements DBResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBResolver.class);

    private final Map<String, DataSourceConfiguration> datasources;
    private final Map<String, DB> dbMap = new HashMap<>();
    private final Map<String, DB> dsMap = new HashMap<>();

    public MongoDBResolver(DataSourcesConfiguration ds) {
        datasources = ds.getDataSourcesByType(MongoConfiguration.class);
    }

    @Override
    public DB get(MongoDataStore store) {
        LOGGER.debug("Returning DB for {}", store);
        DB db = null;
        try {
            if (store.getDatasourceName() != null) {
                db = dsMap.get(store.getDatasourceName());
                if (db == null) {
                    MongoConfiguration cfg = (MongoConfiguration) datasources.get(store.getDatasourceName());
                    if (cfg == null) {
                        throw new IllegalArgumentException("No datasources for " + store.getDatasourceName());
                    }
                    db = cfg.getDB();
                    dsMap.put(store.getDatasourceName(), db);
                }
            } else if (store.getDatabaseName() != null) {
                db = dbMap.get(store.getDatabaseName());
                if (db == null) {
                    for (DataSourceConfiguration cfg : datasources.values()) {
                        if (((MongoConfiguration) cfg).getDatabase().equals(store.getDatabaseName())) {
                            db = ((MongoConfiguration) cfg).getDB();
                            dbMap.put(store.getDatabaseName(), db);
                            break;
                        }
                    }
                }
            }
        } catch (RuntimeException re) {
            LOGGER.error("Cannot get {}:{}", store, re);
            throw re;
        } catch (Exception e) {
            LOGGER.error("Cannot get {}:{}", store, e);
            throw new IllegalArgumentException(e);
        }
        if (db == null) {
            throw new IllegalArgumentException("Cannot find DB for  " + store);
        }
        return db;
    }
}
