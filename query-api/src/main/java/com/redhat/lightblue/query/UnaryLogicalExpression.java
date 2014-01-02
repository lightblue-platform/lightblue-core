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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;

/**
 * Expression of the form
 * <pre>
 * { $not : { query } }
 * </pre>
 */
public class UnaryLogicalExpression extends LogicalExpression {

    private UnaryLogicalOperator op;
    private QueryExpression query;

    public UnaryLogicalExpression(UnaryLogicalOperator op,
            QueryExpression query) {
        this.op = op;
        this.query = query;
    }

    /**
     * Returns the operator
     */
    public UnaryLogicalOperator getOp() {
        return this.op;
    }

    /**
     * Returns the query to which the operator will be applied
     */
    public QueryExpression getQuery() {
        return this.query;
    }

    /**
     * Returns a json representation of the query
     */
    @Override
    public JsonNode toJson() {
        return getFactory().objectNode().set(op.toString(), query.toJson());
    }

    /**
     * Parses a unary logical expression using the given object node
     */
    public static UnaryLogicalExpression fromJson(ObjectNode node) {
        if (node.size() != 1) {
            throw Error.get(INVALID_LOGICAL_EXPRESSION, node.toString());
        }
        String fieldName = node.fieldNames().next();
        UnaryLogicalOperator op = UnaryLogicalOperator.fromString(fieldName);
        if (op == null) {
            throw Error.get(INVALID_LOGICAL_EXPRESSION, node.toString());
        }
        QueryExpression q = QueryExpression.fromJson(node.get(fieldName));
        return new UnaryLogicalExpression(op, q);
    }
}
