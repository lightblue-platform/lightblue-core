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
        doc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test
    public void field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'>','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'<','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$parent.field4','op':'>','rfield':'field6.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$parent.field4','op':'<','rfield':'field6.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf1.$parent.$parent.field4','op':'>','rfield':'field6.nf1.$parent.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf1.$parent.$parent.field4','op':'<','rfield':'field6.nf1.$parent.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$this_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.elemf3','op':'>','rfield':'field7.0.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$this_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.elemf3','op':'<','rfield':'field7.0.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$this_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.$this.elemf3','op':'>','rfield':'field7.0.$this.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$this_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field7.1.$this.$this.elemf3','op':'<','rfield':'field7.0.$this.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }
}
