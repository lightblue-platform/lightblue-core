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

import java.io.IOException;

public class QueryPlanIteratorTest extends AbstractJsonNodeTest {

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
    public void simpleTest() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);
        QueryPlan qp=new QueryPlan(md);
        QueryPlanIterator itr=new QueryPlanIterator(qp);

        // There is only one edge, two iterations are possible
        System.out.println(qp.treeToString());
        Assert.assertEquals("A",qp.getSources()[0].getMetadata().getName());

        Assert.assertTrue(itr.next());
        System.out.println(qp.treeToString());
        Assert.assertEquals("C",qp.getSources()[0].getMetadata().getName());

        Assert.assertFalse(itr.next());
    }

    @Test
    public void two_level_test() throws Exception {
        GMD gmd=new GMD(projection("[{'field':'r.*.r.*','include':1},{'field':'b.*.','include':1}]"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/R.json"),gmd);
        QueryPlan qp=new QueryPlan(md);
        QueryPlanIterator itr=new QueryPlanIterator(qp);

        // 3 edges, 8 query plans
        System.out.println(qp.treeToString());
        for(int i=0;i<7;i++) {
            Assert.assertTrue(itr.next());
            System.out.println(qp.treeToString());
        }
        Assert.assertFalse(itr.next());
    }
}

