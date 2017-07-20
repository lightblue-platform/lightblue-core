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

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Response information from mediator APIs
 */
public class Response extends BaseResponse  {

    private static final Logger LOGGER = LoggerFactory.getLogger(Response.class);

    private static final String PROPERTY_PROCESSED = "processed";
    private static final String PROPERTY_RESULT_METADATA = "resultMetadata";

    private JsonNode entityData;
    private List<ResultMetadata> resultMetadata;

    private int responseDataSizeB = 0;
    private int maxResultSetSizeB = -1, warnResultSetSizeB = -1;
    private Request forRequest;

    // TODO: Response has no access to CrudConstants
    public static final String ERR_RESULT_SIZE_TOO_LARGE = "crud:ResultSizeTooLarge";


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

    public void ensureResponseSizeNotTooLarge(int maxResultSetSizeB, int warnResultSetSizeB, Request forRequest) {
        this.forRequest = forRequest;
        this.maxResultSetSizeB = maxResultSetSizeB;
        this.warnResultSetSizeB = warnResultSetSizeB;
    }

    public boolean isEnsureResposneSizeNotTooLarge() {
        return maxResultSetSizeB > 0;
    }

    public boolean isWarnResponseSizeLarge() {
        return warnResultSetSizeB > 0;
    }

    public boolean isCheckResponseSize() {
        return isEnsureResposneSizeNotTooLarge() || isWarnResponseSizeLarge();
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

            enforceResponseSizeLimits(doc);

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
     * Ensures that the response is not larger than maxResultSetSizeB to protect Lightblue from running out of memory when building large result sets.
     *
     * This method is used to checks only the data part of the response. TODO: check dataErrors as well, as they include entire docs.
     *
     * @param entityDataJson json being added to the response
     */
    private void enforceResponseSizeLimits(JsonNode entityDataJson) {

        if (isCheckResponseSize()) {
            responseDataSizeB += JsonUtils.size(entityDataJson);

            // TODO: could account for copies made for hooks here
            // better do https://github.com/lightblue-platform/lightblue-core/issues/802 instead
        }

        if (isEnsureResposneSizeNotTooLarge() && responseDataSizeB >= maxResultSetSizeB) {
            LOGGER.error(ERR_RESULT_SIZE_TOO_LARGE + ": request={}, responseDataSizeB={}", forRequest, responseDataSizeB);

            // empty data
            // returning incomplete result set could be useful, but also confusing and thus dangerous
            // the counts - matchCount, modifiedCount - are unmodified
            setEntityData(JsonNodeFactory.instance.arrayNode());

            throw Error.get(ERR_RESULT_SIZE_TOO_LARGE, responseDataSizeB+"B > "+maxResultSetSizeB+"B");
        } else if (isWarnResponseSizeLarge() && responseDataSizeB >= warnResultSetSizeB) {
            LOGGER.warn("crud:ResultSizeIsLarge: request={}, responseDataSizeB={}", forRequest, responseDataSizeB);
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

    public int getResponseDataSizeB() {
        return responseDataSizeB;
    }
}
