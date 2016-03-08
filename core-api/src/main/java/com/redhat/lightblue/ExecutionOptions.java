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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonObject;

/**
 * Execution options are name-value pairs
 */
public class ExecutionOptions extends JsonObject {

    private static final long serialVersionUID = 1L;

    private final Map<String,String> options=new HashMap<>();

    public Map<String,String> getOptions() {
        return options;
    }

    /**
     * Returns a json representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = getFactory().objectNode();
        for(Map.Entry<String,String> entry:options.entrySet()) {
            node.put(entry.getKey(),getFactory().textNode(entry.getValue()));
        }
        return node;
    }

    /**
     * Parses execution options from a json object. Unrecognized elements are
     * ignored.
     */
    public static ExecutionOptions fromJson(ObjectNode node) {
        ExecutionOptions ret = new ExecutionOptions();
        for(Iterator<Map.Entry<String,JsonNode>> itr=node.fields();itr.hasNext();) {
            Map.Entry<String,JsonNode> entry=itr.next();
            ret.options.put(entry.getKey(),entry.getValue().asText());
        }
        return ret;
    }
}
