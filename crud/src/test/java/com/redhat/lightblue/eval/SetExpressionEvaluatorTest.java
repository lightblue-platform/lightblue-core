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

import com.fasterxml.jackson.databind.node.NullNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class SetExpressionEvaluatorTest extends AbstractJsonNodeTest {

    EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void nullify_simple_field() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field1' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field1")).getClass());
    }

    @Test
    public void nullify_simple_field_w_null() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field1' : null } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field1")).getClass());
    }

    @Test
    public void nullify_object_field() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field6' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field6")).getClass());
    }

    @Test
    public void nullify_array() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field7' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field7")).getClass());
    }

    @Test
    public void nullify_array_element() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field7.1' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field7.1")).getClass());
    }

    @Test
    public void assign_obj_to_field() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$set': {'field6': { 'nf2':'blah','nf3':5 } } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals("blah", jsonDoc.get(new Path("field6.nf2")).asText());
        Assert.assertEquals(5, jsonDoc.get(new Path("field6.nf3")).asInt());
    }

    @Test
    public void assign_arr_to_field() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$set': {'field6.nf10': [1,2] } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals("[1,2]", jsonDoc.get(new Path("field6.nf10")).toString());

    }

    @Test
    public void setting_reference_to_null_is_ignored() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$set': {'ref': null } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertFalse(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

    }

    @Test(expected=EvaluationError.class)
    public void setting_reference_to_non_null_throws_exception() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("{'$set': {'ref': 'foo' } }");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertFalse(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));

    }
}
