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
package com.redhat.lightblue.crud;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonObject;
import com.redhat.lightblue.Request;

/**
 * Base class for bulk request and responses. This class contains the
 * code common to both, as bulk request and response are structurally
 * similar. It deals with a JSON of the form:
 *
 *
 * <pre>
 *   {
 *     "<entries>": [
 *         {
 *             "seq":0,
 *             "op": "FIND",
 *             "<entry>": { item }
 *         }
 *     ]
 *   }
 * </pre> 
 */
abstract class AbstractBulkJsonObject<T extends JsonObject> extends JsonObject {

    protected final List<T> entries=new ArrayList<T>();

    /**
     * Returns all entries in the bulk object
     */
    public List<T> getEntries() {
        return entries;
    }

    /**
     * Sets the entries in the bulk object. Copies the collection
     */
    public void setEntries(List<T> x) {
        entries.clear();
        entries.addAll(x);
    }

    public void add(T x) {
        entries.add(x);
    }


}
