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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JsonNodeDocTest extends AbstractJsonNodeTest {

    protected JsonNode createJsonNode(String postfix) {
        try {
            return loadJsonNode("JsonNodeDocTest-" + postfix + ".json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getSimple() {
        JsonNode node = createJsonNode("simple");
        JsonDoc doc = new JsonDoc(node);

        JsonNode result = doc.get(new Path("simple"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value", ((TextNode) result).textValue());
    }

    @Test
    public void getObject() {
        JsonNode node = createJsonNode("object");
        JsonDoc doc = new JsonDoc(node);

        JsonNode result = doc.get(new Path("object"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof ObjectNode);

        result = doc.get(new Path("object.simple"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value", ((TextNode) result).textValue());
    }

    @Test
    public void getArray() {
        JsonNode node = createJsonNode("array");
        JsonDoc doc = new JsonDoc(node);

        JsonNode result = doc.get(new Path("array"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof ArrayNode);
        Assert.assertTrue(node.size() > 0);

        JsonNodeCursor i = doc.cursor(new Path("array"));
        Assert.assertTrue(i.firstChild());

        for (int a = 0; i.nextSibling(); a++) {
            Assert.assertEquals(String.valueOf(a), ((TextNode) i.getCurrentNode()).textValue());
        }
    }

    @Test
    public void getComplex() {
        JsonNode node = createJsonNode("complex");
        JsonDoc doc = new JsonDoc(node);

        JsonNode result = doc.get(new Path("object1.array1.1.simple2"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value2", ((TextNode) result).textValue());

        result = doc.get(new Path("object2.simple3"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value3", ((TextNode) result).textValue());

        result = doc.get(new Path("object2.array2.0"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof ObjectNode);

    }

    @Test
    public void itr() {
        JsonNode node = createJsonNode("complexarray");
        JsonDoc doc = new JsonDoc(node);

        JsonNode x = doc.get(new Path("array1.1.nested1.0"));
        Assert.assertEquals(1, x.asInt());

        KeyValueCursor<Path, JsonNode> c = doc.getAllNodes(new Path("array1.*.deep.0.deeper.*"));
        System.out.println(c);
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(1, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(1, c.getCurrentValue().asInt());
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(2, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(2, c.getCurrentValue().asInt());
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(3, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(3, c.getCurrentValue().asInt());
        Assert.assertTrue(c.hasNext());
        c.next();
        Assert.assertEquals(4, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(4, c.getCurrentValue().asInt());
        Assert.assertTrue(!c.hasNext());

        c = doc.getAllNodes(new Path("array1.0.nested1.0"));
        Assert.assertTrue(c.hasNext());
        c.next();
        System.out.println(c.getCurrentKey());
        Assert.assertEquals(1, doc.get(c.getCurrentKey()).asInt());
        Assert.assertEquals(1, c.getCurrentValue().asInt());
        Assert.assertTrue(!c.hasNext());
    }
}
