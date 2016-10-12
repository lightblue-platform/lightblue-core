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

public class QueryEvalTest extends AbstractJsonNodeTest {

    EntityMetadata md;

    @Before
    public void setup() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void q_value_comparison() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'>','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'<','rvalue':3.5}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{ '$and' : [ {'field':'field4','op':'>','rvalue':3.5},{'field':'field6.nf1','op':'>','rvalue':'nvalue0'}] }");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_in() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field3','op':'$in','values':[3]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_field_comparison() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'>','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_regex() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field1','regex':'val.*'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'field':'field1','regex':'Val.*','caseInsensitive':1}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'field':'field1','regex':'Val.*'}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void q_logical() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [{'field':'field1','regex':'Val.*','caseInsensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'$not': {'$and': [{'field':'field1','regex':'Val.*','caseInsensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'$not': {'$or': [{'field':'field1','regex':'Val.*'},{'field':'field3','op':'$eq','rvalue':4}]}}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_arr_contains() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf6','contains':'$any','values':['one','five','six']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf6','contains':'$all','values':['one','five','six']}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf6','contains':'$all','values':['one','two']}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        q = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf6','contains':'$none','values':['onet','twot']}");
        qe = QueryEvaluator.getInstance(q, md);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_nested_array_elemmatch() throws Exception {
        QueryExpression q=EvalTestContext.queryExpressionFromJson("{'array':'field12.nf1.nnf1.*.nnnf1.arr','elemMatch':{'field':'id','op':'=','rvalue':1}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        
    }
    
    @Test
    public void q_compare_nonexistant_field_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [{'field':'field1','regex':'Val.*','caseInsensitive':1},{'field':'field7.5.elemf1','op':'$eq','rvalue':'x'}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void nullObjcmp() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6','op':'!=','rvalue':null}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

}
