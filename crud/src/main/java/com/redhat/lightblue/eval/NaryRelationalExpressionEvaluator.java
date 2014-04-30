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

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

public class NaryRelationalExpressionEvaluator extends QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaryRelationalExpressionEvaluator.class);

    private final Path field;
    private final FieldTreeNode fieldMd;
    private final NaryRelationalOperator operator;
    private final List<Object> values;

    public NaryRelationalExpressionEvaluator(NaryRelationalExpression expr, FieldTreeNode context) {
        field = expr.getField();
        fieldMd = context.resolve(field);
        if (fieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + field);
        }
        operator = expr.getOp();
        List<Value> l = expr.getValues();
        values = new ArrayList<>(l.size());
        for (Value x : l) {
            if (x != null) {
                values.add(x.getValue());
            }
        }
        LOGGER.debug("ctor {} {} {}", expr.getField(), operator, values);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        LOGGER.debug("evaluate {} {} {}", field, operator, values);
        KeyValueCursor<Path,JsonNode> cursor=ctx.getNodes(field);
        boolean ret=false;
        while(cursor.hasNext()) {
            cursor.next();
            JsonNode valueNode = cursor.getCurrentValue();
            Object docValue;
            if (valueNode != null) {
                docValue = fieldMd.getType().fromJson(valueNode);
            } else {
                docValue = null;
            }
            LOGGER.debug(" value={}", valueNode);
            boolean in = false;
            for (Object x : values) {
                if (docValue == null) {
                    if (x == null) {
                        in = true;
                        break;
                    }
                } else if (x != null && fieldMd.getType().compare(docValue, x) == 0) {
                    in = true;
                    break;
                }
            }
            LOGGER.debug(" result={}", in);
            if(in) {
                ret=true;
                break;
            }
        }
        ctx.setResult(operator.apply(ret));
        return ctx.getResult();
    }
}
