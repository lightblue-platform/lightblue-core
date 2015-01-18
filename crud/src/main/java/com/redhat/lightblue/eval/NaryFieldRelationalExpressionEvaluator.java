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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.query.NaryFieldRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

public class NaryFieldRelationalExpressionEvaluator extends QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NaryFieldRelationalExpressionEvaluator.class);

    private final Path field;
    private final FieldTreeNode fieldMd;
    private final NaryRelationalOperator operator;
    private final Path rfield;
    private final ArrayField rfieldMd;

    public NaryFieldRelationalExpressionEvaluator(NaryFieldRelationalExpression expr, FieldTreeNode context) {
        field = expr.getField();
        fieldMd = context.resolve(field);
        if (fieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + field);
        }
        rfield = expr.getRfield();
        FieldTreeNode rf = context.resolve(rfield);
        if(rf == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + field);
        }
        if(rf instanceof ArrayField) {
            rfieldMd=(ArrayField)rf;
        } else {
            throw new EvaluationError(expr,CrudConstants.ERR_REQUIRED_ARRAY + rfield);
        }
        operator = expr.getOp();
        LOGGER.debug("ctor {} {} {}", field, operator, rfield);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        LOGGER.debug("evaluate {} {} {}", field, operator, rfield);
        KeyValueCursor<Path, JsonNode> cursor = ctx.getNodes(field);
        boolean ret = false;
        while (cursor.hasNext()) {
            cursor.next();
            JsonNode valueNode = cursor.getCurrentValue();
            Object docValue;
            if (valueNode != null) {
                docValue = fieldMd.getType().fromJson(valueNode);
            } else {
                docValue = null;
            }
            boolean in = false;
            KeyValueCursor<Path, JsonNode> rcursor = ctx.getNodes(rfield);
            while(rcursor.hasNext()) {
                rcursor.next();
                JsonNode lnode = rcursor.getCurrentValue();
                if(lnode!=null&&lnode instanceof ArrayNode) {
                    ArrayNode listNode=(ArrayNode)lnode;
                    LOGGER.debug("value={} list={}",valueNode,listNode);

                    for (Iterator<JsonNode> itr=listNode.elements();itr.hasNext();) {
                        JsonNode rvalue=itr.next();
                        if (docValue == null) {
                            if (rvalue==null||rvalue instanceof NullNode) 
                                in = true;
                            break;
                        } else if (fieldMd.getType().compare(docValue, rvalue) == 0) {
                            in = true;
                            break;
                        }
                    }
                    LOGGER.debug("in={}",in);
                    if (in) {
                        ret = true;
                        break;
                    }
                }
            }
        }
        ctx.setResult(operator.apply(ret));
        return ctx.getResult();
    }
}
