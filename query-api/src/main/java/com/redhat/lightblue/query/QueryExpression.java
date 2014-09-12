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
package com.redhat.lightblue.query;

import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

/**
 * Base class for all query expressions
 */
public abstract class QueryExpression extends JsonObject {
    private static final long serialVersionUID = 1L;

    /**
     * Returns the query expressions tnat can be bound to a value
     */
    public List<QueryInContext> getBindableClauses() {
        List<QueryInContext> list=new ArrayList<>(8);
        getBindableClauses(list,Path.EMPTY);
        return list;
    }


    /**
     * Adds the query expressions that can be bound to a value to the given list
     */
    public void getBindableClauses(List<QueryInContext> list,Path ctx) {}

    public QueryExpression bind(List<FieldBinding> bindingResult,
                                Set<Path> bindRequest) {
        return bind(Path.EMPTY,bindingResult,bindRequest);
    }

    /**
     * Binds all the bindable fields in the bindRequest, populates the
     * bindingResult with binding information, and return a new
     * QueryExpression with bound values.
     *
     * @param ctx Context
     * @param bindingResult The results of the bindings will be added to this list
     * @param bindRequest Full paths to the fields to be bound. If
     * there are array elements, '*' must be used
     *
     * @return A new instance of the query object with bound
     * values. If there are no bindable values, the same query object
     * will be returned.
     */
    protected abstract QueryExpression bind(Path ctx,
                                            List<FieldBinding> bindingResult,
                                            Set<Path> bindRequest);

    
    /**
     * Parses a query expression from the given json node
     */
    public static QueryExpression fromJson(JsonNode node) {
        if (node instanceof ObjectNode) {
            ObjectNode onode = (ObjectNode) node;
            // If there is only one field, then that field must be a
            // logical operator
            String firstField = onode.fieldNames().next();
            if (UnaryLogicalOperator.fromString(firstField) != null) {
                return UnaryLogicalExpression.fromJson(onode);
            } else if (NaryLogicalOperator.fromString(firstField) != null) {
                return NaryLogicalExpression.fromJson(onode);
            } else {
                return ComparisonExpression.fromJson(onode);
            }
        } else {
            throw Error.get(QueryConstants.ERR_INVALID_QUERY, node.toString());
        }
    }
}
