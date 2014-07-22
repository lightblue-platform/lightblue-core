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
package com.redhat.lightblue.util;

import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Base class for objects constructed from Json nodes. Optionally contains a
 * pointer to the json node the object is constructed from.
 */
public abstract class JsonObject implements Serializable {

    private static final long serialVersionUID = 1l;

    private static JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    /**
     * @return the factory
     */
    public static JsonNodeFactory getFactory() {
        return factory;
    }

    private JsonNode sourceNode;

    public JsonObject() {
    }

    public JsonObject(JsonNode node) {
        sourceNode = node;
    }

    public JsonNode getSourceNode() {
        return sourceNode;
    }

    public abstract JsonNode toJson();

    @Override
    public String toString() {
        return toJson().toString();
    }
}
