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
        Assert.assertEquals(5, pdoc.get(new Path("field6")).size());
        Assert.assertNotNull(pdoc.get(new Path("field6.nf4")));
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
        Assert.assertEquals(11, pdoc.get(new Path("field6")).size());
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
    public void fieldProjectorTest_arr_range_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','range':[1,2],'projection':{'field':'elemf3'}}");
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
    public void fieldProjectorTest_arr_range_zero_zero_bounds() throws Exception {

        Projection p = EvalTestContext
                .projectionFromJson("{'field':'field7','range':[0,0],'projection':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertEquals(1, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(3, pdoc.get(new Path("field7.0.elemf3")).asInt());
    }

    @Test
    public void fieldProjectorTest_arr_range_zero_upper_bound() throws Exception {
        Projection p = EvalTestContext
                .projectionFromJson("{'field':'field7','range':[1,0],'projection':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field7")));

    }

    @Test
    public void fieldProjectorTest_arr_range_to_gt_from() throws Exception {
        Projection p = EvalTestContext
                .projectionFromJson("{'field':'field7','range':[3,1],'projection':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field7")));

    }

    @Test
    public void fieldProjectorTest_arr_range_negative_upper_bound() throws Exception {
        Projection p = EvalTestContext
                .projectionFromJson("{'field':'field7','range':[1,-8],'projection':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field7")));
    }

    @Test
    public void fieldProjectorTest_arr_range_null_upper_bound() throws Exception {
        Projection p = EvalTestContext
                .projectionFromJson("{'field':'field7','range':[1,null],'projection':{'field':'elemf3'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertEquals(3, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(4, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertEquals(5, pdoc.get(new Path("field7.1.elemf3")).asInt());
        Assert.assertEquals(6, pdoc.get(new Path("field7.2.elemf3")).asInt());

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
    public void fieldProjectorTest_arr_query_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':4},'projection':{'field':'*'}}");
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
    public void fieldProjectorTest_arr_star_query() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field12.nf1.nnf1.*.nnnf1.arr','match':{'field':'id','op':'=','rvalue':12}}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        System.out.println(pdoc);
    }

    @Test
    public void fieldProjectorTest_arr_query_sort_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>=','rvalue':4},'project':{'field':'*'}, 'sort': { 'elemf2':'$desc'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertNull(pdoc.get(new Path("field2")));
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertEquals(3, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(6, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertEquals(5, pdoc.get(new Path("field7.1.elemf3")).asInt());
        Assert.assertEquals(4, pdoc.get(new Path("field7.2.elemf3")).asInt());
    }

    @Test
    public void fieldProjectorTest_arr_query_sort() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>=','rvalue':4},'projection':{'field':'*'}, 'sort': { 'elemf2':'$desc'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertNull(pdoc.get(new Path("field1")));
        Assert.assertNull(pdoc.get(new Path("field2")));
        Assert.assertNull(pdoc.get(new Path("field3")));
        Assert.assertNull(pdoc.get(new Path("field4")));
        Assert.assertNull(pdoc.get(new Path("field5")));
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertEquals(3, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(6, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertEquals(5, pdoc.get(new Path("field7.1.elemf3")).asInt());
        Assert.assertEquals(4, pdoc.get(new Path("field7.2.elemf3")).asInt());
    }

    @Test
    public void fieldProjectorTest_arr_query_or() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'$or':[{'field':'elemf3','op':'>','rvalue':4},{'field':'elemf3','op':'>','rvalue':5}]},'project':{'field':'*'}}");
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
    public void fieldProjectorTest_arr_query_or_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'$or':[{'field':'elemf3','op':'>','rvalue':4},{'field':'elemf3','op':'>','rvalue':5}]},'projection':{'field':'*'}}");
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

    @Test
    public void fieldProjectorTest_includeArrayExcludeFields_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','include':true,'match':{'field':'elemf3','op':'>','rvalue':4},'projection':[{'field':'*'},{'field':'elemf1','include':false}]}");
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

    @Test
    public void fieldProjectorTest_includeArrayExcludeFields2() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'*'},{'field':'field7.*.*'},{'field':'field7.*.elemf1','include':0}]");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertEquals("value1", pdoc.get(new Path("field1")).asText());
        Assert.assertEquals("value2", pdoc.get(new Path("field2")).asText());
        Assert.assertEquals(3, pdoc.get(new Path("field3")).asInt());
        Assert.assertEquals(4.0, pdoc.get(new Path("field4")).asDouble(), 0.1);
        Assert.assertTrue(pdoc.get(new Path("field5")).asBoolean());
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertEquals(4, pdoc.get(new Path("field7")).size());
        Assert.assertEquals(3, pdoc.get(new Path("field7.0.elemf3")).asInt());
        Assert.assertNull(pdoc.get(new Path("field7.0.elemf1")));
    }

    @Test
    public void fieldProjectorTest_nestedArray() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field11', 'include':1, 'match':{ 'field':'f1','op':'=','rfield':'f1'}, "
                + "'project':[ {'field':'arr','include':1,'match':{'field':'id','op':'=','rvalue':1}, 'project':[{'field':'x1'},{'field':'x2'}]}  ] }");
        Assert.assertTrue("x1 is required", p.isFieldRequiredToEvaluateProjection(new Path("field11.*.arr.*.x1")));
        Assert.assertTrue("id is required", p.isFieldRequiredToEvaluateProjection(new Path("field11.*.arr.*.id")));
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field11.*.arr.*.x1")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field11.0.arr.0.x2")));
        System.out.println("doc:" + pdoc);
    }

    @Test
    public void fieldProjectorTest_nestedArray_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field11', 'include':1, 'match':{ 'field':'f1','op':'=','rfield':'f1'}, "
                + "'project':[ {'field':'arr','include':1,'match':{'field':'id','op':'=','rvalue':1}, 'projection':[{'field':'x1'},{'field':'x2'}]}  ] }");
        Assert.assertTrue("x1 is required", p.isFieldRequiredToEvaluateProjection(new Path("field11.*.arr.*.x1")));
        Assert.assertTrue("id is required", p.isFieldRequiredToEvaluateProjection(new Path("field11.*.arr.*.id")));
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field11.*.arr.*.x1")));
        Assert.assertEquals(Projection.Inclusion.explicit_inclusion, p.getFieldInclusion(new Path("field11.0.arr.0.x2")));
        System.out.println("doc:" + pdoc);
    }

    @Test
    public void emptyArray() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'*','recursive':1},{'field':'field11', 'include':1, 'match':{ 'field':'f1','op':'=','rvalue':100}}]");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        System.out.println(pdoc);
        Assert.assertNull(pdoc.get(new Path("field11.0")));
        Assert.assertNull(pdoc.get(new Path("field11")));
    }

    /**
     * If there is an empty array in doc, it is copied even if it is not
     * projected
     */
    @Test
    public void dont_project_unwanted_emptyArray() throws Exception {
        // Set nf5 to an empty array
        jsonDoc.modify(new Path("field6.nf5"), JSON_NODE_FACTORY.arrayNode(), true);
        Projection p = EvalTestContext.projectionFromJson("{'field':'field1'}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        System.out.println(pdoc);
        Assert.assertNotNull(pdoc.get(new Path("field1")));
        Assert.assertNull(pdoc.get(new Path("field6")));
        Assert.assertNull(pdoc.get(new Path("field6.nf5")));
    }

    @Test
    public void project_wanted_emptyArray() throws Exception {
        // Set nf5 to an empty array
        jsonDoc.modify(new Path("field6.nf5"), JSON_NODE_FACTORY.arrayNode(), true);
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.*'}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        System.out.println(pdoc);
        Assert.assertNotNull(pdoc.get(new Path("field6")));
        Assert.assertNotNull(pdoc.get(new Path("field6.nf5")));
    }

    @Test
    public void include_then_exclude_626() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[ {'field':'field7','include':true,'match':{'field':'elemf3','op':'>','rvalue':3}},"
                + "  {'field':'field7','include':false,'match':{'field':'elemf2','op':'=','rvalue':'elvalue1_2'}},"
                + "  {'field':'*','include':true,'recursive':false} ] ");

        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);
        System.out.println(pdoc);
        // 3 is included, because 2nd clause includes it
        // 5 is included, becayse 1st clause includes it
        Assert.assertEquals("3", pdoc.get(new Path("field7.0.elemf3")).asText());
        Assert.assertEquals("5", pdoc.get(new Path("field7.1.elemf3")).asText());
    }
}
