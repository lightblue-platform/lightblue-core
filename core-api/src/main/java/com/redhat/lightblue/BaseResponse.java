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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;


/**
 * Common response information from mediator APIs
 */
public class BaseResponse extends JsonObject {

    private static final long serialVersionUID = 1L;

    private static final String PROPERTY_ENTITY = "entity";
    private static final String PROPERTY_VERSION = "entityVersion";
    private static final String PROPERTY_STATUS = "status";
    private static final String PROPERTY_MOD_COUNT = "modifiedCount";
    private static final String PROPERTY_MATCH_COUNT = "matchCount";
    private static final String PROPERTY_TASK_HANDLE = "taskHandle";
    private static final String PROPERTY_SESSION = "session";
    private static final String PROPERTY_DATA_ERRORS = "dataErrors";
    private static final String PROPERTY_ERRORS = "errors";
    private static final String PROPERTY_HOSTNAME = "hostname";

    private EntityVersion entity;
    private OperationStatus status;
    private long modifiedCount;
    private long matchCount;
    private String taskHandle;
    private SessionInfo session;
    private String hostname;
    private final List<DataError> dataErrors;
    private final List<Error> errors;

    final JsonNodeFactory jsonNodeFactory;

    private static final String HOSTNAME;

    static {
        String hostName = "unknown";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (localHost != null) {
                hostName = localHost.getHostName();
            }
        } catch (UnknownHostException e) {

        }
        HOSTNAME = hostName;
    }

    public BaseResponse(JsonNodeFactory jsonNodeFactory, OperationStatus status) {
        this.jsonNodeFactory = jsonNodeFactory;
        this.hostname = HOSTNAME;
        this.dataErrors=new ArrayList<>();
        this.errors=new ArrayList<>();
        setStatus(status);
    }

    public BaseResponse(BaseResponse r) {
        jsonNodeFactory=r.jsonNodeFactory;
        entity=r.entity;
        status=r.status;
        modifiedCount=r.modifiedCount;
        matchCount=r.matchCount;
        taskHandle=r.taskHandle;
        session=r.session;
        hostname=r.hostname;
        dataErrors = r.dataErrors;
        errors = r.errors;
    }

    public EntityVersion getEntity() {
        return entity;
    }

    public void setEntity(EntityVersion e) {
        entity=e;
    }

    public void setEntity(String entityName,String version) {
        this.entity=new EntityVersion(entityName,version);
    }

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
        status = Objects.requireNonNull(s, "Response must always have a status");
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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
     * If the operation continues asynchronously, the task handle can be used to
     * retrieve status information, and the result of the call once the
     * operation is complete
     */
    public String getTaskHandle() {
        return taskHandle;
    }

    /**
     * If the operation continues asynchronously, the task handle can be used to
     * retrieve status information, and the result of the call once the
     * operation is complete
     */
    public void setTaskHandle(String t) {
        taskHandle = t;
    }

    /**
     * If the operation starts a session or uses an existing session, the
     * session information
     */
    public SessionInfo getSessionInfo() {
        return session;
    }

    /**
     * If the operation starts a session or uses an existing session, the
     * session information
     */
    public void setSessionInfo(SessionInfo s) {
        session = s;
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
     * Returns JSON representation of this
     */
    @Override
    public JsonNode toJson() {
        JsonNodeBuilder builder = new JsonNodeBuilder();
        if(entity!=null) {
            builder.add(PROPERTY_ENTITY,entity.getEntity());
            builder.add(PROPERTY_VERSION,entity.getVersion());
        }
        builder.add(PROPERTY_STATUS, status);
        builder.add(PROPERTY_MOD_COUNT, modifiedCount);
        builder.add(PROPERTY_MATCH_COUNT, matchCount);
        builder.add(PROPERTY_TASK_HANDLE, taskHandle);
        builder.add(PROPERTY_SESSION, session);
        builder.add(PROPERTY_HOSTNAME, HOSTNAME);
        builder.addJsonObjectsList(PROPERTY_DATA_ERRORS, dataErrors);
        builder.addErrorsList(PROPERTY_ERRORS, errors);
        return builder.build();
    }
}
