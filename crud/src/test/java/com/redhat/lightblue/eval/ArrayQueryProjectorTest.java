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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ArrayQueryProjectorTest extends AbstractJsonNodeTest {

    EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        jsonDoc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test(expected = com.redhat.lightblue.eval.EvaluationError.class)
    public void non_array_field_results_in_expression_error() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field2','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}");
        Projector.getInstance(p, md);
    }

    @Test(expected = com.redhat.lightblue.eval.EvaluationError.class)
    public void non_array_field_results_in_expression_error_with_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field2','match':{'field':'elemf3','op':'>','rvalue':4},'projection':{'field':'*'}}");
        Projector.getInstance(p, md);
    }

    @Test(expected = com.redhat.lightblue.util.Error.class)
    public void array_query_projection_with_incompatible_value() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':'foo'},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);

        projector.project(jsonDoc, JSON_NODE_FACTORY);
    }

    @Test(expected = com.redhat.lightblue.util.Error.class)
    public void array_query_projection_with_incompatible_value_with_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':'foo'},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);

        projector.project(jsonDoc, JSON_NODE_FACTORY);
    }

    @Test
    public void array_query_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void array_query_projection_with_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':4},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void array_query_projection_with_no_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':25},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void array_query_projection_with_no_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7','match':{'field':'elemf3','op':'>','rvalue':25},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    // https://github.com/lightblue-platform/lightblue-core/issues/347
    @Test
    public void array_query_projection_only_one_match_others_should_be_excluded() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field12.nf1.nnf1.*.nnnf1.arr','match':{'field':'id','op':'=','rvalue':1}}");
        Projector projector = Projector.getInstance(p, md);
        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        System.out.println("doc:" + pdoc);
        ArrayNode node = (ArrayNode) pdoc.get(new Path("field12.nf1.nnf1.0.nnnf1.arr"));
        Assert.assertEquals(1, node.size());
        node = (ArrayNode) pdoc.get(new Path("field12.nf1.nnf1"));
        Assert.assertEquals(1, node.size());
    }

    @Test
    public void one_$parent_array_query_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_array_query_projection_with_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':4},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_array_query_projection_with_no_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':25},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_array_query_projection_with_no_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':25},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_array_query_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.nf7.$parent.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_array_query_projection_with_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.nf7.$parent.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':4},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_array_query_projection_with_no_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.nf7.$parent.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':25},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_array_query_projection_with_no_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field6.nf7.$parent.$parent.field7','match':{'field':'elemf3','op':'>','rvalue':25},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$this_array_query_projection_with_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7.$this','match':{'field':'elemf3','op':'>','rvalue':4},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$this_array_query_projection_with_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7.$this','match':{'field':'elemf3','op':'>','rvalue':4},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf1':'elvalue2_1','elemf2':'elvalue2_2','elemf3':5},{'elemf1':'elvalue3_1','elemf2':'elvalue3_2','elemf3':6}]}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$this_array_query_projection_with_no_match() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7.$this','match':{'field':'elemf3','op':'>','rvalue':25},'project':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$this_array_query_projection_with_no_match_projection() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("{'field':'field7.$this','match':{'field':'elemf3','op':'>','rvalue':25},'projection':{'field':'*'}}");
        Projector projector = Projector.getInstance(p, md);
        JsonNode expectedNode = JsonUtils.json("{}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(jsonDoc, JSON_NODE_FACTORY);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

}
