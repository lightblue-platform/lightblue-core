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
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class FieldComparisonEvaluatorTest extends AbstractJsonSchemaTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test
    public void field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'>','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'<','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$parent.field4','op':'>','rfield':'field6.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$parent.field4','op':'<','rfield':'field6.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf1.$parent.$parent.field4','op':'>','rfield':'field6.nf1.$parent.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf1.$parent.$parent.field4','op':'<','rfield':'field6.nf1.$parent.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$this_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.elemf3','op':'>','rfield':'field7.0.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$this_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.elemf3','op':'<','rfield':'field7.0.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$this_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.$this.elemf3','op':'>','rfield':'field7.0.$this.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$this_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.$this.elemf3','op':'<','rfield':'field7.0.$this.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void can_compare_simple_arrays() throws Exception {
        // No exception: success
        QueryEvaluator.getInstance(EvalTestContext.
                queryExpressionFromJson("{'field':'field6.nf5','op':'<','rfield':'field3'}"), md);
        QueryEvaluator.getInstance(EvalTestContext.
                queryExpressionFromJson("{'field':'field3','op':'<','rfield':'field6.nf5'}"), md);
        QueryEvaluator.getInstance(EvalTestContext.
                queryExpressionFromJson("{'field':'field6.nf9','op':'<','rfield':'field6.nf5'}"), md);
    }

    @Test
    public void cant_compare_object_arrays() throws Exception {
        try {
            QueryEvaluator.getInstance(EvalTestContext.
                    queryExpressionFromJson("{'field':'field7','op':'<','rfield':'field3'}"), md);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            QueryEvaluator.getInstance(EvalTestContext.
                    queryExpressionFromJson("{'field':'field3','op':'<','rfield':'field7'}"), md);
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            QueryEvaluator.getInstance(EvalTestContext.
                    queryExpressionFromJson("{'field':'field7','op':'<','rfield':'field8.nnf4'}"), md);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void nf3_less_than_nf9() throws Exception {
        QueryEvaluationContext ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf3','op':'<','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void nf5_ne_nf9() throws Exception {
        QueryEvaluationContext ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'=','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'<','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'<=','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'>','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'>=','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'!=','rfield':'field6.nf9'}"), md).
                evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void nf5_e_nf5() throws Exception {
        QueryEvaluationContext ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'=','rfield':'field6.nf5'}"), md).
                evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'<','rfield':'field6.nf5'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'<=','rfield':'field6.nf5'}"), md).
                evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'>','rfield':'field6.nf5'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'>=','rfield':'field6.nf5'}"), md).
                evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
        ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf5','op':'!=','rfield':'field6.nf5'}"), md).
                evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void nf3_greater_than_nf11() throws Exception {
        QueryEvaluationContext ctx = QueryEvaluator.
                getInstance(EvalTestContext.queryExpressionFromJson("{'field':'field6.nf3','op':'>','rfield':'field6.nf11'}"), md).
                evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

}
