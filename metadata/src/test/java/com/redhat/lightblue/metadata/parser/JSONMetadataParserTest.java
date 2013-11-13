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
import com.fasterxml.jackson.databind.node.TextNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

public class JSONMetadataParserTest extends AbstractJsonNodeTest {

    JsonNodeFactory factory = new JsonNodeFactory(true);

    private JSONMetadataParser parser;

    @Before
    public void setup() {
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        parser = new JSONMetadataParser(extensions, new DefaultTypes(), factory);
    }

    @After
    public void tearDown() {
        parser = null;
    }

    @Test
    public void fullObjectEverything() throws IOException, ParseException, JSONException {
        String resource = "JSONMetadataParserTest-object-everything.json";
        JsonNode object = loadJsonNode(resource);

        // json to java
        EntityMetadata em = parser.parseEntityMetadata(object);
        
        // verify got something
        Assert.assertNotNull(em);
        
        // java back to json
        JsonNode converted = parser.convert(em);
        
        String original = loadResource(resource);
        String before = object.toString();
        String after = converted.toString();
        JSONAssert.assertEquals(original, before, false);
        JSONAssert.assertEquals(original, after, false);
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

    @Test
    public void putString() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        String value = "bar";

        parser.putString(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value, x.textValue());
    }

    @Test
    public void putObject() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Object value = new ObjectNode(factory);

        parser.putObject(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertTrue("expected instanceof ObjectNode", x instanceof ObjectNode);
        Assert.assertEquals(value, x);
    }

    @Test
    public void putValueBoolean() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Boolean value = Boolean.TRUE;

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.booleanValue(), x.booleanValue());
    }

    @Test
    public void putValueBigDecimal() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        BigDecimal value = new BigDecimal("213.55");

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value, x.decimalValue());
    }

    @Test
    public void putValueBigInteger() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        BigInteger value = new BigInteger("123444");

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value, x.bigIntegerValue());
    }

    @Test
    public void putValueDouble() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Double value = new Double("12928.222");

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.doubleValue(), x.doubleValue());
    }

    @Test
    public void putValueFloat() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Float value = new Float("123.222");

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.floatValue(), x.floatValue());
    }

    @Test
    public void putValueInteger() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Integer value = 123444;

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.intValue(), x.intValue());
    }

    @Test
    public void putValueLong() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Long value = 1272722l;

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.longValue(), x.longValue());
    }

    @Test
    public void putValueSohrt() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Short value = 123;

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.shortValue(), x.shortValue());
    }

    @Test
    public void putValueString() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        String value = "bar";

        parser.putValue(parent, name, value);

        JsonNode x = (JsonNode) parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value, x.textValue());
    }

    @Test
    public void newArrayField() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Object array = parser.newArrayField(parent, name);

        Assert.assertNotNull(array);
        Assert.assertEquals(array, parent.get(name));
    }

    @Test
    public void addStringToArray() {
        String name = "foo";
        String value = "bar";
        ArrayNode array = new ArrayNode(factory);

        Assert.assertEquals(0, array.size());

        parser.addStringToArray(array, value);

        Assert.assertEquals(1, array.size());
        Assert.assertEquals(value, array.get(0).textValue());
    }

    @Test
    public void addObjectToArray() {
        String name = "foo";
        JsonNode value = new TextNode("asdf");
        ArrayNode array = new ArrayNode(factory);

        Assert.assertEquals(0, array.size());

        parser.addObjectToArray(array, value);

        Assert.assertEquals(1, array.size());
        Assert.assertEquals(value, array.get(0));
    }

}
