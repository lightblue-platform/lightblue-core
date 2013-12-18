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

import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.UnaryLogicalOperator;

import com.redhat.lightblue.util.Path;

public class UnaryLogicalExpressionEvaluator extends QueryEvaluator {

    private static final Logger logger=LoggerFactory.getLogger(UnaryLogicalExpressionEvaluator.class);

    private final QueryEvaluator evaluator;
    private final UnaryLogicalOperator operator;

    public UnaryLogicalExpressionEvaluator(UnaryLogicalExpression expr,
                                           FieldTreeNode context) {
        evaluator=QueryEvaluator.getInstance(expr.getQuery(),context);
        operator=expr.getOp();
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        logger.debug("evaluate {}",operator);
        ctx.setResult(operator.apply(evaluator.evaluate(ctx)));
        return ctx.getResult();
    }
}
