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
import com.redhat.lightblue.util.Path;

/**
 * Represents a query of the form
 * <pre>
 * nary_relational_expression := { field: <field>,
 *                                 op: nary_comparison_operator,
 *                                 rfield: array_field }
 * </pre>
 */
public class NaryFieldRelationalExpression extends NaryRelationalExpression {

    private final Path rfield;

    /**
     * Ctor with the given values
     */
    public NaryFieldRelationalExpression(Path field,
                                         NaryRelationalOperator op,
                                         Path rfield) {
        super(field, op);
        this.rfield = rfield;
    }

    /**
     * Array field against which to compare the field
     */
    public Path getRfield() {
        return rfield;
    }

    /**
     * Returns a json representation of this query
     */
    @Override
    public JsonNode toJson() {
        return getFactory().objectNode().put("field", getField().toString()).
                put("op", getOp().toString()).
                put("rfield", rfield.toString());
    }

    /**
     * Parses an n-ary relational expression from the given json object
     */
    public static NaryFieldRelationalExpression fromJson(ObjectNode node) {
        if (node.size() == 3) {
            JsonNode x = node.get("op");
            if (x != null) {
                NaryRelationalOperator op
                        = NaryRelationalOperator.fromString(x.asText());
                if (op != null) {
                    x = node.get("field");
                    if (x != null) {
                        Path field = new Path(x.asText());
                        x = node.get("rfield");
                        if (x != null) {
                            return new NaryFieldRelationalExpression(field, op, new Path(x.asText()));
                        }
                    }
                }
            }
        }
        throw Error.get(QueryConstants.ERR_INVALID_COMPARISON_EXPRESSION, node.toString());
    }
}
