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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.Response;

/**
 * Contains a list of responses. Each request has a sequence number matching the
 * sequence number of the request.
 * <pre>
 *   {
 *     "responses": [
 *         {
 *             "seq":0,
 *             "response": { response }
 *         }
 *     ]
 *   }
 * </pre>
 */
public class BulkResponse extends AbstractBulkJsonObject<Response> {

    /**
     * Returns a JSON representation of this
     */
    @Override
    public JsonNode toJson() {
        JsonNodeFactory factory = getFactory();
        ObjectNode node = factory.objectNode();
        ArrayNode arr = factory.arrayNode();
        int seq = 0;
        for (Response x : entries) {
            ObjectNode entryNode = factory.objectNode();
            entryNode.set("seq", factory.numberNode(seq++));
            entryNode.set("response", x.toJson());
            arr.add(entryNode);
        }
        node.put("responses", arr);
        return node;
    }
}
