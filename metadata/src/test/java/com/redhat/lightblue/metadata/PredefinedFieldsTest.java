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
package com.redhat.lightblue.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

public class PredefinedFieldsTest {

    private EntityMetadata getMD1() {
        EntityMetadata entityMetadata = new EntityMetadata("test");

        entityMetadata.getFields().addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("simpleString", StringType.TYPE));
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.getFields().addNew(objectField1);
        objectField1.getFields().addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        objectField1.getFields().addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.getFields().addNew(objectField2);
        objectField2.getFields().addNew(new SimpleField("doubleNestedString", StringType.TYPE));
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        //arrayField1.addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        entityMetadata.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString2", StringType.TYPE));
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);

        return entityMetadata;
    }

    private EntityMetadata getMD2() {
        EntityMetadata entityMetadata = new EntityMetadata("test");

        entityMetadata.getFields().addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("simpleString", StringType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("objectType", StringType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("_id", IntegerType.TYPE));
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.getFields().addNew(objectField1);
        objectField1.getFields().addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        objectField1.getFields().addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.getFields().addNew(objectField2);
        objectField2.getFields().addNew(new SimpleField("doubleNestedString", StringType.TYPE));
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        //arrayField1.addNew(new SimpleField("nestedSimpleString", StringType.TYPE));
        entityMetadata.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString2", StringType.TYPE));
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);
        objectField2.getFields().addNew(new SimpleField("objArr#", IntegerType.TYPE));

        return entityMetadata;
    }

    @Test
    public void testAddPredef() throws Exception {
        EntityMetadata md = getMD1();
        try {
            md.resolve(new Path("objectType"));
            Assert.fail();
        } catch (Exception e) {
        }
        try {
            md.resolve(new Path("simpleArr#"));
        } catch (Exception e) {
        }
        try {
            md.resolve(new Path("nested.objArr#"));
        } catch (Exception e) {
        }

        PredefinedFields.ensurePredefinedFields(md);
        Extensions<JsonNode> x = new Extensions<>();
        x.addDefaultExtensions();
        JSONMetadataParser p = new JSONMetadataParser(x, new DefaultTypes(), JsonNodeFactory.withExactBigDecimals(false));
        System.out.println(p.convert(md).toString());

        Field f = (SimpleField) md.resolve(new Path("objectType"));
        Assert.assertEquals(StringType.TYPE, f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(MetadataConstants.ROLE_ANYONE));

        f = (SimpleField) md.resolve(new Path("simpleArr#"));
        Assert.assertEquals(IntegerType.TYPE, f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(MetadataConstants.ROLE_ANYONE));

        f = (SimpleField) md.resolve(new Path("obj1.nested.objArr#"));
        Assert.assertEquals(IntegerType.TYPE, f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(MetadataConstants.ROLE_ANYONE));
    }

    @Test
    public void testModPredef() throws Exception {
        EntityMetadata md = getMD2();

        PredefinedFields.ensurePredefinedFields(md);

        Field f = (SimpleField) md.resolve(new Path("objectType"));
        Assert.assertEquals(StringType.TYPE, f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(MetadataConstants.ROLE_ANYONE));
        Assert.assertTrue(f.getAccess().getUpdate().getRoles().contains(MetadataConstants.ROLE_NOONE));

        f = (SimpleField) md.resolve(new Path("simpleArr#"));
        Assert.assertEquals(IntegerType.TYPE, f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(MetadataConstants.ROLE_ANYONE));

        f = (SimpleField) md.resolve(new Path("obj1.nested.objArr#"));
        Assert.assertEquals(IntegerType.TYPE, f.getType());
        Assert.assertTrue(f.getAccess().getFind().getRoles().contains(MetadataConstants.ROLE_ANYONE));
    }

    @Test
    public void testInvalidObjectType() throws Exception {
        EntityMetadata md = new EntityMetadata("test");
        md.getFields().addNew(new SimpleField("objectType", IntegerType.TYPE));
        try {
            PredefinedFields.ensurePredefinedFields(md);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testInvalidArr() throws Exception {
        EntityMetadata md = new EntityMetadata("test");
        md.getFields().addNew(new ArrayField("x", new SimpleArrayElement(IntegerType.TYPE)));
        md.getFields().addNew(new SimpleField("x#", StringType.TYPE));
        try {
            PredefinedFields.ensurePredefinedFields(md);
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void testDocModify() throws Exception {
        JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(false);
        ObjectNode node = factory.objectNode();
        JsonDoc doc = new JsonDoc(node);

        ObjectNode obj1;
        node.put("obj1", obj1 = factory.objectNode());
        node.put("simpleString", factory.textNode("str"));
        ArrayNode a1;
        obj1.put("simpleArr", a1 = factory.arrayNode());
        a1.add(factory.textNode("a"));
        ObjectNode o;
        obj1.put("nested", o = factory.objectNode());

        ArrayNode a2;
        o.put("objArr", a2 = factory.arrayNode());
        a2.add(factory.objectNode());

        PredefinedFields.updateArraySizes(getMD2(), factory, doc);
        System.out.println(doc);

        Assert.assertNotNull(doc.get(new Path("obj1.simpleArr")));
        Assert.assertNull(doc.get(new Path("obj1.simpleArr#")));
        Assert.assertEquals(1, doc.get(new Path("obj1.nested.objArr#")).intValue());
    }

    @Test
    public void testDocModify_ovrLength() throws Exception {
        JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(false);
        ObjectNode node = factory.objectNode();
        JsonDoc doc = new JsonDoc(node);

        ObjectNode obj1;
        node.put("obj1", obj1 = factory.objectNode());
        node.put("simpleString", factory.textNode("str"));
        ArrayNode a1;
        obj1.put("simpleArr", a1 = factory.arrayNode());
        a1.add(factory.textNode("a"));
        ObjectNode o;
        obj1.put("nested", o = factory.objectNode());

        ArrayNode a2;
        o.put("objArr", a2 = factory.arrayNode());
        o.put("objArr#", factory.numberNode(100));
        a2.add(factory.objectNode());

        PredefinedFields.updateArraySizes(getMD2(), factory, doc);
        System.out.println(doc);

        Assert.assertNotNull(doc.get(new Path("obj1.simpleArr")));
        Assert.assertNull(doc.get(new Path("obj1.simpleArr#")));
        Assert.assertEquals(1, doc.get(new Path("obj1.nested.objArr#")).intValue());
    }

    @Test
    public void testRemoveArray() throws Exception {
        JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(false);
        ObjectNode node = factory.objectNode();
        JsonDoc doc = new JsonDoc(node);

        ObjectNode obj1;
        node.put("obj1", obj1 = factory.objectNode());
        node.put("simpleString", factory.textNode("str"));
        ArrayNode a1;
        obj1.put("simpleArr", a1 = factory.arrayNode());
        a1.add(factory.textNode("a"));
        ObjectNode o;
        obj1.put("nested", o = factory.objectNode());

        o.put("objArr#", factory.numberNode(100));

        System.out.println(doc);
        PredefinedFields.updateArraySizes(getMD2(), factory, doc);

        Assert.assertEquals(0,doc.get(new Path("obj1.nested.objArr#")).intValue());
    }

    @Test
    public void testIsFieldObjectType_True(){
        assertTrue(PredefinedFields.isFieldObjectType(PredefinedFields.OBJECTTYPE_FIELD));
    }

    @Test
    public void testIsFieldObjectType_False(){
        assertFalse(PredefinedFields.isFieldObjectType("NOT " + PredefinedFields.OBJECTTYPE_FIELD));
    }

    @Test
    public void testIsFieldObjectType_NullValue(){
        assertFalse(PredefinedFields.isFieldObjectType(null));
    }

    @Test
    public void testDoesFieldNameMatchArrayCountPattern_True(){
        assertTrue(PredefinedFields.doesFieldNameMatchArrayCountPattern("somearray" + PredefinedFields.FIELD_ARRAY_COUNT_POSTFIX));
    }

    @Test
    public void testDoesFieldNameMatchArrayCountPattern_NullValue(){
        assertFalse(PredefinedFields.doesFieldNameMatchArrayCountPattern(null));
    }

    @Test
    public void testDoesFieldNameMatchArrayCountPattern_False(){
        assertFalse(PredefinedFields.doesFieldNameMatchArrayCountPattern("somearray"));
    }

    @Test
    public void testCreateArrayCountFieldName(){
        assertEquals("somearray#", PredefinedFields.createArrayCountFieldName("somearray"));
    }

    @Test
    public void testCreateArrayFieldNameFromCountField_NullValue(){
        assertNull(PredefinedFields.createArrayFieldNameFromCountField(null));
    }

    @Test
    public void testCreateArrayFieldNameFromCountField_NotArrayCountField(){
        String nonArrayCountFieldName = "notarraycount";
        assertEquals(nonArrayCountFieldName, PredefinedFields.createArrayFieldNameFromCountField(nonArrayCountFieldName));
    }

    @Test
    public void testCreateArrayFieldNameFromCountField_ArrayCountField(){
        String arrayFieldName = "arrayFieldName";
        String arrayCountFieldName = arrayFieldName + PredefinedFields.FIELD_ARRAY_COUNT_POSTFIX;
        assertEquals(arrayFieldName, PredefinedFields.createArrayFieldNameFromCountField(arrayCountFieldName));
    }

    @Test
    public void testIsFieldAnArrayCount_DoesNotMatchArrayCountPattern(){
        assertFalse(PredefinedFields.isFieldAnArrayCount("someField", null));
    }

    @Test
    public void testIsFieldAnArrayCount_DoesNotHaveMatchingArrayField(){
        assertFalse(PredefinedFields.isFieldAnArrayCount("someField" + PredefinedFields.FIELD_ARRAY_COUNT_POSTFIX, new Fields(null)));
    }

    @Test
    public void testIsFieldAnArrayCount_True(){
        String arrayFieldName = "arrayField";

        Fields fields = new Fields(null);
        fields.addNew(new ArrayField(arrayFieldName));

        assertTrue(PredefinedFields.isFieldAnArrayCount(arrayFieldName + PredefinedFields.FIELD_ARRAY_COUNT_POSTFIX, fields));
    }

    @Test
    public void testIsFieldAnArrayCount_Exists_ButNotArrayField(){
        String arrayFieldName = "arrayField";

        Fields fields = new Fields(null);
        fields.addNew(new SimpleField(arrayFieldName));

        assertFalse(PredefinedFields.isFieldAnArrayCount(arrayFieldName + PredefinedFields.FIELD_ARRAY_COUNT_POSTFIX, fields));
    }

}
