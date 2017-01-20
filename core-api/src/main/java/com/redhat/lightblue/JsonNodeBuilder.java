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
package com.redhat.lightblue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

import java.util.Collection;
import java.util.List;

public class JsonNodeBuilder {

    private final ObjectNode root = JsonObject.getFactory().objectNode();

    private boolean includeNulls = false;

    protected ObjectNode getRoot() {
        return root;
    }

    public boolean includeNulls() {
        return includeNulls;
    }

    public void includeNulls(boolean include) {
        includeNulls = include;
    }

    public JsonNodeBuilder add(String key, JsonNode value) {
        if (include(value)) {
            root.set(key, value);
        }
        return this;

    }

    public JsonNodeBuilder add(String key, java.util.Comparator value) {
        if (include(value)) {
            root.put(key, value.toString());
        }
        return this;
    }

    public JsonNodeBuilder add(String key, Long value) {
        if (include(value)) {
            root.put(key, value);
        }
        return this;
    }

    public JsonNodeBuilder add(String key, Boolean value) {
        if (include(value)) {
            root.put(key, value);
        }
        return this;
    }

    public JsonNodeBuilder add(String key, OperationStatus value) {
        if (include(value)) {
            root.put(key, value.name());
        }
        return this;
    }

    public JsonNodeBuilder add(String key, JsonObject value) {
        if (include(value)) {
            root.set(key, value.toJson());
        }
        return this;
    }

    public <T> JsonNodeBuilder add(String key, List<T> values) {
        if (includes(values)) {
            ArrayNode arr = JsonObject.getFactory().arrayNode();
            root.set(key, arr);
            for (Object err : values) {
                arr.add(err.toString());
            }
        }
        return this;
    }

    public <T> JsonNodeBuilder addJsonObjectsList(String key, List<? extends JsonObject> values) {
        if (includes(values)) {
            ArrayNode arr = JsonObject.getFactory().arrayNode();
            root.set(key, arr);
            for (JsonObject err : values) {
                arr.add(err==null?JsonObject.getFactory().nullNode():err.toJson());
            }
        }
        return this;
    }

    public <T> JsonNodeBuilder addErrorsList(String key, List<Error> values) {
        if (includes(values)) {
            ArrayNode arr = JsonObject.getFactory().arrayNode();
            root.set(key, arr);
            for (Error err : values) {
                arr.add(err.toJson());
            }
        }
        return this;
    }

    protected boolean include(Object object) {
        return object != null || includeNulls;
    }

    private <T> boolean includes(Collection<T> collection) {
        if (includeNulls) {
            return true;
        } else {
            return collection != null && !collection.isEmpty();
        }
    }

    public JsonNodeBuilder add(String key, String value) {
        if (value != null) {
            root.put(key, value);
        }
        return this;
    }

    public JsonNode build() {
        return root;
    }

}
