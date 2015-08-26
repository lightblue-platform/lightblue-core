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

import com.redhat.lightblue.Response;

/**
 * Contains a list of responses. Each request has a sequence number
 * matching the sequence number of the request.
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

    @Override
    public JsonNode toJson() {
        return toJson("responses","response");
    }

    @Override
    protected void toJsonEntryNode(ObjectNode node,Response entry) {
        node.set("response",entry.toJson());
    }

    @Override
    protected Response parseEntry(ObjectNode node) {
        // This should not be required. We never parse response
        throw new UnsupportedOperationException();
    }
}
