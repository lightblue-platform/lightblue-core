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

import com.redhat.lightblue.crud.PartialUpdateExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Base class for array update expressions
 * <pre>
 * array_update_expression := $pop: { field: "first" | "last" } |
 *                            $remove: { field: [ value1, value2, ] } |
 *                            $remove: { field: query_expression }
 *                            $push: { field: value } |
 *                            $push: { field: [ value1, value2, ...] }
 * </pre>
 */
public abstract class ArrayUpdateExpression extends PartialUpdateExpression {
	private static final long serialVersionUID = 1L;
	
	private Path field;

    /**
     * Default ctor
     */
    public ArrayUpdateExpression() {
    }

    /**
     * Constructs an array update expression for the given array field
     */
    public ArrayUpdateExpression(Path field) {
        this.field = field;
    }

    /**
     * Returns the update operator
     */
    public abstract UpdateOperator getOp();

    /**
     * Returns the array field
     */
    public Path getField() {
        return field;
    }

    /**
     * Sets the array field
     */
    public void setField(Path p) {
        field = p;
    }

    /**
     * Returns JSON representation of this array update expression
     */
    @Override
    public JsonNode toJson() {
        ObjectNode node = getFactory().objectNode();
        ObjectNode child = getFactory().objectNode();
        child.put(field.toString(), jsonValue());
        node.put(getOp().toString(), child);
        return node;
    }

    /**
     * The implementation should return the JSON object representing the operand value. That is:
     * <pre>
     *   $pop: { field: "first" | "last" } |
     * </pre> The implementation should return "first" or "last"
     */
    protected abstract JsonNode jsonValue();

    /**
     * Parses an array update expression from a Json object, using the given update operator
     */
    public static ArrayUpdateExpression fromJson(UpdateOperator op, ObjectNode node) {
        if (node.size() == 1) {
            String fld = node.fieldNames().next();
            JsonNode value = node.get(fld);
            Path field = new Path(fld);
            switch (op) {
                case _pop:
                    return ArrayPopExpression.fromJson(field, value);
                case _remove:
                    if (value instanceof ArrayNode) {
                        return ArrayRemoveValuesExpression.fromJson(field, (ArrayNode) value);
                    } else {
                        return ArrayRemoveByQueryExpression.fromJson(field, value);
                    }
                case _push:
                    return ArrayPushExpression.fromJson(field, value);
            }
        }
        throw Error.get(ERR_INVALID_UPDATE_EXPRESSION, node.toString());
    }
}
