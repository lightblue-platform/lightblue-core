package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ArrayContainsEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
        doc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test(expected = com.redhat.lightblue.eval.EvaluationError.class)
    public void non_array_field_results_in_expression_error() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field1', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator.getInstance(expr, md);
    }

    @Test(expected = com.redhat.lightblue.eval.EvaluationError.class)
    public void simple_values_in_expression_for_object_array_results_in_expression_error() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field7', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator.getInstance(expr, md);
    }

    @Test
    public void contains_any_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf5', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void contains_any_returns_true_when_any_expression_value_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf5', 'contains':'$any', 'values':[1,2,3,4,5]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void contains_all_returns_true_when_all_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf5', 'contains':'$all', 'values':[5,10,15,20]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void contains_all_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf5', 'contains':'$all', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void contains_none_returns_false_when_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf5', 'contains':'$none', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void contains_none_returns_true_when_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf5', 'contains':'$none', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$parent_contains_any_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf4.$parent.nf5', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$parent_contains_any_returns_true_when_any_expression_value_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf4.$parent.nf5', 'contains':'$any', 'values':[1,2,3,4,5]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$parent_contains_all_returns_true_when_all_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf4.$parent.nf5', 'contains':'$all', 'values':[5,10,15,20]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$parent_contains_all_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf4.$parent.nf5', 'contains':'$all', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$parent_contains_none_returns_false_when_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf4.$parent.nf5', 'contains':'$none', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$parent_contains_none_returns_true_when_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf4.$parent.nf5', 'contains':'$none', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$parent_contains_any_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.nnf1.$parent.$parent.nf5', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$parent_contains_any_returns_true_when_any_expression_value_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.nnf1.$parent.$parent.nf5', 'contains':'$any', 'values':[1,2,3,4,5]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$parent_contains_all_returns_true_when_all_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.nnf1.$parent.$parent.nf5', 'contains':'$all', 'values':[5,10,15,20]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$parent_contains_all_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.nnf1.$parent.$parent.nf5', 'contains':'$all', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$parent_contains_none_returns_false_when_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.nnf1.$parent.$parent.nf5', 'contains':'$none', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$parent_contains_none_returns_true_when_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.nf7.nnf1.$parent.$parent.nf5', 'contains':'$none', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$this_contains_any_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.nf5', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$this_contains_any_returns_true_when_any_expression_value_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.nf5', 'contains':'$any', 'values':[1,2,3,4,5]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$this_contains_all_returns_true_when_all_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.nf5', 'contains':'$all', 'values':[5,10,15,20]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$this_contains_all_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.nf5', 'contains':'$all', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$this_contains_none_returns_false_when_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.nf5', 'contains':'$none', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$this_contains_none_returns_true_when_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.nf5', 'contains':'$none', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$this_contains_any_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.$this.nf5', 'contains':'$any', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$this_contains_any_returns_true_when_any_expression_value_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.$this.nf5', 'contains':'$any', 'values':[1,2,3,4,5]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$this_contains_all_returns_true_when_all_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.$this.nf5', 'contains':'$all', 'values':[5,10,15,20]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void two_$this_contains_all_returns_false_when_all_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.$this.nf5', 'contains':'$all', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$this_contains_none_returns_false_when_expression_values_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.$this.nf5', 'contains':'$none', 'values':[5,10,15,25]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$this_contains_none_returns_true_when_expression_values_not_in_array() throws Exception {
        QueryExpression expr = EvalTestContext.queryExpressionFromJson("{'array':'field6.$this.$this.nf5', 'contains':'$none', 'values':[1,2,3,4]}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }
}
