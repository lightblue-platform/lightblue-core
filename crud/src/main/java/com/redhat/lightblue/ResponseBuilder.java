package com.redhat.lightblue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

public class ResponseBuilder {
	
    private OperationStatus status;
    private long modifiedCount;
    private long matchCount;
    private String taskHandle;
    private SessionInfo session;
    private JsonNode entityData;
    private List<DataError> dataErrors = new ArrayList<>();
    private List<Error> errors = new ArrayList<>();
    
    public ResponseBuilder() {
    	
    }
    
    public ResponseBuilder(Response response) {
    	status = response.getStatus();
    	modifiedCount = response.getModifiedCount();
    	matchCount = response.getMatchCount();
        taskHandle = response.getTaskHandle();
        session = response.getSessionInfo();
        entityData = response.getEntityData();
        dataErrors = response.getDataErrors();
        errors = response.getErrors();
    }
    
    public ResponseBuilder withStatus(JsonNode node) {
    	if (node != null) {
            try {
                status = OperationStatus.valueOf(node.asText().toUpperCase());
            } catch (IllegalArgumentException e) {
                status = OperationStatus.ERROR;
            }
        }
    	return this;
    }
	
    public ResponseBuilder withModifiedCount(JsonNode node) {
    	if (node != null) {
            modifiedCount = node.asLong();
        }
    	return this;
    }
    
    public ResponseBuilder withMatchCount(JsonNode node) {
         if (node != null) {
             matchCount = node.asLong();
         }
    	return this;
    }
    
    public ResponseBuilder withTaskHandle(JsonNode node) {
    	if (node != null) {
            taskHandle = node.asText();
        }
    	return this;
    }
    
    public ResponseBuilder withSession(JsonNode node) {
    	//TODO
    	return this;
    }
    
    public ResponseBuilder withEntityData(JsonNode node) {
    	if (node != null) {
            entityData = node;
        }
    	return this;
    }
    
    public ResponseBuilder withDataErrors(JsonNode node) {
        if (node instanceof ArrayNode) {
            for (Iterator<JsonNode> itr = ((ArrayNode) node).elements();
                    itr.hasNext();) {
                dataErrors.add(DataError.fromJson((ObjectNode) itr.next()));
            }
        }
    	return this;
    }
    
    public ResponseBuilder withErrors(JsonNode node) {
    	if (node instanceof ArrayNode) {
            for (Iterator<JsonNode> itr = ((ArrayNode) node).elements();
                    itr.hasNext();) {
                errors.add(Error.fromJson(itr.next()));
            }
        }
    	return this;
    }
    
    public Response buildResponse() {
    	Response response = new Response();
    	
    	response.setStatus(status);
    	response.setModifiedCount(modifiedCount);
    	response.setMatchCount(matchCount);
    	response.setTaskHandle(taskHandle);
    	response.setSessionInfo(session);
    	response.setEntityData(entityData);
    	response.getDataErrors().addAll(dataErrors);
    	response.getErrors().addAll(errors);
    	
    	return response;
    }
	
    public JsonNode buildJson() {
    	ObjectNode node = JsonObject.getFactory().objectNode();
        if (status != null) {
            node.put("status", status.name().toLowerCase());
        }
        node.put("modifiedCount", modifiedCount);
        node.put("matchCount", matchCount);
        if (taskHandle != null) {
            node.put("taskHandle", taskHandle);
        }
        if (session != null) {
            node.set("session", session.toJson());
        }
        if (entityData != null) {
            node.set("processed", entityData);
        }
        if (!dataErrors.isEmpty()) {
            ArrayNode arr = JsonObject.getFactory().arrayNode();
            node.set("dataErrors", arr);
            for (DataError err : dataErrors) {
                arr.add(err.toJson());
            }
        }
        if (!errors.isEmpty()) {
            ArrayNode arr = JsonObject.getFactory().arrayNode();
            node.set("errors", arr);
            for (Error err : errors) {
                arr.add(err.toJson());
            }
        }
        return node;
    }
    
}
