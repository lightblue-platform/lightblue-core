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

import java.io.IOException;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class JsonNodeDocTest extends AbstractJsonNodeTest {

    protected JsonNode createJsonNode(String postfix) {
        try {
            return loadJsonNode("JsonNodeDocTest-" + postfix + ".json");
        } catch (Throwable e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void getSimple() throws IOException {
        JsonNode node = createJsonNode("simple");
        JsonDocAdapter adapter = new JsonDocAdapter();
        Doc<JsonNode> doc = new Doc<JsonNode>(adapter, node);

        JsonNode result = doc.get(new Path("simple"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value", ((TextNode) result).textValue());
    }

    @Test
    public void getObject() {
        JsonNode node = createJsonNode("object");
        JsonDocAdapter adapter = new JsonDocAdapter();
        Doc<JsonNode> doc = new Doc<JsonNode>(adapter, node);

        JsonNode result = doc.get(new Path("object"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof ObjectNode);
        Assert.assertTrue(adapter.getNumChildren(node) > 0);

        result = doc.get(new Path("object.simple"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof TextNode);
        Assert.assertEquals("value", ((TextNode) result).textValue());
    }

    @Test
    public void getArray() {
        JsonNode node = createJsonNode("array");
        JsonDocAdapter adapter = new JsonDocAdapter();
        Doc<JsonNode> doc = new Doc<JsonNode>(adapter, node);

        JsonNode result = doc.get(new Path("array"));
        Assert.assertNotNull(result);
        Assert.assertTrue("unexpected class", result instanceof ArrayNode);
        Assert.assertTrue(adapter.getNumChildren(node) > 0);

        Iterator<JsonNode> i = adapter.getChildren(result);

        for (int a = 0; i.hasNext(); a++) {
            Assert.assertEquals(String.valueOf(a), ((TextNode) i.next()).textValue());
        }
    }

    @Test
    public void getComplex() {
        JsonNode node = createJsonNode("complex");
        JsonDocAdapter adapter = new JsonDocAdapter();
        Doc<JsonNode> doc = new Doc<JsonNode>(adapter, node);

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
        Assert.assertTrue(adapter.getNumChildren(result) > 0);
    }
}
