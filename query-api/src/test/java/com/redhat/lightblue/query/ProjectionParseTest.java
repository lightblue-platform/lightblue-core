package com.redhat.lightblue.query;

import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.redhat.lightblue.util.JsonUtils;

public class ProjectionParseTest {
    final String doc1 = "{\"field\":\"field.x\", \"include\": true}";
    final String doc2 = "{\"field\":\"field.y.x\",\"include\": false, \"recursive\": true}";
    final String doc3 = "{\"field\":\"field.z\"}";
    final String doc4 = "{\"field\":\"field.x\",\"include\":true,\"match\":{\"field\":\"field.x\",\"op\":\"$eq\",\"rvalue\":1},\"project\":{\"field\":\"member\"}}";
    final String doc5 = "{\"field\":\"field.x\",\"include\":true,\"project\":{\"field\":\"member\"}}";
    final String doc6 = "{\"field\":\"field.x\",\"include\":true, \"range\":[1,4],\"project\":{\"field\":\"member\"}}";
    final String doc7 = "[" + doc1 + "," + doc2 + "," + doc3 + "]";

    @Test
    public void doc1Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc1));
        Assert.assertTrue(p instanceof FieldProjection);
        FieldProjection x = (FieldProjection) p;
        doc1Asserts(x);
    }

    private void doc1Asserts(FieldProjection x) {
        Assert.assertEquals("field.x", x.getField().toString());
        Assert.assertTrue(x.isInclude());
        Assert.assertTrue(!x.isRecursive());
    }

    @Test
    public void doc2Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc2));
        Assert.assertTrue(p instanceof FieldProjection);
        FieldProjection x = (FieldProjection) p;
        doc2Asserts(x);
    }

    private void doc2Asserts(FieldProjection x) {
        Assert.assertEquals("field.y.x", x.getField().toString());
        Assert.assertTrue(!x.isInclude());
        Assert.assertTrue(x.isRecursive());
    }

    @Test
    public void doc3Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc3));
        Assert.assertTrue(p instanceof FieldProjection);
        FieldProjection x = (FieldProjection) p;
        doc3Asserts(x);
    }

    private void doc3Asserts(FieldProjection x) {
        Assert.assertEquals("field.z", x.getField().toString());
        Assert.assertTrue(x.isInclude());
        Assert.assertTrue(!x.isRecursive());
    }

    @Test
    public void doc4Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc4));
        Assert.assertTrue(p instanceof ArrayQueryMatchProjection);
        ArrayQueryMatchProjection x = (ArrayQueryMatchProjection) p;
        Assert.assertEquals("field.x", x.getField().toString());
        Assert.assertTrue(x.isInclude());
        Assert.assertTrue(x.getMatch() instanceof ValueComparisonExpression);
        Assert.assertEquals("field.x", ((ValueComparisonExpression) x.getMatch()).getField().toString());
        Assert.assertEquals(BinaryComparisonOperator._eq, ((ValueComparisonExpression) x.getMatch()).getOp());
        Assert.assertEquals(1, ((Number) ((ValueComparisonExpression) x.getMatch()).getRvalue().getValue()).intValue());
        Assert.assertTrue(x.getProject() instanceof FieldProjection);
        Assert.assertEquals("member", ((FieldProjection) x.getProject()).getField().toString());
        Assert.assertTrue(((FieldProjection) x.getProject()).isInclude());
        Assert.assertTrue(!((FieldProjection) x.getProject()).isRecursive());
    }

    @Test
    public void doc5Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc5));
        Assert.assertTrue(p instanceof ArrayMatchingElementsProjection);
        ArrayMatchingElementsProjection x = (ArrayMatchingElementsProjection) p;
        Assert.assertEquals("field.x", x.getField().toString());
        Assert.assertTrue(x.isInclude());
        Assert.assertTrue(x.getProject() instanceof FieldProjection);
        Assert.assertEquals("member", ((FieldProjection) x.getProject()).getField().toString());
        Assert.assertTrue(((FieldProjection) x.getProject()).isInclude());
        Assert.assertTrue(!((FieldProjection) x.getProject()).isRecursive());
    }

    @Test
    public void doc6Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc6));
        Assert.assertTrue(p instanceof ArrayRangeProjection);
        ArrayRangeProjection x = (ArrayRangeProjection) p;
        Assert.assertEquals("field.x", x.getField().toString());
        Assert.assertTrue(x.isInclude());
        Assert.assertTrue(x.getProject() instanceof FieldProjection);
        Assert.assertEquals("member", ((FieldProjection) x.getProject()).getField().toString());
        Assert.assertTrue(((FieldProjection) x.getProject()).isInclude());
        Assert.assertTrue(!((FieldProjection) x.getProject()).isRecursive());
        Assert.assertEquals(1, x.getFrom());
        Assert.assertEquals(4, x.getTo());
    }

    @Test
    public void doc7Test() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc7));
        Assert.assertTrue(p instanceof ProjectionList);
        ProjectionList x = (ProjectionList) p;
        Assert.assertEquals(3, x.getItems().size());
        doc1Asserts((FieldProjection) x.getItems().get(0));
        doc2Asserts((FieldProjection) x.getItems().get(1));
        doc3Asserts((FieldProjection) x.getItems().get(2));
    }

    @Test
    public void convertTest() throws Exception {
        JSONAssert.assertEquals(doc1, Projection.fromJson(JsonUtils.json(doc1)).toString(), false);
        JSONAssert.assertEquals(doc2, Projection.fromJson(JsonUtils.json(doc2)).toString(), false);
        JSONAssert.assertEquals(doc3, Projection.fromJson(JsonUtils.json(doc3)).toString(), false);
        JSONAssert.assertEquals(doc4, Projection.fromJson(JsonUtils.json(doc4)).toString(), false);
        JSONAssert.assertEquals(doc5, Projection.fromJson(JsonUtils.json(doc5)).toString(), false);
        JSONAssert.assertEquals(doc6, Projection.fromJson(JsonUtils.json(doc6)).toString(), false);
        JSONAssert.assertEquals(doc7, Projection.fromJson(JsonUtils.json(doc7)).toString(), false);
    }

}
