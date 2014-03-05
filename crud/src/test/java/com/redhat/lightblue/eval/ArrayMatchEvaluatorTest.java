package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ArrayMatchEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test(expected = com.redhat.lightblue.eval.EvaluationError.class)
    public void non_array_field_results_in_expression_error() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field2','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator.getInstance(expr, md);
    }

    @Test(expected = com.redhat.lightblue.eval.EvaluationError.class)
    public void simple_values_in_expression_for_object_array_results_in_expression_error() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf9','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator.getInstance(expr, md);
    }

    @Test
    public void elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field7','elemMatch':{'field':'elemf3','op':'>','rvalue':10}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field7','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$parent_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$parent.field7','elemMatch':{'field':'elemf3','op':'>','rvalue':10}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$parent_elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$parent.field7','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$parent_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.$parent.$parent.field7','elemMatch':{'field':'elemf3','op':'>','rvalue':10}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$this_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field8.nf1.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':50}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$this_elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field8.nf1.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':5}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$this_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field8.nf1.$this.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':50}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$this_elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field8.nf1.$this.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':5}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(jsonDoc);

        Assert.assertTrue(context.getResult());
    }

}
