/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import java.io.IOException;
import java.util.Iterator;

import junit.framework.Assert;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 *
 * @author nmalik
 */
public class JsonNodeCursorTest {

    private Iterator<JsonNode> getChildren(JsonNode node) {
        if (node instanceof ArrayNode) {
            return ((ArrayNode) node).elements();
        } else if (node instanceof ObjectNode) {
            return ((ObjectNode) node).elements();
        }
        return null;
    }

    @Test
    public void illegalRoot() throws IOException {
        try {
            TextNode node = new TextNode("value");
            new JsonNodeCursor(new Path(""), node);
            Assert.fail("Expected IllegalArgumentException with TextNode as root element");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void getCurrentPath() throws IOException {
        String jsonString = "{\"text\":\"value\"}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Path p = c.getCurrentPath();
        Assert.assertNotNull(p);
        Assert.assertFalse(p instanceof MutablePath);
    }

    @Test
    public void firstChild_TextNode() throws IOException {
        String jsonString = "{\"text\":\"value\"}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof TextNode);
        Assert.assertEquals("text", c.getCurrentPath().toString());
    }

    @Test
    public void firstChild_ObjectNode() throws IOException {
        String jsonString = "{\"x\":{\"text\":\"value\",\"foo\":\"bar\"}}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof ObjectNode);
        Assert.assertEquals("x", c.getCurrentPath().toString());
    }

    @Test
    public void firstChild_ArrayNode() throws IOException {
        String jsonString = "{\"x\":[1,2,3,4]}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof ArrayNode);
        Assert.assertEquals("x", c.getCurrentPath().toString());
    }

    @Test
    public void nextSibling_TextNode() throws IOException {
        String jsonString = "{\"text\":\"value\"}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertFalse(c.firstChild());
        Assert.assertFalse(c.nextSibling());
    }

    @Test
    public void nextSibling_ObjectNode() throws IOException {
        String jsonString = "{\"x\":{\"text\":\"value\",\"foo\":\"bar\"}}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertTrue(c.firstChild());

        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof TextNode);
        Assert.assertEquals("x.text", c.getCurrentPath().toString());

        Assert.assertTrue(c.nextSibling());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof TextNode);
        Assert.assertEquals("x.foo", c.getCurrentPath().toString());
    }

    @Test
    public void nextSibling_ArrayNode() throws IOException {
        String jsonString = "{\"x\":[1,2,3,4]}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertTrue(c.firstChild());

        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof NumericNode);
        Assert.assertEquals("x.0", c.getCurrentPath().toString());
    }

    @Test
    public void hasChildren_TextNode() throws IOException {
        String jsonString = "{\"text\":\"value\"}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof TextNode);
        Assert.assertEquals("text", c.getCurrentPath().toString());
        Assert.assertFalse(c.hasChildren(c.getCurrentNode()));
    }

    @Test
    public void hasChildren_ObjectNode() throws IOException {
        String jsonString = "{\"x\":{\"text\":\"value\"}}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof ObjectNode);
        Assert.assertEquals("x", c.getCurrentPath().toString());
        Assert.assertTrue(c.hasChildren(c.getCurrentNode()));
    }

    @Test
    public void hasChildren_ArrayNode() throws IOException {
        String jsonString = "{\"x\":[1,2,3,4]}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof ArrayNode);
        Assert.assertEquals("x", c.getCurrentPath().toString());
        Assert.assertTrue(c.hasChildren(c.getCurrentNode()));
    }

    @Test
    public void parent_TextNode() throws IOException {
        String jsonString = "{\"parent\":{\"text\":\"value\"}}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof TextNode);
        Assert.assertEquals("parent.text", c.getCurrentPath().toString());
        Assert.assertFalse(c.hasChildren(c.getCurrentNode()));
        Assert.assertTrue(c.parent());
        Assert.assertSame(node, c.getCurrentNode());
    }

    @Test
    public void parent_ObjectNode() throws IOException {
        String jsonString = "{\"parent\":{\"x\":{\"text\":\"value\"}}}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof ObjectNode);
        Assert.assertEquals("parent.x", c.getCurrentPath().toString());
        Assert.assertTrue(c.hasChildren(c.getCurrentNode()));
        Assert.assertTrue(c.parent());
        Assert.assertSame(node, c.getCurrentNode());
    }

    @Test
    public void parent_ArrayNode() throws IOException {
        String jsonString = "{\"parent\":{\"x\":[1,2,3,4]}}";
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);
        Assert.assertTrue(c.firstChild());
        Assert.assertTrue(c.firstChild());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertTrue(c.getCurrentNode() instanceof ArrayNode);
        Assert.assertEquals("parent.x", c.getCurrentPath().toString());
        Assert.assertTrue(c.hasChildren(c.getCurrentNode()));
        Assert.assertTrue(c.parent());
        Assert.assertSame(node, c.getCurrentNode());
    }

    @Test
    public void next() throws IOException {
        String jsonString = "{\"text\":\"value\",\"parent\":{\"array\":[1,2,3,4],\"object\":{\"foo\":\"bar\"}}}";
        MutablePath p = new MutablePath();
        JsonNode node = JsonUtils.json(jsonString);
        JsonNodeCursor c = new JsonNodeCursor(new Path(""), node);

        // simply going to walk through the document with next() and verify after each call manually
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals("text", c.getCurrentPath().toString());
        Assert.assertEquals("value", c.getCurrentNode().asText());

        p.push("parent");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof ObjectNode);

        p.push("array");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof ArrayNode);

        p.push("0");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof NumericNode);
        Assert.assertEquals(1, c.getCurrentNode().asInt());

        p.setLast("1");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof NumericNode);
        Assert.assertEquals(2, c.getCurrentNode().asInt());

        p.setLast("2");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof NumericNode);
        Assert.assertEquals(3, c.getCurrentNode().asInt());

        p.setLast("3");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof NumericNode);
        Assert.assertEquals(4, c.getCurrentNode().asInt());

        p.pop();
        p.pop();
        p.push("object");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof ObjectNode);

        p.push("foo");
        Assert.assertTrue(c.next());
        Assert.assertNotNull(c.getCurrentNode());
        Assert.assertEquals(p.toString(), c.getCurrentPath().toString());
        Assert.assertTrue(c.getCurrentNode() instanceof TextNode);
        Assert.assertEquals("bar", c.getCurrentNode().asText());

        Assert.assertFalse(c.next());
    }
}
