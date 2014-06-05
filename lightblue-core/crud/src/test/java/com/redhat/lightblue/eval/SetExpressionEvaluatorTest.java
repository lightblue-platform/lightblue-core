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
    public void nullify_object_field() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field6' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field6")).getClass());
    }

    @Test(expected = EvaluationError.class)
    public void nullify_array_not_supported() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field7' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field6.nf5")).getClass());
    }

    @Test
    public void nullify_array_element() throws Exception {
        UpdateExpression expr = EvalTestContext.updateExpressionFromJson("[ {'$set' : { 'field7.1' : '$null' } }] ");
        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);

        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals(NullNode.class, jsonDoc.get(new Path("field7.1")).getClass());
    }

}
