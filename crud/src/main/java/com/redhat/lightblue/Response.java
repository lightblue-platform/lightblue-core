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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

/**
 * Response information from mediator APIs
 */
public class Response extends JsonObject {

    private OperationStatus status;
    private long modifiedCount;
    private long matchCount;
    private String taskHandle;
    private SessionInfo session;
    private JsonNode entityData;
    private final List<DataError> dataErrors=new ArrayList<DataError>();
    private final List<Error> errors=new ArrayList<Error>();

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
        status=s;
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
        modifiedCount=l;
    }

    /**
     * Number of documents that matched the search criteria
     */
    public long getMatchCount() {
        return matchCount;
    }

    /**
     * Number of documents that matched the search criteria
     */
    public void setMatchCount(long l) {
        matchCount=l;
    }

    /**
     * If the operation continues asynchronously, the task handle can
     * be used to retrieve status information, and the result of the
     * call once the operation is complete
     */
    public String getTaskHandle() {
        return taskHandle;
    }

    /**
     * If the operation continues asynchronously, the task handle can
     * be used to retrieve status information, and the result of the
     * call once the operation is complete
     */
    public void setTaskHandle(String t) {
        taskHandle=t;
    }

    /**
     * If the operation starts a session or uses an existing session,
     * the session information
     */
    public SessionInfo getSessionInfo() {
        return session;
    }

    /**
     * If the operation starts a session or uses an existing session,
     * the session information
     */
    public void setSessionInfo(SessionInfo s) {
        session=s;
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
        entityData=node;
    }

    /**
     * Errors related to each document
     */
    public List<DataError> getDataErrors() {
        return dataErrors;
    }

    /**
     * Errors related to the operation
     */
    public List<Error> getErrors() {
        return errors;
    }
    
    /**
     * Parses a response from a Json object
     */
    public static Response fromJson(ObjectNode node) {
        Response ret=new Response();
        JsonNode x=node.get("status");
        if(x!=null) {
            String s=x.asText();
            if("complete".equals(s)) {
                ret.status=OperationStatus.COMPLETE;
            } else if("async".equals(s)) {
                ret.status=OperationStatus.ASYNC;
            } else if("partial".equals(s)) {
                ret.status=OperationStatus.PARTIAL;
            } else {
                ret.status=OperationStatus.ERROR;
            }
        }
        x=node.get("modifiedCount");
        if(x!=null) {
            ret.modifiedCount=x.asLong();
        }
        x=node.get("matchCount");
        if(x!=null) {
            ret.matchCount=x.asLong();
        }
        x=node.get("taskHandle");
        if(x!=null) {
            ret.taskHandle=x.asText();
        }
        x=node.get("session");
        // TODO
        x=node.get("processed");
        if(x!=null) {
            ret.entityData=x;
        }
        x=node.get("dataErrors");
        if(x instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                itr.hasNext();) {
                ret.dataErrors.add(DataError.fromJson((ObjectNode)itr.next()));
            }
        }
        x=node.get("errors");
        if(x instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                itr.hasNext();) {
                ret.errors.add(Error.fromJson(itr.next()));
            }
        }
        return ret;
    }

    /**
     * Returns JSON representation of this
     */
    public JsonNode toJson() {
        ObjectNode node=factory.objectNode();
        if(status!=null) {
            String statusStr=null;
            switch(status) {
            case COMPLETE: statusStr="complete";break;
            case ASYNC: statusStr="async";break;
            case PARTIAL: statusStr="partial";break;
            case ERROR: statusStr="error";break;
            }
            if(statusStr!=null) {
                node.put("status",statusStr);
            }
        }
        node.put("modifiedCount",modifiedCount);
        node.put("matchCount",matchCount);
        if(taskHandle!=null) {
            node.put("taskHandle",taskHandle);
        }
        if(session!=null) {
            node.set("session",session.toJson());
        }
        if(entityData!=null) {
            node.set("processed",entityData);
        }
        if(!dataErrors.isEmpty()) {
            ArrayNode arr=factory.arrayNode();
            node.set("dataErrors",arr);
            for(DataError err:dataErrors) {
                arr.add(err.toJson());
            }
        }
        if(!errors.isEmpty()) {
            ArrayNode arr=factory.arrayNode();
            node.set("errors",arr);
            for(Error err:errors) {
                arr.add(err.toJson());
            }
        }
        return node;
    }
}
