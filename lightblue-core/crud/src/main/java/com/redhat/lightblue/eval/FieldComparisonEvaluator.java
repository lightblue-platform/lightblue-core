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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.KeyValueCursor;

public class FieldComparisonEvaluator extends QueryEvaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldComparisonEvaluator.class);

    private final FieldTreeNode fieldMd;
    private final FieldTreeNode rfieldMd;
    private final Path relativePath;
    private final Path rfieldRelativePath;
    private final BinaryComparisonOperator operator;

    /**
     * Constructs evaluator for {field op field} style comparison
     *
     * @param expr The expression
     * @param md Entity metadata
     * @param context The path relative to which the expression will be
     * evaluated
     */
    public FieldComparisonEvaluator(FieldComparisonExpression expr, FieldTreeNode context) {
        this.relativePath = expr.getField();
        this.rfieldRelativePath = expr.getRfield();
        fieldMd = context.resolve(relativePath);
        if (fieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + relativePath);
        }
        rfieldMd = context.resolve(rfieldRelativePath);
        if (rfieldMd == null) {
            throw new EvaluationError(expr, CrudConstants.ERR_FIELD_NOT_THERE + rfieldRelativePath);
        }
        operator = expr.getOp();
        LOGGER.debug("ctor {} {} {}", relativePath, operator, rfieldRelativePath);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        LOGGER.debug("evaluate {} {} {}", relativePath, operator, rfieldRelativePath);
        KeyValueCursor<Path, JsonNode> lcursor = ctx.getNodes(relativePath);
        if (lcursor != null) {
            while (lcursor.hasNext()) {
                lcursor.next();
                JsonNode lvalueNode = lcursor.getCurrentValue();
                Object ldocValue;
                if (lvalueNode != null) {
                    ldocValue = fieldMd.getType().fromJson(lvalueNode);
                } else {
                    ldocValue = null;
                }

                KeyValueCursor<Path, JsonNode> rcursor = ctx.getNodes(rfieldRelativePath);
                if (rcursor != null) {
                    while (rcursor.hasNext()) {
                        rcursor.next();
                        JsonNode rvalueNode = rcursor.getCurrentValue();
                        Object rdocValue;
                        if (rvalueNode != null) {
                            rdocValue = rfieldMd.getType().fromJson(rvalueNode);
                        } else {
                            rdocValue = null;
                        }
                        LOGGER.debug(" lvalue={} rvalue={}", lvalueNode, rvalueNode);
                        int result = fieldMd.getType().compare(ldocValue, rdocValue);
                        LOGGER.debug(" result={}", result);
                        boolean ret = operator.apply(result);
                        if (ret) {
                            ctx.setResult(ret);
                            return ret;
                        }
                    }
                }

            }
        }
        return ctx.getResult();
    }
}
