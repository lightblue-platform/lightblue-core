package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class RegexEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void regex_on_field_partial_string_match_works() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field1','regex':'val.*'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void regex_on_field_partial_string_case_insensitive_match_works() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field1','regex':'Val.*','case_insensitive':1}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void regex_expr_without_case_insensitive_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field1','regex':'Val.*'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_regex_on_field_partial_string_match_works() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field1','regex':'val.*'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_regex_on_field_partial_string_case_insensitive_match_works() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field1','regex':'Val.*','case_insensitive':1}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_regex_expr_without_case_insensitive_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'field':'field2.$parent.field1','regex':'Val.*'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(jsonDoc);
        ctx = qe.evaluate(jsonDoc);
        Assert.assertFalse(ctx.getResult());
    }

}
