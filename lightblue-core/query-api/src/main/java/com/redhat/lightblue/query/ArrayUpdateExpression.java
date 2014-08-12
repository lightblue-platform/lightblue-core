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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;

/**
 * Base class for array update expressions
 * <pre>
 * array_update_expression := { $append : { path : rvalue_expression } } |
 *                            { $append : { path : [ rvalue_expression, ... ] }} |
 *                            { $insert : { path : rvalue_expression } } |
 *                            { $insert : { path : [ rvalue_expression,...] }} |
 *                            { $foreach : { path : update_query_expression,
 *                                           $update : foreach_update_expression } }
 * </pre>
 */
public abstract class ArrayUpdateExpression extends PartialUpdateExpression {

    private static final long serialVersionUID = 1L;

    /**
     * Parses an array update expression using the given json object
     */
    public static ArrayUpdateExpression fromJson(ObjectNode node) {
        if (node.has(UpdateOperator._append.toString()) || node.has(UpdateOperator._insert.toString())) {
            return ArrayAddExpression.fromJson(node);
        } else if (node.has(UpdateOperator._foreach.toString())) {
            return ForEachExpression.fromJson(node);
        } else {
            throw Error.get(QueryConstants.ERR_INVALID_ARRAY_UPDATE_EXPRESSION, node.toString());
        }

    }
}
