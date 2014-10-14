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
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ProjectorTest extends AbstractJsonNodeTest {

    EntityMetadata md;

    @Before
    public void setup() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void fieldProjectorTest_nonrecursive() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field2'},{'field':'field6.*'}]");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertEquals("value2", pdoc.get(new Path("field2")).asText());
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field7")));
        Assert.assertEquals(10, pdoc.get(new Path("field6")).size());
        Assert.assertNotNull(pdoc.get(new Path("field6.nf7")));
        Assert.assertNull(pdoc.get(new Path("field6.nf7.nnf1")));
        Assert.assertNull(pdoc.get(new Path("field6.nf7.nnf2")));
    }

    @Test
    public void fieldProjectorTest_recursive() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field2'},{'field':'field6.*','recursive':true}]");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertEquals("value2", pdoc.get(new Path("field2")).asText());
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field7")));
        Assert.assertEquals(10, pdoc.get(new Path("field6")).size());
        Assert.assertNotNull(pdoc.get(new Path("field6.nf7")));
        Assert.assertNotNull(pdoc.get(new Path("field6.nf7.nnf1")));
        Assert.assertNotNull(pdoc.get(new Path("field6.nf7.nnf2")));
    }

    @Test
    public void fieldProjectorTest_arr_range() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertNull(pdoc.get(new Path("field2")));
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertEquals(2, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(4, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertEquals(5, pdoc.get(new Path("field7.1.elemf3")).asInt());
        Assert.assertNull(pdoc.get(new Path("field7.0.elemf1")));
        Assert.assertNull(pdoc.get(new Path("field7.0.elemf2")));
        Assert.assertNull(pdoc.get(new Path("field7.1.elemf1")));
        Assert.assertNull(pdoc.get(new Path("field7.1.elemf2")));

    }

    @Test
    public void fieldProjectorTest_arr_query() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertNull(pdoc.get(new Path("field2")));
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertEquals(2, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(5, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertEquals(6, pdoc.get(new Path("field7.1.elemf3")).asInt());
        Assert.assertEquals("elvalue2_1", pdoc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals("elvalue2_2", pdoc.get(new Path("field7.0.elemf2")).asText());
        Assert.assertEquals("elvalue3_1", pdoc.get(new Path("field7.1.elemf1")).asText());
        Assert.assertEquals("elvalue3_2", pdoc.get(new Path("field7.1.elemf2")).asText());

    }

    @Test
    public void fieldProjectorTest_include_then_exclude() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field6.*'},{'field':'field6.nf3','include':false}]");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNotNull(pdoc.get(new Path("field6")));
        Assert.assertEquals("nvalue1", pdoc.get(new Path("field6.nf1")).asText());
        Assert.assertEquals("nvalue2", pdoc.get(new Path("field6.nf2")).asText());
        Assert.assertNull(pdoc.get(new Path("field6.nf3")));
    }

    @Test
    public void fieldProjectorTest_includeArrayExcludeFields() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','include':true,'match':{'field':'elemf3','op':'>','rvalue':4},'project':[{'field':'*'},{'field':'elemf1','include':false}]}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertNull(pdoc.get(new Path("field2")));
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertEquals(2, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(5, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertEquals(6, pdoc.get(new Path("field7.1.elemf3")).asInt());
        Assert.assertNull(pdoc.get(new Path("field7.0.elemf1")));
        Assert.assertEquals("elvalue2_2", pdoc.get(new Path("field7.0.elemf2")).asText());
        Assert.assertNull(pdoc.get(new Path("field7.1.elemf1")));
        Assert.assertEquals("elvalue3_2", pdoc.get(new Path("field7.1.elemf2")).asText());
    }
}
