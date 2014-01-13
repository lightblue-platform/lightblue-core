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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

/**
 * Response information from mediator APIs
 */
public class Response extends JsonObject {

	private static final long serialVersionUID = 1L;
	
	private OperationStatus status;
    private long modifiedCount;
    private long matchCount;
    private String taskHandle;
    private SessionInfo session;
    private JsonNode entityData;
    private final List<DataError> dataErrors = new ArrayList<>();
    private final List<Error> errors = new ArrayList<>();

    /**
     * Status of the completed operation
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * Status of the completed operation
     */
    public void setStatus(OperationStatus s) {
        status = s;
    }

    /**
     * Number of documents inserted/updated/deleted
     */
    public long getModifiedCount() {
        return modifiedCount;
    }

    /**
     * Number of documents inserted/updated/deleted
     */
    public void setModifiedCount(long l) {
        modifiedCount = l;
    }

    /**
     * Number of documents that matched the search cResponseriteria
     */
    public long getMatchCount() {
        return matchCount;
    }

    /**
     * Number of documents that matched the search criteria
     */
    public void setMatchCount(long l) {
        matchCount = l;
    }

    /**
     * If the operation continues asynchronously, the task handle can be used to retrieve status information, and the
     * result of the call once the operation is complete
     */
    public String getTaskHandle() {
        return taskHandle;
    }

    /**
     * If the operation continues asynchronously, the task handle can be used to retrieve status information, and the
     * result of the call once the operation is complete
     */
    public void setTaskHandle(String t) {
        taskHandle = t;
    }

    /**
     * If the operation starts a session or uses an existing session, the session information
     */
    public SessionInfo getSessionInfo() {
        return session;
    }

    /**
     * If the operation starts a session or uses an existing session, the session information
     */
    public void setSessionInfo(SessionInfo s) {
        session = s;
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
        entityData = node;
    }

    /**
     * Errors related to each document
     */
    public List<DataError> getDataErrors() {
        return dataErrors == null ? new ArrayList<DataError>() : dataErrors;
    }

    /**
     * Errors related to the operation
     */
    public List<Error> getErrors() {
    	return errors == null ? new ArrayList<Error>() : errors;
    }

    /**
     * Parses a response from a Json object
     */
    public static Response fromJson(ObjectNode node) {
        ResponseBuilder builder = new ResponseBuilder();
        
        builder.withStatus(node.get("status"));
        builder.withModifiedCount(node.get("modifiedCount"));
        builder.withMatchCount(node.get("matchCount"));
    	builder.withTaskHandle(node.get("taskHandle"));
        builder.withSession(node.get("session"));
        builder.withEntityData(node.get("processed"));
        builder.withDataErrors(node.get("dataErrors"));
        builder.withErrors(node.get("errors"));
        
        return builder.buildResponse();
    }

    /**
     * Returns JSON representation of this
     */
    @Override
    public JsonNode toJson() {
    	ResponseBuilder builder = new ResponseBuilder(this);
    	return builder.buildJson();
    }
}
