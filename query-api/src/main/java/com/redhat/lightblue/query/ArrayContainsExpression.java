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
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Query of the form
 * <pre>
 * array_contains_expression := { array: <field>,
 *                               contains: "$any" | "$all" | "$none",
 *                               values: value_list_array }
 * </pre>
 */
public class ArrayContainsExpression extends ArrayComparisonExpression {

    private static final long serialVersionUID = 1L;
    private final Path array;
    private final ContainsOperator op;
    private final List<Value> values;

    /**
     * Ctor with the given values
     */
    public ArrayContainsExpression(Path array,
                                   ContainsOperator op,
                                   List<Value> values) {
        this.array = array;
        this.op = op;
        this.values = values;
    }

    /**
     * The array field. If this is included in a nested query, relative to the
     * context
     */
    public Path getArray() {
        return this.array;
    }

    /**
     * Contains operator
     */
    public ContainsOperator getOp() {
        return this.op;
    }

    /**
     * The values
     */
    public List<Value> getValues() {
        return values;
    }

    /**
     * Returns a json representation of the query
     */
    @Override
    public JsonNode toJson() {
        ArrayNode arr = getFactory().arrayNode();
        for (Value x : values) {
            arr.add(x.toJson());
        }
        return getFactory().objectNode().
                put("array", array.toString()).
                put("contains", op.toString()).
                set("values", arr);
    }

    /**
     * Parses an ArrayContainsExpression from a JSON object node.
     */
    public static ArrayContainsExpression fromJson(ObjectNode node) {
        JsonNode x = node.get("array");
        if (x != null) {
            Path field = new Path(x.asText());
            x = node.get("contains");
            if (x != null) {
                ContainsOperator op = ContainsOperator.fromString(x.asText());
                if (op != null) {
                    x = node.get("values");
                    if (x instanceof ArrayNode) {
                        ArrayList<Value> values = new ArrayList<>(((ArrayNode) x).size());
                        for (Iterator<JsonNode> itr = ((ArrayNode) x).elements();
                                itr.hasNext();) {
                            values.add(Value.fromJson(itr.next()));
                        }
                        return new ArrayContainsExpression(field, op, values);
                    }
                }
            }
        }
        throw Error.get(QueryConstants.ERR_INVALID_ARRAY_COMPARISON_EXPRESSION, node.toString());
    }
}
