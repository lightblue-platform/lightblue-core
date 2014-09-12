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

import java.util.Set;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Represents an expression of the form
 * <pre>
 * {
 *   field: <fieldName>,
 *   op: <operator>,
 *   rvalue: <value>
 * }
 * </pre>
 *
 * If this is part of a nested query, the field is a relative path.
 */
public class ValueComparisonExpression extends BinaryRelationalExpression {

    private static final long serialVersionUID = 1L;

    private final Path field;
    private final BinaryComparisonOperator op;
    protected Value rvalue;

    /**
     * Initializes all fields
     */
    public ValueComparisonExpression(Path field,
                                     BinaryComparisonOperator op,
                                     Value rvalue) {
        this.field = field;
        this.op = op;
        this.rvalue = rvalue;
    }

    /**
     * Returns the field path.
     */
    public Path getField() {
        return this.field;
    }

    /**
     * Returns the operator
     */
    public BinaryComparisonOperator getOp() {
        return this.op;
    }

    /**
     * Returns the right-value
     */
    public Value getRvalue() {
        return this.rvalue;
    }

    @Override
    protected QueryExpression bind(Path ctx,
                                   List<FieldBinding> bindingResult,
                                   Set<Path> bindRequest) {
        if(bindRequest.contains(new Path(ctx,field)))
            throw Error.get(QueryConstants.ERR_INVALID_VALUE_BINDING,this.toString());
        return this;
    }

    /**
     * Returns the json representation of the query
     */
    @Override
    public JsonNode toJson() {
        return getFactory().objectNode().put("field", field.toString()).
                put("op", op.toString()).
                set("rvalue", rvalue.toJson());
    }
}
