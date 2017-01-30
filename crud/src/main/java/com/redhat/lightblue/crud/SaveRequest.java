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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.query.Projection;

/**
 * Request to save documents
 */
public class SaveRequest extends DocRequest implements WithRange, WithProjection, WithIfCurrent {

    private Projection returnFields;
    private boolean upsert;
    private Long from;
    private Long to;
    private boolean ifCurrentOnly;
    private List<String> documentVersions;

    @Override
    public boolean isIfCurrentOnly() {
        return ifCurrentOnly;
    }

    @Override
    public void setIfCurrentOnly(boolean b) {
        ifCurrentOnly=b;
    }

    @Override
    public List<String> getDocumentVersions() {
        return documentVersions;
    }

    @Override
    public void setDocumentVersions(List<String> s) {
        documentVersions=s;
    }

    /**
     * Specifies the fields of the inserted entities to return. This can be used
     * to retrieve the _id fields of the inserted entities.
     */
    public Projection getReturnFields() {
        return returnFields;
    }

    @Override
    public Projection getProjection() {
        return returnFields;
    }

    /**
     * Specifies the fields of the inserted entities to return. This can be used
     * to retrieve the _id fields of the inserted entities.
     */
    public void setReturnFields(Projection p) {
        returnFields = p;
    }

    /**
     * If true, documents that don't exist will be inserted
     */
    public boolean isUpsert() {
        return upsert;
    }

    /**
     * If true, documents that don't exist will be inserted
     */
    public void setUpsert(boolean b) {
        upsert = b;
    }

    @Override
    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    @Override
    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    @Override
    public CRUDOperation getOperation() {
        return CRUDOperation.SAVE;
    }

    /**
     * Returns json representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (returnFields != null) {
            node.set("projection", returnFields.toJson());
        }
        node.put("upsert", upsert);
        WithIfCurrent.toJson(this,node);
        WithRange.toJson(this, getFactory(), node);
        return node;
    }

    /**
     * Parses a save request from a JSON object
     */
    public static SaveRequest fromJson(ObjectNode node) {
        SaveRequest req = new SaveRequest();
        req.parse(node);
        JsonNode x = node.get("projection");
        if (x != null) {
            req.returnFields = Projection.fromJson(x);
        }
        x = node.get("upsert");
        if (x != null) {
            req.upsert = x.asBoolean();
        }
        WithIfCurrent.fromJson(req,node);
        Range r = WithRange.fromJson(node);
        req.setFrom(r.from);
        req.setTo(r.to);
        return req;
    }
}
