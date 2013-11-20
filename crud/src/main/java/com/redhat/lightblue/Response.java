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

public class Response extends JsonObject {

    private OperationStatus status;
    private long modifiedCount;
    private long matchCount;
    private String taskHandle;
    private SessionInfo session;
    private JsonNode entityData;
    private final List<DataError> dataErrors=new ArrayList<DataError>();
    private final List<Error> errors=new ArrayList<Error>();

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus s) {
        status=s;
    }

    public long getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(long l) {
        modifiedCount=l;
    }

    public long getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(long l) {
        matchCount=l;
    }

    public String getTaskHandle() {
        return taskHandle;
    }

    public void setTaskHandle(String t) {
        taskHandle=t;
    }

    public SessionInfo getSessionInfo() {
        return session;
    }

    public void setSessionInfo(SessionInfo s) {
        session=s;
    }

    public List<DataError> getDataErrors() {
        return dataErrors;
    }

    public List<Error> getErrors() {
        return errors;
    }

    static public Response fromJson(ObjectNode node) {
        Response ret=new Response();
        JsonNode x=node.get("status");
        if(x!=null) {
            String s=x.asText();
            if("complete".equals(s))
                ret.status=OperationStatus.COMPLETE;
            else if("async".equals(s))
                ret.status=OperationStatus.ASYNC;
            else if("partial".equals(s))
                ret.status=OperationStatus.PARTIAL;
            else
                ret.status=OperationStatus.ERROR;
        }
        x=node.get("modifiedCount");
        if(x!=null)
            ret.modifiedCount=x.asLong();
        x=node.get("matchCount");
        if(x!=null)
            ret.matchCount=x.asLong();
        x=node.get("taskHandle");
        if(x!=null)
            ret.taskHandle=x.asText();
        x=node.get("session");
        // TODO
        x=node.get("processed");
        if(x!=null)
            ret.entityData=x;
        x=node.get("dataErrors");
        if(x!=null&&x instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                itr.hasNext();)
                ret.dataErrors.add(DataError.fromJson((ObjectNode)itr.next()));
        }
        x=node.get("errors");
        if(x!=null&&x instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                itr.hasNext();)
                ret.errors.add(Error.fromJson(itr.next()));
        }
        return ret;
    }

    public JsonNode toJson() {
        ObjectNode node=factory.objectNode();
        if(status!=null) {
            switch(status) {
            case COMPLETE: node.put("status","complete");break;
            case ASYNC: node.put("status","async");break;
            case PARTIAL: node.put("status","partial");break;
            case ERROR: node.put("status","error");break;
            }
        }
        node.put("modifiedCount",modifiedCount);
        node.put("matchCount",matchCount);
        if(taskHandle!=null)
            node.put("taskHandle",taskHandle);
        if(session!=null)
            node.set("session",session.toJson());
        if(entityData!=null)
            node.set("processed",entityData);
        if(!dataErrors.isEmpty()) {
            ArrayNode arr=factory.arrayNode();
            node.set("dataErrors",arr);
            for(DataError err:dataErrors)
                arr.add(err.toJson());
        }
        if(!errors.isEmpty()) {
            ArrayNode arr=factory.arrayNode();
            node.set("errors",arr);
            for(Error err:errors)
                arr.add(err.toJson());
        }
        return node;
    }
}
