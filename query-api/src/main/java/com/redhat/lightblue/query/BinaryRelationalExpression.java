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
 * Base class for all binary relational expression
 */
public abstract class BinaryRelationalExpression
        extends RelationalExpression {

    /**
     * Parses a field comparison or value comparison expression from the given json object
     */
    public static BinaryRelationalExpression fromJson(ObjectNode node) {
        if (node.size() == 3) {
            JsonNode x = node.get("op");
            if (x != null) {
                BinaryComparisonOperator op
                        = BinaryComparisonOperator.fromString(x.asText());
                if (op != null) {
                    x = node.get("field");
                    if (x != null) {
                        Path field = new Path(x.asText());
                        x = node.get("rfield");
                        if (x != null) {
                            return new FieldComparisonExpression(field, op, new Path(x.asText()));
                        } else {
                            x = node.get("rvalue");
                            if (x != null) {
                                return new ValueComparisonExpression(field, op, Value.fromJson(x));
                            }
                        }
                    }
                }
            }
        }
        throw Error.get(INVALID_COMPARISON_EXPRESSION, node.toString());
    }
}
