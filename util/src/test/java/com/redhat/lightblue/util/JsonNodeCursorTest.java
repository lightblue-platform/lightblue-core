/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import java.io.IOException;
import java.util.Iterator;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class JsonNodeCursorTest extends AbstractTreeCursorTest<JsonNode> {
    private JsonNode rootNode;

    @Before
    @Override
    public void setup() {
        path = new Path("object.text");
        super.setup();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
        rootNode = null;
    }

    @Override
    public AbstractTreeCursor<JsonNode> createCursor(Path p) {
        try {
            rootNode = AbstractJsonNodeTest.loadJsonNode("JsonNodeCursorTest-general.json");
            return new JsonNodeCursor(path, rootNode);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public JsonNode getRootNode() {
        return rootNode;
    }

    @Override
    public Iterator<JsonNode> getChildren(JsonNode node) {
        if (node instanceof ArrayNode) {
            return ((ArrayNode) node).elements();
        } else if (node instanceof ObjectNode) {
            return ((ObjectNode) node).elements();
        }
        return null;
    }

    @Test
    public void hasChildren_TextNode() throws IOException {
        String jsonString = "{\"text\":\"value\"}";
        JsonNode node = AbstractJsonNodeTest.fromString(jsonString);
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
        JsonNode node = AbstractJsonNodeTest.fromString(jsonString);
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
        JsonNode node = AbstractJsonNodeTest.fromString(jsonString);
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
        JsonNode node = AbstractJsonNodeTest.fromString(jsonString);
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
        JsonNode node = AbstractJsonNodeTest.fromString(jsonString);
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
        JsonNode node = AbstractJsonNodeTest.fromString(jsonString);
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
}
