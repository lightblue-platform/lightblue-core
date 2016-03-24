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
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.UpdateExpression;

/**
 * Request to update documents based on a query
 */
public class UpdateRequest extends Request implements WithQuery, WithProjection, WithRange {

    private QueryExpression query;
    private UpdateExpression updateExpression;
    private Projection returnFields;
    private Long from;
    private Long to;

    /**
     * The fields to return from the updated documents
     */
    public Projection getReturnFields() {
        return returnFields;
    }

    /**
     * The fields to return from the updated documents
     */
    public void setReturnFields(Projection p) {
        returnFields = p;
    }

    @Override
    public Projection getProjection() {
        return returnFields;
    }

    /**
     * The expression specifying how to modify the documents
     */
    public UpdateExpression getUpdateExpression() {
        return updateExpression;
    }

    /**
     * The expression specifying how to modify the documents
     */
    public void setUpdateExpression(UpdateExpression x) {
        updateExpression = x;
    }

    /**
     * The query specifying which documents to be updated
     */
    @Override
    public QueryExpression getQuery() {
        return query;
    }

    /**
     * The query specifying which documents to be updated
     */
    public void setQuery(QueryExpression q) {
        query = q;
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
        return CRUDOperation.UPDATE;
    }

    /**
     * Returns a json representation of this
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (query != null) {
            node.set("query", query.toJson());
        }
        if (updateExpression != null) {
            node.set("update", updateExpression.toJson());
        }
        if (returnFields != null) {
            node.set("projection", returnFields.toJson());
        }
        WithRange.toJson(this, getFactory(), node);
        return node;
    }

    /**
     * Parses an update request from a Json object
     */
    public static UpdateRequest fromJson(ObjectNode node) {
        UpdateRequest req = new UpdateRequest();
        req.parse(node);
        JsonNode x = node.get("query");
        if (x != null) {
            req.query = QueryExpression.fromJson(x);
        }
        x = node.get("update");
        if (x != null) {
            req.updateExpression = UpdateExpression.fromJson(x);
        }
        x = node.get("projection");
        if (x != null) {
            req.returnFields = Projection.fromJson(x);
        }
        Range r = WithRange.fromJson(node);
        req.setFrom(r.from);
        req.setTo(r.to);
        return req;
    }
}
