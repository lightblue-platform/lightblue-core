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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class ModifyDocTest {
        
    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    @Test(expected=IllegalArgumentException.class)
    public void emptyPath() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());
        doc.modify(new Path(""), factory.numberNode(1), true);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parentNotAContainer() throws Exception {
        JsonDoc doc = new JsonDoc(factory.booleanNode(true));
        doc.modify(new Path("1"), factory.objectNode(), true);
    }
    
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
    
    @Test(expected=IllegalArgumentException.class)
    public void parentNodeNull() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());
        doc.modify(new Path("x.y.z.1.w"), null, false);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void parentNotContainerNode() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());
        doc.modify(new Path("x.y.z.1"), factory.booleanNode(true), false);
    }
    
    
    @Test
    public void existingNodeRemove() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());

        doc.modify(new Path("x.y.z.1.w"), factory.textNode("test"), true);
        
        JsonNode oldValue = doc.modify(new Path("x.y.z.1.w"), null, true);
        Assert.assertEquals(oldValue, factory.textNode("test"));
        Assert.assertNull(doc.get(new Path("x.y.z.1.w")));

    }

    @Test
    public void existingArrayNodeRemove() throws Exception {
        JsonDoc doc = new JsonDoc(factory.objectNode());
        
        doc.modify(new Path("x.y.z.0"), factory.textNode("ztext0"), true);
        doc.modify(new Path("x.y.z.1"), factory.textNode("ztext1"), true);
        doc.modify(new Path("x.y.z.2"), factory.textNode("ztext2"), true);
        doc.modify(new Path("x.y.z.3.0"), factory.textNode("ztext0"), true);
        
        JsonNode oldValue = doc.modify(new Path("x.y.z.0"), null, false);
        Assert.assertEquals(oldValue, factory.textNode("ztext0"));
       
        Assert.assertEquals(NullNode.class, doc.get(new Path("x.y.z.0")).getClass());
        Assert.assertEquals(TextNode.class, doc.get(new Path("x.y.z.1")).getClass());
        Assert.assertEquals(factory.textNode("ztext1"), doc.get(new Path("x.y.z.1")));
        Assert.assertEquals(factory.textNode("ztext2"), doc.get(new Path("x.y.z.2")));
    }   
    
}
