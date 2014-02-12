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
        doc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void $and_expression_case_insensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [{'field':'field1','regex':'Val.*','case_insensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void $and_expression_case_insensitive_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$and' : [{'field':'field1','regex':'Val.*','case_insensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void $and_expression_case_sensitiveinsensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$or' : [{'field':'field1','regex':'Val.*'},{'field':'field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_expression_case_insensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{ '$and' : [{'field':'field2.$parent.field1','regex':'Val.*','case_insensitive':1},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_expression_case_insensitive_returns_false() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$and' : [{'field':'field2.$parent.field1','regex':'Val.*','case_insensitive':1},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_expression_case_sensitiveinsensitive_returns_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$not': { '$or' : [{'field':'field2.$parent.field1','regex':'Val.*'},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        QueryEvaluationContext ctx = qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

}
