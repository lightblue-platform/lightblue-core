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

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.RegexMatchExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ArrayMatchExpression;
import com.redhat.lightblue.query.NaryFieldRelationalExpression;
import com.redhat.lightblue.query.NaryValueRelationalExpression;

public abstract class QueryEvaluator {

    public static final QueryEvaluator MATCH_ALL_EVALUATOR = new QueryEvaluator() {
        public boolean evaluate(QueryEvaluationContext ctx) {
            ctx.setResult(true);
            return true;
        }
    };

    public abstract boolean evaluate(QueryEvaluationContext ctx);

    public QueryEvaluationContext evaluate(JsonDoc doc) {
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());
        evaluate(ctx);
        return ctx;
    }

    public static QueryEvaluator getInstance(QueryExpression expr,
                                             EntityMetadata md) {
        return getInstance(expr, md.getFieldTreeRoot());
    }

    public static QueryEvaluator getInstance(QueryExpression expr,
                                             FieldTreeNode context) {
        QueryEvaluator ret = null;
        if (expr instanceof ValueComparisonExpression) {
            return new ValueComparisonEvaluator((ValueComparisonExpression) expr, context);
        } else if (expr instanceof FieldComparisonExpression) {
            return new FieldComparisonEvaluator((FieldComparisonExpression) expr, context);
        } else if (expr instanceof RegexMatchExpression) {
            return new RegexEvaluator((RegexMatchExpression) expr, context);
        } else if (expr instanceof NaryValueRelationalExpression) {
            return new NaryValueRelationalExpressionEvaluator((NaryValueRelationalExpression) expr, context);
        } else if (expr instanceof NaryFieldRelationalExpression) {
            return new NaryFieldRelationalExpressionEvaluator((NaryFieldRelationalExpression) expr, context);
        } else if (expr instanceof UnaryLogicalExpression) {
            return new UnaryLogicalExpressionEvaluator((UnaryLogicalExpression) expr, context);
        } else if (expr instanceof NaryLogicalExpression) {
            return new NaryLogicalExpressionEvaluator((NaryLogicalExpression) expr, context);
        } else if (expr instanceof ArrayContainsExpression) {
            return new ArrayContainsEvaluator((ArrayContainsExpression) expr, context);
        } else if (expr instanceof ArrayMatchExpression) {
            return new ArrayMatchEvaluator((ArrayMatchExpression) expr, context);
        }
        return ret;
    }
}
