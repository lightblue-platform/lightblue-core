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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.constraints.RequiredConstraint;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.metadata.types.TimestampType;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TimestampTest {

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    private EntityMetadata getMD1() {
        EntityMetadata entityMetadata = new EntityMetadata("test");

        entityMetadata.getFields().addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        entityMetadata.getFields().addNew(new SimpleField("simpleTimestamp", TimestampType.TYPE));
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.getFields().addNew(objectField1);
        objectField1.getFields().addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        objectField1.getFields().addNew(new SimpleField("nestedTimestamp", TimestampType.TYPE));
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.getFields().addNew(objectField2);
        objectField2.getFields().addNew(new SimpleField("doubleNestedTimestamp", TimestampType.TYPE));
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        objectField2.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjTimestamp", TimestampType.TYPE));
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);

        return entityMetadata;
    }

    private EntityMetadata getMDWithReq() {
        EntityMetadata entityMetadata = new EntityMetadata("test");
        List<FieldConstraint> list = new ArrayList<>();
        list.add(new RequiredConstraint());

        entityMetadata.getFields().addNew(new SimpleField("simpleInteger", IntegerType.TYPE));
        Field f = new SimpleField("simpleTimestamp", TimestampType.TYPE);
        f.setConstraints(list);
        entityMetadata.getFields().addNew(f);
        ObjectField objectField1 = new ObjectField("obj1");
        entityMetadata.getFields().addNew(objectField1);
        objectField1.getFields().addNew(new SimpleField("nestedSimpleInteger", IntegerType.TYPE));
        f = new SimpleField("nestedTimestamp", TimestampType.TYPE);
        f.setConstraints(list);
        objectField1.getFields().addNew(f);
        ObjectField objectField2 = new ObjectField("nested");
        objectField1.getFields().addNew(objectField2);
        f = new SimpleField("doubleNestedTimestamp", TimestampType.TYPE);
        f.setConstraints(list);
        objectField2.getFields().addNew(f);
        ArrayField arrayField1 = new ArrayField("simpleArr", new SimpleArrayElement(StringType.TYPE));
        objectField2.getFields().addNew(arrayField1);

        ObjectArrayElement objectArrayElement = new ObjectArrayElement();
        objectArrayElement.getFields().addNew(new SimpleField("nestedArrObjString1", StringType.TYPE));
        f = new SimpleField("nestedArrObjTimestamp", TimestampType.TYPE);
        f.setConstraints(list);
        objectArrayElement.getFields().addNew(f);
        ArrayField arrayField2 = new ArrayField("objArr", objectArrayElement);
        objectField2.getFields().addNew(arrayField2);

        return entityMetadata;
    }

    @Test
    public void nonReq() throws Exception {
        EntityMetadata md = getMD1();
        ObjectNode node = nodeFactory.objectNode();
        node.put("simpleInteger", 10);
        JsonDoc doc = new JsonDoc(node);
        TimestampFields.initializeTimestampFields(nodeFactory, md, doc);
        Assert.assertEquals(10, doc.get(new Path("simpleInteger")).asInt());
        Assert.assertNull(doc.get(new Path("simpleTimestamp")));
        Assert.assertNull(doc.get(new Path("obj1")));
        Assert.assertNull(doc.get(new Path("obj1.nested.objArr")));
    }



    @Test
    public void userTest() throws Exception {
        JsonNode node = JsonUtils.json(getClass().getResourceAsStream("/usermd.json"));
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new DataStoreParser<JsonNode>() {
            public String getDefaultName() {
                return "mongo";
            }

            public DataStore parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
                return new DataStore() {
                    public String getBackend() {
                        return "mongo";
                    }
                };
            }

            public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, DataStore object) {
            }
        });
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);

        JsonNode userNode = JsonUtils.json(getClass().getResourceAsStream("/userdata.json"));
        JsonDoc doc = new JsonDoc(userNode);
        TimestampFields.initializeTimestampFields(nodeFactory, md, doc);
        System.out.println(doc);


        Assert.assertTrue(doc.get(new Path("reqtimestamp")).asText().length()>0);
        Assert.assertTrue(doc.get(new Path("nonreqtimestamp")).asText().length()>0);

    }
}
