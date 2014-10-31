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
    public void array_foreach_modone() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : {'$set': { 'elemf1':'test'}} } }");
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
            updateExpressionFromJson("{ '$foreach' : { 'termsVerbiage' : { 'field':'uid','op':'=','rvalue':1} ,"+
                                     "'$update' : [ "+
                                     "{ '$insert': { 'termsVerbiageTranslation.0': {}}},"+
                                     "{ '$set': {'termsVerbiageTranslation.0.localeCode':'lg','termsVerbiageTranslation.0.localeText':'Lang' } }"+
                                     " ] }}");
        
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        System.out.println("before:"+JsonUtils.prettyPrint(jsonDoc.getRoot()));
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        System.out.println("After:"+JsonUtils.prettyPrint(jsonDoc.getRoot()));

        Assert.assertEquals(4, jsonDoc.get(new Path("termsVerbiage.0.termsVerbiageTranslation")).size());
        Assert.assertEquals("lg", jsonDoc.get(new Path("termsVerbiage.0.termsVerbiageTranslation.0.localeCode")).asText());
    }
}
