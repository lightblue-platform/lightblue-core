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
import com.redhat.lightblue.util.Path;

/**
 * Represents a query of the form
 * <pre>
 * nary_relational_expression := { field: <field>,
 *                                 op: nary_comparison_operator,
 *                                 values: value_list_array }
 * </pre>
 * or
 * <pre>
 * nary_relational_expression := { field: <field>,
 *                                 op: nary_comparison_operator,
 *                                 rfield: array_field }
 * </pre>
 */
public abstract class NaryRelationalExpression extends RelationalExpression {

    private final Path field;
    private final NaryRelationalOperator op;

    /**
     * Ctor with the given values
     */
    public NaryRelationalExpression(Path field,
                                    NaryRelationalOperator op) {
        this.field = field;
        this.op = op;
    }

    /**
     * The field. If this is a nested query, the field is relative to the
     * context
     */
    public Path getField() {
        return this.field;
    }

    /**
     * The operator
     */
    public NaryRelationalOperator getOp() {
        return this.op;
    }

    /**
     * Parses an n-ary relational expression from the given json object
     */
    public static NaryRelationalExpression fromJson(ObjectNode node) {
        if (node.size() == 3) {
            if (node.get("rfield") != null) {
                return NaryFieldRelationalExpression.fromJson(node);
            } else if(node.get("values")!=null) {
                return NaryValueRelationalExpression.fromJson(node);
            }
        }
        throw Error.get(QueryConstants.ERR_INVALID_COMPARISON_EXPRESSION, node.toString());
    }
}
