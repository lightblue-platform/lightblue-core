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

import com.redhat.lightblue.crud.DocRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.query.Projection;

/**
 * Request to insert documents
 */
public class InsertionRequest extends DocRequest {

    private Projection returnFields;

    /**
     * Specifies the fields of the inserted entities to return. This can be used to retrieve the _id fields of the
     * inserted entities.
     */
    public Projection getReturnFields() {
        return returnFields;
    }

    /**
     * Specifies the fields of the inserted entities to return. This can be used to retrieve the _id fields of the
     * inserted entities.
     */
    public void setReturnFields(Projection p) {
        returnFields = p;
    }

    /**
     * Returns JSON representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (returnFields != null) {
            node.set("projection", returnFields.toJson());
        }
        return node;
    }

    /**
     * Parses an insertion request from a json object. Unrecognized elements are ignored.
     */
    public static InsertionRequest fromJson(ObjectNode node) {
        InsertionRequest req = new InsertionRequest();
        req.parse(node);
        JsonNode x = node.get("projection");
        if (x != null) {
            req.returnFields = Projection.fromJson(x);
        }
        return req;
    }
}
