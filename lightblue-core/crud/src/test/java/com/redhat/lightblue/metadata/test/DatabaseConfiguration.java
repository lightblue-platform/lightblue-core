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
package com.redhat.lightblue.metadata.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.util.JsonInitializable;

/**
 *
 * @author nmalik
 */
public class DatabaseConfiguration implements JsonInitializable {
    private String name;
    private String hostname = "localhost";
    private String port = "27017";
    private String collection = "metadata";

    public static final Metadata create(DatabaseConfiguration config) {
        return new DatabaseMetadata();
    }

    public String getName() {
        return name;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getCollection() {
        return collection;
    }

    @Override
    public void initializeFromJson(JsonNode node) {

    }
}
