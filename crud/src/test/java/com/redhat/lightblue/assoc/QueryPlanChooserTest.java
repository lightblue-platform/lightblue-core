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
package com.redhat.lightblue.assoc;

import java.util.List;

import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.AbstractGetMetadata;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.TestDataStoreParser;
import com.redhat.lightblue.assoc.iterators.*;
import com.redhat.lightblue.assoc.scorers.*;

public class QueryPlanChooserTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    private static JsonNode json(String q) {
        try {
            return JsonUtils.json(q.replace('\'', '\"'));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    private class GMD extends AbstractGetMetadata {
        public GMD(Projection p,QueryExpression q) {
            super(p,q);
        }

        @Override
        protected EntityMetadata retrieveMetadata(Path injectionField,
                                                  String entityName,
                                                  String version) {
            try {
                return getMd("composite/"+entityName+".json");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void constructionTest() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);
        QueryPlanChooser chooser=new QueryPlanChooser(md,
                                                      new BruteForceQueryPlanIterator(),
                                                      new IndexedFieldScorer(),
                                                      query("{'field':'field1','op':'=','rvalue':'s'}"));

        QueryPlanNode[] nodes=chooser.getQueryPlan().getAllNodes();
        QueryPlanNode anode=null;
        QueryPlanNode cnode=null;
        for(QueryPlanNode node:nodes)
            if(node.getMetadata().getName().equals("A"))
                anode=node;
            else if(node.getMetadata().getName().equals("C"))
                cnode=node;
        Assert.assertNotNull(anode);
        Assert.assertNotNull(cnode);
        Assert.assertTrue(chooser.getQueryPlan().isUndirectedConnected(anode,cnode));
        List<Conjunct> edgeData=chooser.getQueryPlan().getEdgeData(anode,cnode).getConjuncts();
        Assert.assertEquals(edgeData,chooser.getQueryPlan().getEdgeData(cnode,anode).getConjuncts());
        // The request query must be associated with A
        Assert.assertTrue(anode.getData().getConjuncts().size()==1);

        System.out.println(chooser.getQueryPlan().treeToString());
        for(QueryPlanNode node:chooser.getQueryPlan().getAllNodes())
            System.out.println(node.getName()+":"+node.getData().getConjuncts());
    }

    @Test
    public void iterationTestA() throws Exception {
        GMD gmd=new GMD(projection("[{'field':'obj1.c','include':1},{'field':'b','include':1}]"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);
        QueryPlanChooser chooser=new QueryPlanChooser(md,
                                                      new BruteForceQueryPlanIterator(),
                                                      new IndexedFieldScorer(),
                                                      query("{'field':'field1','op':'=','rvalue':'s'}"));
        // Iterate
        while(chooser.next());
        Assert.assertNotNull(chooser.getBestPlan());
        System.out.println("Best plan:"+chooser.getBestPlan().treeToString());
        // Best plan should have A first
        Assert.assertEquals(1,chooser.getBestPlan().getSources().length);
        Assert.assertEquals("A",chooser.getBestPlan().getSources()[0].getMetadata().getName());
    }

    @Test
    public void iterationTestC() throws Exception {
        GMD gmd=new GMD(projection("[{'field':'obj1.c','include':1},{'field':'b','include':1}]"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);
        QueryPlanChooser chooser=new QueryPlanChooser(md,
                                                      new BruteForceQueryPlanIterator(),
                                                      new IndexedFieldScorer(),
                                                      query("{'field':'obj1.c.*.field1','op':'=','rvalue':'s'}"));
        // Iterate
        while(chooser.next());
        Assert.assertNotNull(chooser.getBestPlan());
        System.out.println("Best plan:"+chooser.getBestPlan().mxToString());
        System.out.println("Best plan:"+chooser.getBestPlan().treeToString());
        // Best plan should have C first
        Assert.assertEquals(1,chooser.getBestPlan().getSources().length);
        Assert.assertEquals("C",chooser.getBestPlan().getSources()[0].getMetadata().getName());
    }
}
