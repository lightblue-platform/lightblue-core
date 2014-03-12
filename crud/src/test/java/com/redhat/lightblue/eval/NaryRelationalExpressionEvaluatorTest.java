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

public class NaryRelationalExpressionEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test
    public void nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field6.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field6.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field6.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field6.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf7.$parent.$parent.field6.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf7.$parent.$parent.field6.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf7.$parent.$parent.field6.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf7.$parent.$parent.field6.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$this_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$this_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$this_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$this_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$this_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.$this.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$this_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.$this.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$this_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.$this.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$this_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.$this.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

}
