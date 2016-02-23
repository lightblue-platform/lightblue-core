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

import com.redhat.lightblue.query.QueryExpression;

/**
 * Request to delete documents matching a query
 */
public class DeleteRequest extends Request implements WithQuery {

    private QueryExpression query;

    /**
     * The query whose result set will be deleted
     */
    @Override
    public QueryExpression getQuery() {
        return query;
    }

    /**
     * The query whose result set will be deleted
     */
    public void setQuery(QueryExpression q) {
        query = q;
    }

    @Override
    public CRUDOperation getOperation() {
        return CRUDOperation.DELETE;
    }

    /**
     * Returns a Json node representation of the request
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        if (query != null) {
            node.set("query", query.toJson());
        }
        return node;
    }

    /**
     * Parses an object node and populates a DeleteRequest. It is up to the
     * caller to make sure that the node is actually a DeleteRequest. Any
     * unrecignized elements are ignored.
     */
    public static DeleteRequest fromJson(ObjectNode node) {
        DeleteRequest req = new DeleteRequest();
        req.parse(node);
        JsonNode x = node.get("query");
        if (x != null) {
            req.query = QueryExpression.fromJson(x);
        }
        return req;
    }
}
