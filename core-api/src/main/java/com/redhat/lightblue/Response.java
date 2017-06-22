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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Response information from mediator APIs
 */
public class Response extends BaseResponse  {

    private static final String PROPERTY_PROCESSED = "processed";
    private static final String PROPERTY_RESULT_METADATA = "resultMetadata";

    private JsonNode entityData;
    private List<ResultMetadata> resultMetadata;


    /**
     * @deprecated use Response(JsonNodeFactory)
     */
    @Deprecated
    public Response() {
        super();
    }

    public Response(JsonNodeFactory jsonNodeFactory) {
        super(jsonNodeFactory);
    }

    public Response(Response r) {
        super(r);
        entityData=r.entityData;
        resultMetadata=r.resultMetadata;
    }

    public Response(BaseResponse r) {
        super(r);
    }

    /**
     * Returns the entity data resulting from the call.
     */
    public JsonNode getEntityData() {
        return entityData;
    }

    /**
     * Returns the entity data resulting from the call.
     */
    public void setEntityData(JsonNode node) {
        // if the node is not an array then wrap it in an array
        if (node != null && !node.isArray()) {
            ArrayNode arrayNode = new ArrayNode(jsonNodeFactory);
            arrayNode.add(node);
            entityData = arrayNode;
        } else {
            entityData = node;
        }
    }

    /**
     * Adds a document, or array of documents to the entity data
     */
    public void addEntityData(JsonNode doc) {
        if(doc!=null) {
            if(entityData==null) {
                entityData=JsonNodeFactory.instance.arrayNode();
            } 
            if(doc instanceof ArrayNode) {
                for(Iterator<JsonNode> itr=doc.elements();itr.hasNext();) {
                    ((ArrayNode)entityData).add(itr.next());
                }
            } else {
                ((ArrayNode)entityData).add(doc);
            }
        }
    }

    /**
     * Metadata list for documents in entityData. If there are more
     * than one documents, the entitydata and metadata indexes match.
     */
    public List<ResultMetadata> getResultMetadata() {
        return resultMetadata;
    }

    public void setResultMetadata(List<ResultMetadata> l) {
        resultMetadata=l;
    }

    /**
     * Returns JSON representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node=(ObjectNode)super.toJson();
        if(entityData!=null) {
            node.set(PROPERTY_PROCESSED, entityData);
        }
        if(resultMetadata!=null) {
            ArrayNode arr=jsonNodeFactory.arrayNode();
            for(ResultMetadata x:resultMetadata) {
                arr.add(x==null?jsonNodeFactory.nullNode():x.toJson());
            }
            node.set(PROPERTY_RESULT_METADATA,arr);
        }
        return node;
    }
}
