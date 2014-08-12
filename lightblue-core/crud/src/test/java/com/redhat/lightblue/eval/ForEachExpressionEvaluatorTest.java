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

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ForEachExpressionEvaluatorTest extends AbstractJsonNodeTest {
    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
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
    public void one_$parent_array_foreach_modone() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field6.$parent.field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : {'$set': { 'elemf1':'test'}} } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(4, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals("test", jsonDoc.get(new Path("field7.0.elemf1")).asText());
    }

    @Test
    public void one_$parent_array_foreach_removeone() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field6.$parent.field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : '$remove' } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(3, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals("elvalue1_1", jsonDoc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals(3, jsonDoc.get(new Path("field7#")).asInt());
        Assert.assertEquals(3, jsonDoc.get(new Path("field7")).size());
    }

    @Test
    public void one_$parent_array_foreach_removeall() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{ '$foreach' : { 'field6.$parent.field7' : '$all', '$update' : '$remove' } }");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

        Assert.assertEquals(0, jsonDoc.get(new Path("field7")).size());
        Assert.assertEquals(0, jsonDoc.get(new Path("field7#")).asInt());
    }
}
