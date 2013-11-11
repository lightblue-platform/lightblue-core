package com.redhat.lightblue.metadata.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.JSONMetadataParser;

public class JSONMetadataParserTest {

    JsonNodeFactory factory = new JsonNodeFactory(true);

    private JSONMetadataParser parser;

    @Before
    public void setup() {
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();

        parser = new JSONMetadataParser(extensions, factory);
    }

    @After
    public void tearDown() {
        parser = null;
    }

    @Test
    public void getStringProperty() {
        String name = "name";
        String value = "value";
        ObjectNode parent = new ObjectNode(factory);
        parent.put(name, value);

        Assert.assertEquals(value, parser.getStringProperty(parent, name));
    }

    @Test
    public void getObjectProperty() {
        String name = "name";
        String value = "value";
        ObjectNode parent = new ObjectNode(factory);
        parent.put(name, value);
        parent.put(name + "1", value + "1");

        JsonNode object = parser.getObjectProperty(parent, name);

        Assert.assertNotNull("couldn't find node by name", object);
        Assert.assertEquals(value, object.textValue());
    }

    @Test
    public void getValuePropertyNumber() {
        String name = "name";
        Integer value = 1;
        ObjectNode parent = new ObjectNode(factory);
        parent.put(name, value);

        Object object = parser.getValueProperty(parent, name);

        Assert.assertTrue("expected instanceof JsonNode", object instanceof JsonNode);

        Assert.assertEquals(value.intValue(), ((JsonNode) object).intValue());
    }

    @Test
    public void getValuePropertyObject() {
        String name = "name";
        String value = "value";
        ObjectNode parent = new ObjectNode(factory);
        parent.put(name, value);

        Object object = parser.getValueProperty(parent, name);

        Assert.assertTrue("expected instanceof JsonNode", object instanceof String);

        Assert.assertEquals(value, (String) object);
    }

    @Test
    public void getStringList() {
        String name = "x";
        int count = 3;
        ObjectNode parent = new ObjectNode(factory);
        ArrayNode array = factory.arrayNode();
        parent.put(name, array);

        for (int i = 0; i < count; i++) {
            array.add(i);
        }

        List<String> l = parser.getStringList(parent, name);

        Assert.assertNotNull(l);
        Assert.assertEquals(count, l.size());

        for (int i = 0; i < count; i++) {
            Assert.assertEquals("value at index wrong: " + i, String.valueOf(i), l.get(i));
        }
    }

    @Test
    public void getObjectList() {
        String name = "x";
        int count = 3;
        ObjectNode parent = new ObjectNode(factory);
        ArrayNode array = factory.arrayNode();
        parent.put(name, array);

        for (int i = 0; i < count; i++) {
            array.add(i);
        }

        List<JsonNode> l = parser.getObjectList(parent, name);

        Assert.assertNotNull(l);
        Assert.assertEquals(count, l.size());

        for (int i = 0; i < count; i++) {
            Assert.assertEquals("value at index wrong: " + i, i, l.get(i).intValue());
        }
    }

    @Test
    public void getChildNames() {
        List<String> childNames = new ArrayList<String>();
        childNames.add("bob");
        childNames.add("jerry");
        childNames.add("hammer");
        childNames.add("suzy");

        ObjectNode parent = new ObjectNode(factory);

        for (String s : childNames) {
            parent.put(s, "value");
        }

        Set<String> s = parser.getChildNames(parent);
        
        Assert.assertNotNull(s);
        Assert.assertEquals(childNames.size(), s.size());
        
        for (int i = 0; i < childNames.size(); i++) {
            s.remove(childNames.get(i));
        }
        
        Assert.assertTrue("not all child names were removed..", s.isEmpty());
    }

    @Test
    public void newNode() {
        JsonNode x = parser.newNode();
        
        Assert.assertNotNull(x);
    }

    public void putString(JsonNode object, String name, String value) {
        // TODO Auto-generated method stub

    }

    public void putObject(JsonNode object, String name, Object value) {
        // TODO Auto-generated method stub

    }

    public void putValue(JsonNode object, String name, Object value) {
        // TODO Auto-generated method stub

    }

    public Object newArrayField(JsonNode object, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addStringToArray(Object array, String value) {
        // TODO Auto-generated method stub

    }

    public void addObjectToArray(Object array, Object value) {
        // TODO Auto-generated method stub

    }

}
