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
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ProjectionParseTest {
    final String doc1 = "{\"field\":\"field.x\", \"include\": true}";
    final String doc2 = "{\"field\":\"field.y.x\",\"include\": false, \"recursive\": true}";
    final String doc3 = "{\"field\":\"field.z\"}";
    final String doc4 = "{\"field\":\"field.x\",\"include\":true,\"match\":{\"field\":\"field.x\",\"op\":\"$eq\",\"rvalue\":1},\"project\":{\"field\":\"member\"}}";
    final String doc6 = "{\"field\":\"field.x\",\"include\":true, \"range\":[1,4],\"project\":{\"field\":\"member\"}}";
    final String doc7 = "[" + doc1 + "," + doc2 + "," + doc3 + "]";
    final String doc4s = "{\"field\":\"field.x\",\"include\":true,\"match\":{\"field\":\"field.x\",\"op\":\"$eq\",\"rvalue\":1},\"project\":{\"field\":\"member\"},\"sort\":{\"field\":\"$asc\"}}";
    final String doc6s = "{\"field\":\"field.x\",\"include\":true, \"range\":[1,4],\"project\":{\"field\":\"member\"},\"sort\":{\"field\":\"$asc\"}}";

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
    public void doc4sTest() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc4s));
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
        Assert.assertNotNull(x.getSort());
        Assert.assertEquals("field", ((SortKey) x.getSort()).getField().toString());
        Assert.assertTrue(!((SortKey) x.getSort()).isDesc());
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
    public void doc6sTest() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json(doc6s));
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
        Assert.assertNotNull(x.getSort());
        Assert.assertEquals("field", ((SortKey) x.getSort()).getField().toString());
        Assert.assertTrue(!((SortKey) x.getSort()).isDesc());
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
        JSONAssert.assertEquals(doc6, Projection.fromJson(JsonUtils.json(doc6)).toString(), false);
        JSONAssert.assertEquals(doc7, Projection.fromJson(JsonUtils.json(doc7)).toString(), false);
    }

    @Test
    public void testGetNonRelativePathThis() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json("{\"field\":\"" + Path.THIS + ".field.z\"}"));
        FieldProjection x = (FieldProjection) p;
        doc3Asserts(x);
    }

    @Test
    public void testGetNonRelativePathParent() throws Exception {
        Projection p = Projection.fromJson(JsonUtils.json("{\"field\":\"x.y." + Path.PARENT + ".field.z\"}"));
        FieldProjection x = (FieldProjection) p;
        Assert.assertEquals("x.field.z", x.getField().toString());
        Assert.assertTrue(x.isInclude());
        Assert.assertTrue(!x.isRecursive());
    }

    @Test
    public void testAdd() throws Exception {
        Assert.assertNull(Projection.add(null, null));
        Projection p1 = Projection.fromJson(JsonUtils.json("{\"field\":\"x.y." + Path.PARENT + ".field.z\"}"));
        Projection p2 = Projection.fromJson(JsonUtils.json("{\"field\":\"" + Path.THIS + ".field.z\"}"));
        ProjectionList l = (ProjectionList) Projection.add(p1, p2);
        Assert.assertTrue(l.getItems().size() == 2);

        Assert.assertEquals("x.field.z", ((FieldProjection) l.getItems().get(0)).getField().toString());
        Assert.assertTrue(((FieldProjection) l.getItems().get(0)).isInclude());
        Assert.assertTrue(!((FieldProjection) l.getItems().get(0)).isRecursive());

        Assert.assertEquals("field.z", ((FieldProjection) l.getItems().get(1)).getField().toString());
        Assert.assertTrue(((FieldProjection) l.getItems().get(1)).isInclude());
        Assert.assertTrue(!((FieldProjection) l.getItems().get(1)).isRecursive());

        ProjectionList newL = (ProjectionList) Projection.add(l, l);
        Assert.assertTrue(newL.getItems().size() == 4);

        //just checking the first item
        Assert.assertEquals("x.field.z", ((FieldProjection) newL.getItems().get(0)).getField().toString());
        Assert.assertTrue(((FieldProjection) newL.getItems().get(0)).isInclude());
        Assert.assertTrue(!((FieldProjection) newL.getItems().get(0)).isRecursive());
    }
}
