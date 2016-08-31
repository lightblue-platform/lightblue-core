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
package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.redhat.lightblue.util.JsonUtils;

public class UpdaterTest extends AbstractJsonNodeTest {
    EntityMetadata md;

    @Before
    public void setup() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void setSimpleFieldTest() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field1' : 'set1', 'field2':'set2', 'field5': 0, 'field6.nf1':'set6' } }, {'$add' : { 'field3':1 } } ] ");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals("set1", jsonDoc.get(new Path("field1")).asText());
        Assert.assertEquals("set2", jsonDoc.get(new Path("field2")).asText());
        Assert.assertEquals(4, jsonDoc.get(new Path("field3")).asInt());
        Assert.assertFalse(jsonDoc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6", jsonDoc.get(new Path("field6.nf1")).asText());
    }

    @Test
    public void setArrayFieldTest() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$set' : { 'field6.nf5.0':'50', 'field6.nf6.1':'blah', 'field7.0.elemf1':'test'}} ");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(50, jsonDoc.get(new Path("field6.nf5.0")).intValue());
        Assert.assertEquals("blah", jsonDoc.get(new Path("field6.nf6.1")).asText());
        Assert.assertEquals("test", jsonDoc.get(new Path("field7.0.elemf1")).asText());
    }

    @Test
    public void refSet() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$set' : { 'field6.nf5.0': { '$valueof' : 'field3' }, 'field7.0' : {}}}");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(jsonDoc.get(new Path("field3")).intValue(), jsonDoc.get(new Path("field6.nf5.0")).intValue());
        JsonNode node = jsonDoc.get(new Path("field7.0"));
        Assert.assertNotNull(node);
        Assert.assertEquals(0, node.size());
        Assert.assertTrue(node instanceof ObjectNode);
    }

    @Test
    public void unset() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$unset' : [ 'field1', 'field6.nf2', 'field6.nf6.1','field7.1'] }");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertNull(jsonDoc.get(new Path("field1")));
        Assert.assertNull(jsonDoc.get(new Path("field6.nf2")));
        Assert.assertEquals("three", jsonDoc.get(new Path("field6.nf6.1")).asText());
        Assert.assertEquals(3, jsonDoc.get(new Path("field6.nf6#")).asInt());
        Assert.assertEquals(3, jsonDoc.get(new Path("field6.nf6")).size());
        Assert.assertEquals("elvalue2_1", jsonDoc.get(new Path("field7.1.elemf1")).asText());
        Assert.assertEquals(3, jsonDoc.get(new Path("field7#")).asInt());
        Assert.assertEquals(3, jsonDoc.get(new Path("field7")).size());
    }

    @Test
    public void array_append() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$append' : { 'field6.nf6' : [ 'five','six',{'$valueof':'field2' }] } }");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals("one", jsonDoc.get(new Path("field6.nf6.0")).asText());
        Assert.assertEquals("two", jsonDoc.get(new Path("field6.nf6.1")).asText());
        Assert.assertEquals("three", jsonDoc.get(new Path("field6.nf6.2")).asText());
        Assert.assertEquals("four", jsonDoc.get(new Path("field6.nf6.3")).asText());
        Assert.assertEquals("five", jsonDoc.get(new Path("field6.nf6.4")).asText());
        Assert.assertEquals("six", jsonDoc.get(new Path("field6.nf6.5")).asText());
        Assert.assertEquals("value2", jsonDoc.get(new Path("field6.nf6.6")).asText());
        Assert.assertNull(jsonDoc.get(new Path("field6.ng6.7")));
        Assert.assertEquals(7, jsonDoc.get(new Path("field6.nf6#")).asInt());
        Assert.assertEquals(7, jsonDoc.get(new Path("field6.nf6")).size());
    }

    @Test
    public void null_array_append() throws Exception {
        // Set field7 to null
        jsonDoc.modify(new Path("field7"), null, true);
        Assert.assertNull(jsonDoc.get(new Path("field7")));

        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$append' : { 'field7' : {} } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        updater.update(jsonDoc, md.getFieldTreeRoot(), new Path());

        Assert.assertEquals(1, jsonDoc.get(new Path("field7")).size());
    }

    @Test
    public void null_nested_array_append() throws Exception {
        // Set field11.0.arr. to null
        jsonDoc.modify(new Path("field11.0.arr"), null, true);
        Assert.assertNull(jsonDoc.get(new Path("field11.0.arr")));

        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$append' : { 'field11.0.arr' : { 'id':1, 'x1':'x1_1'} } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        updater.update(jsonDoc, md.getFieldTreeRoot(), new Path());

        Assert.assertEquals(1, jsonDoc.get(new Path("field11.0.arr")).size());
    }

    @Test
    public void null_array_set() throws Exception {
        // Set field7 to null
        jsonDoc.modify(new Path("field7"), null, true);
        Assert.assertNull(jsonDoc.get(new Path("field7")));

        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$set' : { 'field7' : [] } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        updater.update(jsonDoc, md.getFieldTreeRoot(), new Path());

        Assert.assertEquals(0, jsonDoc.get(new Path("field7")).size());
    }

    @Test
    public void array_insert() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$insert' : { 'field6.nf6.2' : [ 'five','six',{'$valueof':'field2' }] } }");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals("one", jsonDoc.get(new Path("field6.nf6.0")).asText());
        Assert.assertEquals("two", jsonDoc.get(new Path("field6.nf6.1")).asText());
        Assert.assertEquals("five", jsonDoc.get(new Path("field6.nf6.2")).asText());
        Assert.assertEquals("six", jsonDoc.get(new Path("field6.nf6.3")).asText());
        Assert.assertEquals("value2", jsonDoc.get(new Path("field6.nf6.4")).asText());
        Assert.assertEquals("three", jsonDoc.get(new Path("field6.nf6.5")).asText());
        Assert.assertEquals("four", jsonDoc.get(new Path("field6.nf6.6")).asText());
        Assert.assertNull(jsonDoc.get(new Path("field6.ng6.7")));
        Assert.assertEquals(7, jsonDoc.get(new Path("field6.nf6#")).asInt());
        Assert.assertEquals(7, jsonDoc.get(new Path("field6.nf6")).size());
    }
    
    @Test
    public void array_foreach_set_this() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field7' : '$all' , '$update' : {'$set': { '$this': {} } } } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        for (JsonNode node : jsonDoc.get(new Path("field7"))) {
            Assert.assertEquals(JsonNodeFactory.instance.objectNode(), node);
            Assert.assertEquals(0, node.size());
            Assert.assertTrue(!node.fields().hasNext());
        }
    }
    
    @Test
    public void array_foreach_set_partial_this() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson(
                "{ '$foreach' : { 'field7' : '$all' , '$update' : {'$set': { '$this': {'elemf1': 'NA', 'elemf2': 'NA', 'elemf3': -1 } }, 'fields': [ { 'field': 'elemf2' }, { 'field': 'elemf3' } ] } } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        int i = 0;
        for (JsonNode node : jsonDoc.get(new Path("field7"))) {
            Assert.assertEquals("elvalue" + i + "_1", node.get("elemf1").asText());
            Assert.assertEquals("NA", node.get("elemf2").asText());
            Assert.assertEquals(-1, node.get("elemf3").asInt());
            Assert.assertEquals(3, node.size());
            i++;
        }
    }
    
    @Test
    public void array_foreach_set_partial_this_no_fields() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson(
                "{ '$foreach' : { 'field7' : '$all' , '$update' : {'$set': { '$this': {'elemf1': 'NA', 'elemf2': 'NA', 'elemf3': -1 } }, 'fields': [ ] } } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        int i = 0;
        for (JsonNode node : jsonDoc.get(new Path("field7"))) {
            Assert.assertEquals("elvalue" + i + "_1", node.get("elemf1").asText());
            Assert.assertEquals("elvalue" + i + "_2", node.get("elemf2").asText());
            Assert.assertEquals(3 + i, node.get("elemf3").asInt());
            Assert.assertEquals(3, node.size());
            i++;
        }
    }
    
    @Test
    public void array_foreach_set_partial_this_invalid_fields() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson(
                "{ '$foreach' : { 'field7' : '$all' , '$update' : {'$set': { '$this': {'elemf1': 'NA', 'elemf2': 'NA', 'elemf3': -1 } }, 'fields': [ { 'field': 'elemf4' } ] } } }");
        // should do nothing
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        int i = 0;
        for (JsonNode node : jsonDoc.get(new Path("field7"))) {
            Assert.assertEquals("elvalue" + i + "_1", node.get("elemf1").asText());
            Assert.assertEquals("elvalue" + i + "_2", node.get("elemf2").asText());
            Assert.assertEquals(3 + i, node.get("elemf3").asInt());
            Assert.assertEquals(3, node.size());
            i++;
        }
    }

    @Test
    public void array_foreach_removeall() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field7' : '$all', '$update' : '$remove' } }");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(0, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals(0, jsonDoc.get(new Path("field7#")).asInt());
    }

    @Test
    public void array_foreach_removeone() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : '$remove' } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(3, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals("elvalue1_1", jsonDoc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals(3, jsonDoc.get(new Path("field7#")).asInt());
        Assert.assertEquals(3, jsonDoc.get(new Path("field7")).size());
    }

    @Test
    public void array_foreach_removethis() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field6.nf5' : { 'field':'$this','op':'=','rvalue':15} , '$update' : '$remove' } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertEquals(4, jsonDoc.get(new Path("field6.nf5")).size());
        Assert.assertEquals(15, jsonDoc.get(new Path("field6.nf5.2")).asInt());
        updater.update(jsonDoc, md.getFieldTreeRoot(), new Path());
        Assert.assertEquals(3, jsonDoc.get(new Path("field6.nf5")).size());
        Assert.assertEquals(20, jsonDoc.get(new Path("field6.nf5.2")).asInt());
    }

    @Test
    public void array_foreach_modone() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : {'$set': { 'elemf1':'test'}} } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals("test", jsonDoc.get(new Path("field7.0.elemf1")).asText());
    }

    @Test
    public void array_foreach_nullq() throws Exception {
        jsonDoc.modify(new Path("field7.0.elemf1"), null, false);
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field7' : { 'field':'elemf1','op':'=','rvalue':null} , '$update' : {'$set': { 'elemf1':'test'}} } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals("test", jsonDoc.get(new Path("field7.0.elemf1")).asText());
    }

    @Test
    public void array_foreach_append() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./termsdata.json");
        md = EvalTestContext.getMd("./termsmd.json");
        UpdateExpression expr = EvalTestContext.
                updateExpressionFromJson("{ '$foreach' : { 'termsVerbiage' : { 'field':'uid','op':'=','rvalue':1} ,"
                        + "'$update' : [ "
                        + "{ '$insert': { 'termsVerbiageTranslation.0': {}}},"
                        + "{ '$set': {'termsVerbiageTranslation.0.localeCode':'lg','termsVerbiageTranslation.0.localeText':'Lang' } }"
                        + " ] }}");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        System.out.println("before:" + JsonUtils.prettyPrint(jsonDoc.getRoot()));
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        System.out.println("After:" + JsonUtils.prettyPrint(jsonDoc.getRoot()));

        Assert.assertEquals(4, jsonDoc.get(new Path("termsVerbiage.0.termsVerbiageTranslation")).size());
        Assert.assertEquals("lg", jsonDoc.get(new Path("termsVerbiage.0.termsVerbiageTranslation.0.localeCode")).asText());
    }

    @Test
    public void parent_parent_update() throws Exception {
        UpdateExpression expr = EvalTestContext.
                updateExpressionFromJson("{ '$foreach' : { 'arr13.*.level2.*.level3' : { 'field':'$parent.$parent.id','op':'=','rvalue':'1'},"
                        + "'$update' : {'$set':{'fld':'x' } }}}");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        System.out.println("before:" + JsonUtils.prettyPrint(jsonDoc.getRoot()));
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        System.out.println("After:" + JsonUtils.prettyPrint(jsonDoc.getRoot()));

        Assert.assertEquals("x", jsonDoc.get(new Path("arr13.0.level2.0.level3.0.fld")).asText());
        Assert.assertEquals("value", jsonDoc.get(new Path("arr13.1.level2.0.level3.0.fld")).asText());
    }

}
