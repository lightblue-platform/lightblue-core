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
package com.redhat.lightblue.metadata;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;

import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;

public class AbstractGetMetadataTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    public class TestDataStoreParser<T> implements DataStoreParser<T> {
        
        @Override
            public DataStore parse(String name, MetadataParser<T> p, T node) {
            return new DataStore() {
                public String getBackend() {
                    return "mongo";
                }
            };
        }
        
        @Override
            public void convert(MetadataParser<T> p, T emptyNode, DataStore object) {
        }
        
        @Override
            public String getDefaultName() {
            return "mongo";
        }
    }

    private EntityMetadata getMd(String fname) {
        try {
            JsonNode node = loadJsonNode(fname);
            Extensions<JsonNode> extensions = new Extensions<>();
            extensions.addDefaultExtensions();
            extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
            TypeResolver resolver = new DefaultTypes();
            JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
            return parser.parseEntityMetadata(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public class GMD extends AbstractGetMetadata {
        @Override
        protected EntityMetadata retrieveMetadata(Path injectionField,
                                                  String entityName,
                                                  String version) {
            return getMd("composite/"+entityName+".json");
        }
    }

    private static JsonNode json(String q) {
        try {
            return JsonUtils.json(q.replace('\'', '\"'));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    public static Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void no_proj() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        GMD gmd=new GMD();
        gmd.add(projection("{'field':'_id','include':1}"));
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,gmd);

        System.out.println(a.toTreeString());
        Assert.assertNull(a.getChildMetadata(new Path("b")));
        Assert.assertNull(a.getChildMetadata(new Path("obj1.c")));
    }


    @Test
    public void only_b_in_a_q() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        GMD gmd=new GMD();
        gmd.add(query("{'field':'b.0.field1','op':'=','rvalue':'x'}"));
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(a.toTreeString());
        Assert.assertNotNull(a.getChildMetadata(new Path("b")));
        Assert.assertNull(a.getChildMetadata(new Path("obj1.c")));

    }

    @Test
    public void b_and_c_in_a_q() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        GMD gmd=new GMD();
        gmd.add(query("{'field':'b.field1','op':'=','rfield':'obj1.c.0._id'}"));
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(a.toTreeString());
        Assert.assertNotNull(a.getChildMetadata(new Path("b")));
        Assert.assertNotNull(a.getChildMetadata(new Path("obj1.c")));

    }


    @Test
    public void only_b_in_a_proj() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        GMD gmd=new GMD();
        gmd.add(projection("{'field':'b','include':1}"));
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(a.toTreeString());
        Assert.assertNotNull(a.getChildMetadata(new Path("b")));
        Assert.assertNull(a.getChildMetadata(new Path("obj1.c")));
    }

    @Test
    public void b_and_c_in_a_proj() throws Exception {
        EntityMetadata md=getMd("composite/A.json");
        GMD gmd=new GMD();
        gmd.add(projection("[{'field':'b','include':1},{'field':'obj1.c','include':1}]"));
        CompositeMetadata a=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(a.toTreeString());
        Assert.assertNotNull(a.getChildMetadata(new Path("b")));
        Assert.assertNotNull(a.getChildMetadata(new Path("obj1.c")));
    }

    @Test
    public void only_1r_in_r_proj() throws Exception {
        EntityMetadata md=getMd("composite/R.json");
        GMD gmd=new GMD();
        gmd.add(projection("{'field':'r','include':1}"));
        CompositeMetadata r=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(r.toTreeString());
        Assert.assertNull(r.getChildMetadata(new Path("b")));
        Assert.assertNull(r.getChildMetadata(new Path("obj1.c")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")));
        Assert.assertNull(r.getChildMetadata(new Path("r.0.b")));
    }
   
    @Test
    public void only_3r_in_r_proj() throws Exception {
        EntityMetadata md=getMd("composite/R.json");
        GMD gmd=new GMD();
        gmd.add(projection("{'field':'r.*.r.*.r.*.b','include':1}"));
        CompositeMetadata r=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(r.toTreeString());
        Assert.assertNull(r.getChildMetadata(new Path("b")));
        Assert.assertNull(r.getChildMetadata(new Path("obj1.c")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")));
        Assert.assertNull(r.getChildMetadata(new Path("r.*.b")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")));
        Assert.assertNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")).getChildMetadata(new Path("r.*.r.*.b")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")).getChildMetadata(new Path("r.*.r.*.r")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")).getChildMetadata(new Path("r.*.r.*.r")).
                             getChildMetadata(new Path("r.*.r.*.r.*.b")));
    }
    @Test
    public void only_3r_in_r_proj_nomask() throws Exception {
        EntityMetadata md=getMd("composite/R.json");
        GMD gmd=new GMD();
        gmd.add(projection("{'field':'r.1.r.0.r.2.b','include':1}"));
        CompositeMetadata r=CompositeMetadata.buildCompositeMetadata(md,gmd);
        System.out.println(r.toTreeString());
        Assert.assertNull(r.getChildMetadata(new Path("b")));
        Assert.assertNull(r.getChildMetadata(new Path("obj1.c")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")));
        Assert.assertNull(r.getChildMetadata(new Path("r.*.b")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")));
        Assert.assertNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")).getChildMetadata(new Path("r.*.r.*.b")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")).getChildMetadata(new Path("r.*.r.*.r")));
        Assert.assertNotNull(r.getChildMetadata(new Path("r")).getChildMetadata(new Path("r.*.r")).getChildMetadata(new Path("r.*.r.*.r")).
                             getChildMetadata(new Path("r.*.r.*.r.*.b")));
    }

}
