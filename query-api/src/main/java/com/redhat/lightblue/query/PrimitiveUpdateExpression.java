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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Error;

/**
 * Base class for primitive update expressions
 * <pre>
 * primitive_update_expression := { $set : { path : rvalue_expression , ...} } |
 *                                { $unset : path } |
 *                                { $unset :[ path, ... ] }
 *                                { $add : { path : rvalue_expression, ... } }
 * </pre>
 */
public abstract class PrimitiveUpdateExpression extends PartialUpdateExpression {

    private static final long serialVersionUID = 1L;

    /**
     * Parses a primitive expression using the given json object
     */
    public static PrimitiveUpdateExpression fromJson(ObjectNode node) {
        if (node.has(UpdateOperator._add.toString()) || node.has(UpdateOperator._set.toString())) {
            if (node.has("fields")) {
                return MaskedSetExpression.fromJson(node);
            }
            return SetExpression.fromJson(node);
        } else if (node.has(UpdateOperator._unset.toString())) {
            return UnsetExpression.fromJson(node);
        } else {
            throw Error.get(QueryConstants.ERR_INVALID_UPDATE_EXPRESSION, node.toString());
        }
    }
}
