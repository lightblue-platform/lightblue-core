package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class NaryLogicalExpressionEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        md = EvalTestContext.getMd("./testMetadata.json");
        doc = EvalTestContext.getDoc("./sample1.json");
    }

    @Test
    public void $and_logical_expression_returns_true_when_all_expressions_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$and': [ {'field':'field3','op':'$gt','rvalue':2},{'field':'field7.0.elemf1','op':'$eq','rvalue':'elvalue0_1'}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_$and_logical_expression_returns_true_when_all_expressions_true() throws Exception {
        QueryExpression q = EvalTestContext.queryExpressionFromJson("{'$and': [ {'field':'field2.$parent.field3','op':'$gt','rvalue':2},{'field':'field2.$parent.field7.0.elemf1','op':'$eq','rvalue':'elvalue0_1'}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

}
