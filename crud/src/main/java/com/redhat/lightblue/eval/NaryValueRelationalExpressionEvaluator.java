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

import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.query.NaryValueRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

public class NaryValueRelationalExpressionEvaluator extends QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaryValueRelationalExpressionEvaluator.class);

    private final Path field;
    private final FieldTreeNode fieldMd;
    private final NaryRelationalOperator operator;
    private final Set<Object> values;

    public NaryValueRelationalExpressionEvaluator(NaryValueRelationalExpression expr, FieldTreeNode context) {
        field = expr.getField();
        fieldMd = context.resolve(field);
        if (fieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + field);
        }
        operator = expr.getOp();
        values = new HashSet<>();
        for (Value x : expr.getValues()) {
            values.add(fieldMd.getType().cast(x.getValue()));
        }
        LOGGER.debug("ctor {} {} {}", expr.getField(), operator, values);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        LOGGER.debug("evaluate {} {} {}", field, operator, values);
        KeyValueCursor<Path, JsonNode> cursor = ctx.getNodes(field, true);
        boolean ret = false;
        while (cursor.hasNext()) {
            cursor.next();
            JsonNode valueNode = cursor.getCurrentValue();
            Object docValue = fieldMd.getType().fromJson(valueNode);
            LOGGER.debug(" value={}", valueNode);
            boolean in = values.contains(docValue);
            LOGGER.debug(" result={}", in);
            if (in) {
                ret = true;
                break;
            }
        }
        ctx.setResult(operator.apply(ret));
        return ctx.getResult();
    }
}
