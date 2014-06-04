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

public class UpdateExpressionListEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;

    @Before
    public void setUp() throws Exception {
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
    public void one_$parent_setSimpleFieldTest() throws Exception {

        UpdateExpression expr = EvalTestContext
                .updateExpressionFromJson("[ {'$set' : { 'field2.$parent.field1' : 'set1', 'field3.$parent.field2':'set2', 'field2.$parent.field5': 0, 'field2.$parent.field6.nf1':'set6' } }, {'$add' : { 'field2.$parent.field3':1 } } ] ");

        Updater updater = Updater.getInstance(JSON_NODE_FACTORY, md, expr);
        Assert.assertTrue(updater.update(jsonDoc, md.getFieldTreeRoot(), new Path()));
        Assert.assertEquals("set1", jsonDoc.get(new Path("field1")).asText());
        Assert.assertEquals("set2", jsonDoc.get(new Path("field2")).asText());
        Assert.assertEquals(4, jsonDoc.get(new Path("field3")).asInt());
        Assert.assertFalse(jsonDoc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6", jsonDoc.get(new Path("field6.nf1")).asText());
    }

}
