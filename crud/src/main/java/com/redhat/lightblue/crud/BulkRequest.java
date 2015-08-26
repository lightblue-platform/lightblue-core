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

import com.redhat.lightblue.Request;

/**
 * Contains a list of requests. Each request has a sequence number
 * that can be used to match the corresponding response.
 * <pre>
 *   {
 *     "requests": [
 *         {
 *             "seq":0,
 *             "op": "FIND",
 *             "req": { request }
 *         }
 *     ]
 *   }
 * </pre> 
 */
public class BulkRequest extends AbstractBulkJsonObject<Request> {

    @Override
    public JsonNode toJson() {
        return toJson("requests","request");
    }
    
    @Override
    protected void toJsonEntryNode(ObjectNode node,Request entry) {
        node.set("op",JsonNodeFactory.instance.textNode(entry.getOperation().toString()));
        node.set("request",entry.toJson());
    }


    @Override
    protected Request parseEntry(ObjectNode node) {
        JsonNode opNode=node.get("op");
        if(opNode!=null) {
            Request req;
            String opstr=opNode.asText();
            JsonNode val=node.get("req");
            if(val instanceof ObjectNode) {
                if(opstr.equalsIgnoreCase(CRUDOperation.FIND.toString()))
                    req=FindRequest.fromJson((ObjectNode)val);
                else if(opstr.equalsIgnoreCase(CRUDOperation.INSERT.toString()))
                    req=InsertionRequest.fromJson((ObjectNode)val);
                else if(opstr.equalsIgnoreCase(CRUDOperation.SAVE.toString()))
                    req=SaveRequest.fromJson((ObjectNode)val);
                else if(opstr.equalsIgnoreCase(CRUDOperation.UPDATE.toString()))
                    req=UpdateRequest.fromJson((ObjectNode)val);
                else if(opstr.equalsIgnoreCase(CRUDOperation.DELETE.toString()))
                    req=DeleteRequest.fromJson((ObjectNode)val);
                else
                    throw new IllegalArgumentException(opstr);
                return req;
            } else
                throw new IllegalArgumentException(opstr);
        } else
            throw new IllegalArgumentException("op");
    }    
}
