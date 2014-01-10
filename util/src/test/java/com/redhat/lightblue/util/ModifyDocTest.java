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
package com.redhat.lightblue.util;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;

public class ModifyDocTest {
    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    @Test
    public void basicRootLevelNodes() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());

        doc.modify(new Path("field1"), factory.numberNode(1), true);
        doc.modify(new Path("field2"), factory.textNode("blah"), true);

        Assert.assertEquals(1, doc.get(new Path("field1")).intValue());
        Assert.assertEquals("blah", doc.get(new Path("field2")).textValue());
    }

    @Test
    public void basicArrayStuff() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());

        doc.modify(new Path("arr.1"), factory.numberNode(1), true);
        doc.modify(new Path("arr.5"), factory.numberNode(5), true);

        Assert.assertEquals(6, doc.get(new Path("arr")).size());
        Assert.assertEquals(NullNode.class, doc.get(new Path("arr.0")).getClass());
        Assert.assertEquals(1, doc.get(new Path("arr.1")).intValue());
        Assert.assertEquals(NullNode.class, doc.get(new Path("arr.2")).getClass());
        Assert.assertEquals(NullNode.class, doc.get(new Path("arr.3")).getClass());
        Assert.assertEquals(NullNode.class, doc.get(new Path("arr.4")).getClass());
        Assert.assertEquals(5, doc.get(new Path("arr.5")).intValue());
    }

    @Test
    public void nestedStuff() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());

        doc.modify(new Path("x.y.z.1.w"), factory.numberNode(1), true);
        doc.modify(new Path("x.y.z.5.a.b.c.2"), factory.numberNode(2), true);
        Assert.assertEquals(6, doc.get(new Path("x.y.z")).size());
        Assert.assertEquals(1, doc.get(new Path("x.y.z.1.w")).intValue());
        Assert.assertEquals(2, doc.get(new Path("x.y.z.5.a.b.c.2")).intValue());
        System.out.println(doc);
    }

    @Test
    public void existingNodeModify() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());

        doc.modify(new Path("x.y.z.1.w"), factory.numberNode(1), true);
        doc.modify(new Path("x.y.z.5.a.b.c.2"), factory.numberNode(2), true);

        Assert.assertEquals(NullNode.class, doc.get(new Path("x.y.z.0")).getClass());

        doc.modify(new Path("x.y.z.0"), factory.textNode("blah"), false);
        Assert.assertEquals("blah", doc.get(new Path("x.y.z.0")).asText());
        doc.modify(new Path("x.y.z.5.a.b.c.2"), factory.numberNode(3), false);
        Assert.assertEquals(3, doc.get(new Path("x.y.z.5.a.b.c.2")).intValue());
    }
}
