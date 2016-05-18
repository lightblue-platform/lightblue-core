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

import org.junit.Ignore;
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

import com.redhat.lightblue.assoc.scorers.*;

import java.io.IOException;

public class QueryPlanTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    private static JsonNode json(String q) {
        try {
            return JsonUtils.json(q.replace('\'', '\"'));
        } catch (IOException e) {
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
        public GMD(Projection p, QueryExpression q) {
            super(p, q);
        }

        @Override
        protected EntityMetadata retrieveMetadata(Path injectionField,
                                                  String entityName,
                                                  String version) {
            try {
                return getMd("composite/" + entityName + ".json");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    public void basicPlanTest() throws Exception {
        GMD gmd = new GMD(projection("{'field':'obj1.c','include':1}"), null);
        CompositeMetadata md = CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"), gmd);
        QueryPlan qp = new QueryPlan(md, new IndexedFieldScorer());
        System.out.println(qp.mxToString());
        System.out.println(qp.treeToString());

        QueryPlanNode[] sources = qp.getSources();
        Assert.assertEquals(1, sources.length);
        Assert.assertEquals("A", sources[0].getMetadata().getName());

        QueryPlanNode[] dests = sources[0].getDestinations();
        Assert.assertEquals(1, dests.length);
        Assert.assertEquals("C", dests[0].getMetadata().getName());
        Assert.assertEquals(0, dests[0].getDestinations().length);
        Assert.assertEquals(1, dests[0].getSources().length);
        Assert.assertEquals("A", dests[0].getSources()[0].getMetadata().getName());
    }

    @Test
    public void basicFlipTest() throws Exception {
        GMD gmd = new GMD(projection("{'field':'obj1.c','include':1}"), null);
        CompositeMetadata md = CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"), gmd);
        QueryPlan qp = new QueryPlan(md, new IndexedFieldScorer());
        System.out.println(qp.mxToString());
        System.out.println(qp.treeToString());
        qp.flip(qp.getSources()[0], qp.getSources()[0].getDestinations()[0]);
        System.out.println(qp.mxToString());
        System.out.println(qp.treeToString());

        QueryPlanNode[] sources = qp.getSources();
        Assert.assertEquals(1, sources.length);
        Assert.assertEquals("C", sources[0].getMetadata().getName());

        QueryPlanNode[] dests = sources[0].getDestinations();
        Assert.assertEquals(1, dests.length);
        Assert.assertEquals("A", dests[0].getMetadata().getName());
        Assert.assertEquals(0, dests[0].getDestinations().length);
        Assert.assertEquals(1, dests[0].getSources().length);
        Assert.assertEquals("C", dests[0].getSources()[0].getMetadata().getName());
    }

    @Test
    public void two_level_test() throws Exception {
        GMD gmd = new GMD(projection("[{'field':'r.*.r.*','include':1},{'field':'b.*.','include':1}]"), null);
        CompositeMetadata md = CompositeMetadata.buildCompositeMetadata(getMd("composite/R.json"), gmd);
        QueryPlan qp = new QueryPlan(md, new IndexedFieldScorer());
        System.out.println(qp.mxToString());
        System.out.println(qp.treeToString());

        QueryPlanNode[] sources = qp.getSources();
        QueryPlanNode r = sources[0];
        Assert.assertEquals(1, sources.length);
        Assert.assertEquals("R", r.getMetadata().getName());

        QueryPlanNode[] dests = r.getDestinations();
        Assert.assertEquals(2, dests.length);
        QueryPlanNode rr = dests[0].getMetadata().getName().equals("R") ? dests[0] : dests[1];
        QueryPlanNode rb = dests[0].getMetadata().getName().equals("B") ? dests[0] : dests[1];
        Assert.assertEquals(0, rb.getDestinations().length);

        dests = rr.getDestinations();
        Assert.assertEquals(1, dests.length);
        QueryPlanNode rrr = dests[0];
        Assert.assertEquals("R", rrr.getMetadata().getName());

        dests = rrr.getDestinations();
        Assert.assertEquals(0, dests.length);

    }

    @Ignore
    @Test
    public void simple_StringBuilder_vs_String() {
        // intellij claimed in this case a String might be faster.. I felt like testing it.
        // note originally this created a new string builder each iteration.  it's as slow even this way..

        // net result? no appreciable difference for a single instance but scaled to many iterations string builder is
        // an order of magnitude slower.
        int count = 1000000;

        long timeStringBuilder;
        {
            long before = System.currentTimeMillis();
            StringBuilder bld = new StringBuilder();
            for (int i = 0; i < count; i++) {
                bld.delete(0, 100);
                bld.append("asdf").append('_').append(1);
                String name = bld.toString();
            }
            long after = System.currentTimeMillis();

            timeStringBuilder = after - before;
        }

        long timeString;
        {
            long before = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                String name = "asdf" + '_' + 1;
            }
            long after = System.currentTimeMillis();

            timeString = after - before;
        }

        Assert.assertTrue(timeString < timeStringBuilder);
    }
}
