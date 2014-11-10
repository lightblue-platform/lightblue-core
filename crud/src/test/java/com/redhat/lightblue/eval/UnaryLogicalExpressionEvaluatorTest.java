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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class UnaryLogicalExpressionEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void $and_expression_case_insensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [{'field':'field1','regex':'Val.*','caseInsensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void $and_expression_case_insensitive_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$and' : [{'field':'field1','regex':'Val.*','caseInsensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void $and_expression_case_sensitiveinsensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$or' : [{'field':'field1','regex':'Val.*'},{'field':'field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_expression_case_insensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [{'field':'field2.$parent.field1','regex':'Val.*','caseInsensitive':1},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_expression_case_insensitive_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$and' : [{'field':'field2.$parent.field1','regex':'Val.*','caseInsensitive':1},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_expression_case_sensitiveinsensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$or' : [{'field':'field2.$parent.field1','regex':'Val.*'},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

}
