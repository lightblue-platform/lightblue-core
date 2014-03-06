package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ArrayRangeProjectorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void array_range_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(jsonDoc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf3':4},{'elemf3':5}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void array_range_projection_with_no_match_returns_empty_node() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','range':[5,6],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(jsonDoc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_array_range_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.$parent.field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(jsonDoc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf3':4},{'elemf3':5}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_array_range_projection_with_no_match_returns_empty_node() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.$parent.field7','range':[5,6],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(jsonDoc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_array_range_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.nf7.$parent.$parent.field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(jsonDoc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf3':4},{'elemf3':5}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_array_range_projection_with_no_match_returns_empty_node() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.nf7.$parent.$parent.field7','range':[5,6],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(jsonDoc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

}
