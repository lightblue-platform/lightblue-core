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
package com.redhat.lightblue.metadata.parser;

import static com.redhat.lightblue.util.JsonUtils.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Enum;
import com.redhat.lightblue.metadata.EnumValue;
import com.redhat.lightblue.metadata.Enums;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.metadata.ValueGenerator;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class JSONMetadataParserTest extends AbstractJsonSchemaTest {

    JsonNodeFactory factory = new JsonNodeFactory(true);

    private JSONMetadataParser parser;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("empty", new DataStoreParser<JsonNode>() {

            @Override
            public DataStore parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
                if (!"empty".equals(name)) {
                    throw Error.get(MetadataConstants.ERR_ILL_FORMED_METADATA, name);
                }

                DataStore ds = new DataStore() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getBackend() {
                        return "empty";
                    }
                };
                return ds;
            }

            @Override
            public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, DataStore ds) {
                // nothing to do
            }

            @Override
            public String getDefaultName() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        parser = new JSONMetadataParser(extensions, new DefaultTypes(), factory);
    }

    @After
    public void tearDown() {
        parser = null;
    }

    private void testResource(String resource) throws IOException, JSONException, ProcessingException {
        // verify json is schema compliant
        runValidJsonTest("json-schema/metadata/metadata.json", resource);

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
    public void fullObjectEverythingNoHooks() throws IOException, ParseException, JSONException, ProcessingException {
        testResource("JSONMetadataParserTest-object-everything-no-hooks.json");
    }

    @Test
    public void testBinary() throws IOException, ParseException, JSONException, ProcessingException {
        testResource("JSONMetadataParserTest-object-binary.json");
    }

    @Test
    public void testExtraFieldNoHooks() throws IOException, ParseException, JSONException, ProcessingException {
        JsonNode object = loadJsonNode("JSONMetadataParserTest-object-everything-no-hooks-extra-field.json");
        EntityMetadata em = parser.parseEntityMetadata(object);
        Assert.assertNotNull(em);
        Assert.assertNotNull(em.getEntitySchema());
        Map<String, Object> properties = em.getEntitySchema().getProperties();
        Assert.assertNotNull(properties);
        Assert.assertFalse("Empty 'properties' (it should contain rdbms)", properties.isEmpty());
        Assert.assertTrue("More than a single property (it should contain just rdbms)", properties.size() == 1);
        Object rdbms = properties.get("rdbms");
        Assert.assertNotNull(rdbms);
        Assert.assertEquals("42",((Map<String,Object>)rdbms).get("answer"));
        JsonNode c = parser.convert(em);
        Assert.assertEquals(object.get("schema").get("rdbms"), c.get("schema").get("rdbms"));
    }

    //    @Test hooks not implemented yet
    //    public void fullObjectEverything() throws IOException, ParseException, JSONException {
    //        testResource("JSONMetadataParserTest-object-everything.json");
    //    }
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

        Assert.assertTrue("expected instanceof Number", object instanceof Number);

        Assert.assertEquals(value.intValue(), ((Number) object).intValue());
    }

    @Test
    public void getValuePropertyObject() {
        String name = "name";
        String value = "value";
        ObjectNode parent = new ObjectNode(factory);
        parent.put(name, value);

        Object object = parser.getValueProperty(parent, name);

        Assert.assertTrue("expected instanceof String", object instanceof String);

        Assert.assertEquals(value, object);
    }

    @Test
    public void getStringList() {
        String name = "x";
        int count = 3;
        ObjectNode parent = new ObjectNode(factory);
        ArrayNode array = factory.arrayNode();
        parent.set(name, array);

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
        parent.set(name, array);

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
        List<String> childNames = new ArrayList<>();
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

        for (String childName : childNames) {
            s.remove(childName);
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

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.booleanValue(), x.booleanValue());
    }

    @Test
    public void putValueBigDecimal() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        BigDecimal value = new BigDecimal("213.55");

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value, x.decimalValue());
    }

    @Test
    public void putValueBigInteger() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        BigInteger value = new BigInteger("123444");

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value, x.bigIntegerValue());
    }

    @Test
    public void putValueDouble() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Double value = new Double("12928.222");

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.doubleValue(), x.doubleValue(), 0.001);
    }

    @Test
    public void putValueFloat() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Float value = new Float("123.222");

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.floatValue(), x.floatValue(), 0.001);
    }

    @Test
    public void putValueInteger() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Integer value = 123444;

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.intValue(), x.intValue());
    }

    @Test
    public void putValueLong() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Long value = 1272722l;

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.longValue(), x.longValue());
    }

    @Test
    public void putValueSohrt() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        Short value = 123;

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

        Assert.assertNotNull(x);
        Assert.assertEquals(value.shortValue(), x.shortValue());
    }

    @Test
    public void putValueString() {
        ObjectNode parent = new ObjectNode(factory);
        String name = "foo";
        String value = "bar";

        parser.putValue(parent, name, value);

        JsonNode x = parent.get(name);

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
        String value = "bar";
        ArrayNode array = new ArrayNode(factory);

        Assert.assertEquals(0, array.size());

        parser.addStringToArray(array, value);

        Assert.assertEquals(1, array.size());
        Assert.assertEquals(value, array.get(0).textValue());
    }

    @Test
    public void addObjectToArray() {
        JsonNode value = new TextNode("asdf");
        ArrayNode array = new ArrayNode(factory);

        Assert.assertEquals(0, array.size());

        parser.addObjectToArray(array, value);

        Assert.assertEquals(1, array.size());
        Assert.assertEquals(value, array.get(0));
    }

    @Test
    public void testConvertEnums() throws IOException, JSONException{
        String enumName = "FakeEnum";
        String enumValue1 = "FakeEnumValue1";
        String enumValue2 = "FakeEnumValue2";

        Enum e = new Enum(enumName);
        e.setValues(new HashSet<>(Arrays.asList(
                new EnumValue(enumValue1, null),
                new EnumValue(enumValue2, null))));

        Enums enums = new Enums();
        enums.addEnum(e);

        JsonNode enumsNode = json("{}");
        parser.convertEnums(enumsNode, enums);

        String jsonString = enumsNode.toString();
        JSONAssert.assertEquals("{enums:[{name:\"" + enumName + "\",values:[\"" + enumValue1 + "\",\"" + enumValue2 + "\"]}]}", jsonString, false);

        //Should just be string elements, not a complex objects.
        Assert.assertFalse(jsonString.matches(".*\"annotatedValues\":\\[.*"));
        Assert.assertFalse(jsonString.contains("\"name\":\"" + enumValue1 + "\""));
        Assert.assertFalse(jsonString.contains("\"name\":\"" + enumValue2 + "\""));
    }

    @Test
    public void testConvertEnums_WithDescription() throws IOException, JSONException{
        String enumName = "FakeEnum";
        String enumValue1 = "FakeEnumValue1";
        String enumDescription1 = "this is a fake description of enum value 1";
        String enumValue2 = "FakeEnumValue2";
        String enumDescription2 = "this is a fake description of enum value 2";

        Enum e = new Enum(enumName);
        e.setValues(new HashSet<>(Arrays.asList(
                new EnumValue(enumValue1, enumDescription1),
                new EnumValue(enumValue2, enumDescription2))));

        Enums enums = new Enums();
        enums.addEnum(e);

        JsonNode enumsNode = json("{}");
        parser.convertEnums(enumsNode, enums);

        String jsonString = enumsNode.toString();
        JSONAssert.assertEquals("{enums:[{name:\"" + enumName + "\","
                + "values:[\"" + enumValue1 + "\",\"" + enumValue2 + "\"],"
                + "annotatedValues:[{name:\"" + enumValue1 + "\",description:\"" + enumDescription1 + "\"},{name:\"" + enumValue2 + "\",description:\"" + enumDescription2 + "\"}]}]}",
                jsonString, false);
    }

    @Test
    public void testParseEnum() throws IOException{
        String enumName = "FakeEnum";
        String enumValue1 = "FakeEnumValue1";
        String enumValue2 = "FakeEnumValue2";

        JsonNode enumsNode = json("{\"name\":\"" + enumName + "\", "
                + "\"values\": [\"" + enumValue1 + "\",\"" + enumValue2 + "\"]}");

        Enum e = parser.parseEnum(enumsNode);

        Assert.assertEquals(enumName, e.getName());
        Set<EnumValue> values = e.getEnumValues();
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(values.contains(new EnumValue(enumValue1, null)));
        Assert.assertTrue(values.contains(new EnumValue(enumValue2, null)));
    }

    @Test
    public void testParseEnum_WithDescriptions() throws IOException{
        String enumName = "FakeEnum";
        String enumValue1 = "FakeEnumValue1";
        String enumDescription1 = "this is a fake description of enum value 1";
        String enumValue2 = "FakeEnumValue2";
        String enumDescription2 = "this is a fake description of enum value 2";

        JsonNode enumsNode = json("{\"name\":\"" + enumName + "\", " +
                "\"annotatedValues\": [" +
                "{\"name\":\"" + enumValue1 + "\", \"description\":\"" + enumDescription1 + "\"}," +
                "{\"name\":\"" + enumValue2 + "\", \"description\":\"" + enumDescription2 + "\"}" +
                "]}");

        Enum e = parser.parseEnum(enumsNode);

        Assert.assertEquals(enumName, e.getName());

        Set<EnumValue> values = e.getEnumValues();
        Assert.assertEquals(2, values.size());
        Assert.assertTrue(e.getEnumValues().contains(new EnumValue(enumValue1, enumDescription1)));
        Assert.assertTrue(e.getEnumValues().contains(new EnumValue(enumValue2, enumDescription2)));
    }

    @Test
    public void testParseFields_WithDescription() throws IOException {
        JsonNode fieldsNode = json("{\"field\":{\"type\":\"string\",\"description\":\"foo\"}}");
        Fields fields = new Fields(null);
        parser.parseFields(fields, fieldsNode);
        Assert.assertNotNull(fields.getField("field"));
        Assert.assertEquals("description not parsed", "foo", fields.getField("field").getDescription());
    }
    
    @Test
    public void testParseEnum_MissingName() throws IOException{
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"parseEnum\",\"errorCode\":\"metadata:ParseMissingElement\",\"msg\":\"name\"}");
        parser.parseEnum(json("{}"));
    }

    @Test
    public void testParseEnum_MissingValues() throws IOException{
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"parseEnum\",\"errorCode\":\"metadata:ParseMissingElement\",\"msg\":\"values\"}");
        parser.parseEnum(json("{\"name\":\"FakeEnumName\"}"));
    }

    @Test
    public void testParseEnum_EmptyValues() throws IOException{
        expectedEx.expect(com.redhat.lightblue.util.Error.class);
        expectedEx.expectMessage("{\"objectType\":\"error\",\"context\":\"parseEnum\",\"errorCode\":\"metadata:ParseMissingElement\",\"msg\":\"values\"}");
        parser.parseEnum(json("{\"name\":\"FakeEnumName\", \"values\":[]}"));
    }

    @Test
    public void testParseConvertValueGenerator() throws IOException {
        JsonNode object = loadJsonNode("JSONMetadataParserTest-valuegenerator.json");
        EntitySchema em = parser.parseEntitySchema(object);
        SimpleField field=(SimpleField)em.resolve(new Path("name"));
        Assert.assertNotNull(field);
        ValueGenerator vg=field.getValueGenerator();
        Assert.assertNotNull(vg);
        Assert.assertEquals(ValueGenerator.ValueGeneratorType.IntSequence,vg.getValueGeneratorType());
        Assert.assertEquals("seq",vg.getProperties().get("name"));
        Assert.assertEquals("1000",vg.getProperties().get("initialValue").toString());
        ObjectNode obj=JsonNodeFactory.instance.objectNode();
        parser.convertValueGenerator(vg,obj);
        obj=(ObjectNode)obj.get("valueGenerator");
        System.out.println(obj);
        Assert.assertEquals(ValueGenerator.ValueGeneratorType.IntSequence.toString(),obj.get("type").asText());
        ObjectNode props=(ObjectNode)obj.get("configuration");
        Assert.assertEquals("seq",props.get("name").asText());
        Assert.assertEquals("1000",props.get("initialValue").asText());
    }

    @Test
    public void testProperties() throws IOException {
        JsonNode object = loadJsonNode("JSONMetadataParserTest-properties.json");
        EntitySchema em = parser.parseEntitySchema(object);
        List<Integer> list=(List<Integer>)em.getAccess().getProperties().get("accessProperty");
        Assert.assertEquals(list.size(),5);
        Assert.assertTrue(list.contains(new Integer(1)));
        Assert.assertTrue(list.contains(new Integer(2)));
        Assert.assertTrue(list.contains(new Integer(3)));
        Assert.assertTrue(list.contains(new Integer(4)));
        Assert.assertTrue(list.contains(new Integer(5)));

        SimpleField field=(SimpleField)em.resolve(new Path("name"));
        Assert.assertEquals("y",(String)((Map<String,Object>)field.getProperties().get("nameProperty")).get("x"));

        field=(SimpleField)em.resolve(new Path("customerType"));
        Assert.assertEquals(new Integer(1),(Integer)((Map<String,Object>)field.getProperties()).get("customerTypeProperty"));

        Map<String,Object> scp=(Map<String,Object>)em.getProperties().get("schemaProperty");
        Map<String,Object> scpa=(Map<String,Object>)scp.get("a");
        Assert.assertEquals("c",(String)scpa.get("b"));
    }
}
