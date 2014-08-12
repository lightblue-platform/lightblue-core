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
import com.redhat.lightblue.Request;

/**
 * Abstract base class for requests containing a document list
 */
public abstract class DocRequest extends Request {

    private JsonNode entityData;

    /**
     * Entity data to be saved. this may be an object node containing a single
     * entity, or an array node containing multiple entities. All entities must
     * be of the same type.
     */
    public JsonNode getEntityData() {
        return entityData;
    }

    /**
     * Entity data to be saved. this may be an object node containing a single
     * entity, or an array node containing multiple entities. All entities must
     * be of the same type.
     */
    public void setEntityData(JsonNode data) {
        this.entityData = data;
    }

    /**
     * Returns json representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (entityData != null) {
            node.set("data", entityData);
        }
        return node;
    }

    /**
     * Parses the entitydata from the given Json object
     */
    @Override
    protected void parse(ObjectNode node) {
        super.parse(node);
        entityData = node.get("data");
    }
}
