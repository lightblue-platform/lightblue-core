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
package com.redhat.lightblue.eval;

import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ContainsOperator;
import com.redhat.lightblue.query.Value;

/**
 * Initialize the class with the corresponding expression and the context path.
 * If this is a nested query, the context path determines the field from which
 * the query needs to be evaluated.
 */
public class ArrayContainsEvaluator extends QueryEvaluator {
    private final ArrayContainsExpression expr;
    private final SimpleArrayElement elem;

    public ArrayContainsEvaluator(ArrayContainsExpression expr, FieldTreeNode context) {
        this.expr = expr;
        FieldTreeNode node = context.resolve(expr.getArray());
        if (node == null) {
            throw new EvaluationError(expr);
        }
        if (node instanceof ArrayField) {
            ArrayElement el = ((ArrayField) node).getElement();
            if (el instanceof SimpleArrayElement) {
                elem = (SimpleArrayElement) el;
            } else {
                throw new EvaluationError(expr, CrudConstants.ERR_EXPECTED_SIMPLE_ARRAY);
            }
        } else {
            throw new EvaluationError(expr, CrudConstants.ERR_EXPECTED_ARRAY_FIELD);
        }
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        boolean ret = false;
        JsonNode node = ctx.getNode(expr.getArray());
        if (node instanceof ArrayNode) {
            ArrayNode array = (ArrayNode) node;
            List<Value> values = expr.getValues();
            ContainsOperator op = expr.getOp();
            Type t = elem.getType();
            int numElementsContained = 0;
            for (Iterator<JsonNode> itr = array.elements(); itr.hasNext();) {
                JsonNode valueNode = itr.next();
                for (Value value : values) {
                    Object v = value.getValue();
                    if (isValueInNode(valueNode, v, t)) {
                        numElementsContained++;
                        break;
                    }
                }
            }
            ret = evaluateContainsOperator(op, numElementsContained, values);
        }
        ctx.setResult(ret);
        return ret;
    }

    private boolean isValueInNode(JsonNode valueNode, Object value, Type type) {
        if (valueNode == null || valueNode instanceof NullNode) {
            if (value == null) {
                return true;
            }
        } else if (value != null && elem.getType().compare(value, type.fromJson(valueNode)) == 0) {
            return true;
        }
        return false;
    }

    private boolean evaluateContainsOperator(ContainsOperator op, int numElementsContained, List<Value> values) {
        boolean returnValue = false;
        switch (op) {
            case _any:
                returnValue = numElementsContained > 0;
                break;
            case _all:
                returnValue = numElementsContained == values.size();
                break;
            case _none:
                returnValue = numElementsContained == 0;
                break;
        }
        return returnValue;
    }
}
