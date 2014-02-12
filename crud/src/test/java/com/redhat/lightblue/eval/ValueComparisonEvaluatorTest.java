package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ValueComparisonEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
        doc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test
    public void multiple_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [ {'field':'field4','op':'>','rvalue':3.5},{'field':'field6.nf1','op':'>','rvalue':'nvalue0'}] }");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'>','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void value_comparison_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field4','op':'<','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void value_comparison_returns_true_when_field_has_null_value_and_so_does_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field1','op':'=','rvalue':null}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$parent.field4','op':'>','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_value_comparison_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf7.$parent.$parent.field4','op':'<','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.nf7.$parent.$parent.field4','op':'>','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_value_comparison_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$parent.field4','op':'<','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$this_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.nf3','op':'>','rvalue':2.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$this_value_comparison_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.nf3','op':'<','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$this_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.$this.nf3','op':'>','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$this_value_comparison_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field6.$this.$this.nf3','op':'<','rvalue':3.5}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_multiple_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [ {'field':'field4','op':'>','rvalue':3.5},{'field':'field6.nf7.$parent.nf1','op':'>','rvalue':'nvalue0'}] }");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_multiple_value_comparison_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [ {'field':'field4','op':'>','rvalue':3.5},{'field':'field6.nf7.nnf1.$parent.$parent.nf1','op':'>','rvalue':'nvalue0'}] }");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

}
