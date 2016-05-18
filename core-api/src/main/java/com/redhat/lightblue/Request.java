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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.util.JsonObject;

import com.redhat.lightblue.crud.CRUDOperation;

/**
 * Common part of all requests to the mediator
 */
public abstract class Request extends JsonObject {

    private EntityVersion entityVersion;
    private ClientIdentification client;
    private ExecutionOptions execution;

    /**
     * Entity name and version
     */
    public EntityVersion getEntityVersion() {
        return entityVersion;
    }

    /**
     * Entity name and version
     */
    public void setEntityVersion(EntityVersion e) {
        entityVersion = e;
    }

    /**
     * Information that uniquely identifies the caller
     */
    public ClientIdentification getClientId() {
        return client;
    }

    /**
     * Information that uniquely identifies the caller
     */
    public void setClientId(ClientIdentification c) {
        client = c;
    }

    /**
     * Information about requested execution options
     */
    public ExecutionOptions getExecution() {
        return execution;
    }

    /**
     * Information about requested execution options
     */
    public void setExecution(ExecutionOptions e) {
        execution = e;
    }

    /**
     * Copies contents of r to this. Performs a shallow copy
     */
    public void shallowCopyFrom(Request r) {
        entityVersion = r.entityVersion;
        client = r.client;
        execution = r.execution;
    }

    public abstract CRUDOperation getOperation();

    /**
     * Returns a JSON representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = getFactory().objectNode();
        node.put("entity", entityVersion.getEntity());
        if (entityVersion.getVersion() != null) {
            node.put("entityVersion", entityVersion.getVersion());
        }
        if (client != null) {
            node.set("client", client.toJson());
        }
        if (execution != null) {
            node.set("execution", execution.toJson());
        }
        if (getOperation() != null) {
            node.put("op", getOperation().name());
        }
        return node;
    }

    /**
     * Parses the entity, client identification and execution options from the
     * given json object
     */
    protected void parse(ObjectNode node) {
        entityVersion = new EntityVersion();
        JsonNode x = node.get("entity");
        if (x != null && !(x instanceof NullNode)) {
            entityVersion.setEntity(x.asText());
        }
        x = node.get("entityVersion");
        if (x != null && !(x instanceof NullNode)) {
            entityVersion.setVersion(x.asText());
        }
        // TODO: clientIdentification
        x = node.get("execution");
        if (x != null) {
            execution = ExecutionOptions.fromJson((ObjectNode) x);
        }
    }
}
