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

import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.QueryExpression;

public class NaryLogicalExpressionEvaluator extends QueryEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(NaryLogicalExpressionEvaluator.class);

    private final List<QueryEvaluator> evaluators;
    private final NaryLogicalOperator operator;

    public NaryLogicalExpressionEvaluator(NaryLogicalExpression expr,
            FieldTreeNode context) {
        List<QueryExpression> queries = expr.getQueries();
        evaluators = new ArrayList<QueryEvaluator>(queries.size());
        for (QueryExpression q : queries) {
            evaluators.add(QueryEvaluator.getInstance(q, context));
        }
        operator = expr.getOp();
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        boolean ret = false;
        logger.debug("evaluate {}", operator);
        switch (operator) {
            case _and:
                for (QueryEvaluator q : evaluators) {
                    if (!q.evaluate(ctx)) {
                        ret = false;
                        break;
                    }
                }
                ret = true;
                break;
            case _or:
                for (QueryEvaluator q : evaluators) {
                    if (q.evaluate(ctx)) {
                        ret = true;
                        break;
                    }
                }
                ret = false;
                break;
            case _nor:
                for (QueryEvaluator q : evaluators) {
                    if (q.evaluate(ctx)) {
                        ret = false;
                        break;
                    }
                }
                ret = true;
                break;
        }
        ctx.setResult(ret);
        return ret;
    }
}
