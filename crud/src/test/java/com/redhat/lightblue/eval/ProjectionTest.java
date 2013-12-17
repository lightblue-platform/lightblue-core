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

import java.util.List;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.JSONMetadataParser;

import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.parser.Extensions;

import com.redhat.lightblue.query.Projection;

public class ProjectionTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory=JsonNodeFactory.withExactBigDecimals(true);
    private DocProjector dp;
    
    @Before
    public void init() {
        dp=new DocProjector(factory);
    }
        

    private static JsonNode json(String q) {
        try {
            return JsonUtils.json(q.replace('\'','\"'));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node=loadJsonNode(fname);
        Extensions<JsonNode> extensions=new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        TypeResolver resolver=new DefaultTypes();
        JSONMetadataParser parser=new JSONMetadataParser(extensions,resolver,factory);
        return parser.parseEntityMetadata(node);
    }

    private JsonDoc getDoc(String fname) throws Exception {
        JsonNode node=loadJsonNode(fname);
        return new JsonDoc(node);
    }

    private Projector projector(String str,EntityMetadata md) {
        Projection p=Projection.fromJson(json(str));
        return Projector.getInstance(p,md);
    }

    @Test
    public void basicProjectionTest() throws Exception {
        EntityMetadata md=getMd("query/testMetadata.json");
        JsonDoc doc=getDoc("query/sample1.json");
        Projector projector=projector("{'field':'*','include':1}",md);
        JsonDoc newDoc=dp.project(doc,projector,md,new QueryEvaluationContext(doc.getRoot()));
        System.out.println(newDoc.getRoot());
        Assert.assertEquals("test",newDoc.get(new Path("object_type")).asText());
        Assert.assertEquals("value1",newDoc.get(new Path("field1")).asText());
        Assert.assertEquals("value2",newDoc.get(new Path("field2")).asText());
        Assert.assertEquals(3,newDoc.get(new Path("field3")).asInt());
        Assert.assertEquals(4.0,newDoc.get(new Path("field4")).asDouble(),0.001);
        Assert.assertEquals(true,newDoc.get(new Path("field5")).asBoolean());
        Assert.assertNotNull(newDoc.get(new Path("field6")));
        Assert.assertNull(newDoc.get(new Path("field6.nf1")));
        Assert.assertNull(newDoc.get(new Path("field6.nf2")));
        Assert.assertNull(newDoc.get(new Path("field6.nf3")));
        Assert.assertNull(newDoc.get(new Path("field6.nf4")));
        Assert.assertNull(newDoc.get(new Path("field6.nf5")));
        Assert.assertNull(newDoc.get(new Path("field6.nf6")));
        Assert.assertNull(newDoc.get(new Path("field6.nf7")));
        Assert.assertNotNull(newDoc.get(new Path("field7")));
        Assert.assertEquals(0,newDoc.get(new Path("field7")).size());
    }

    @Test
    public void basicProjectionTest_recursive() throws Exception {
        EntityMetadata md=getMd("query/testMetadata.json");
        JsonDoc doc=getDoc("query/sample1.json");
        Projector projector=projector("{'field':'*','include':1,'recursive':1}",md);
        JsonDoc newDoc=dp.project(doc,projector,md,new QueryEvaluationContext(doc.getRoot()));
        System.out.println(newDoc.getRoot());
        Assert.assertEquals("test",newDoc.get(new Path("object_type")).asText());
        Assert.assertEquals("value1",newDoc.get(new Path("field1")).asText());
        Assert.assertEquals("value2",newDoc.get(new Path("field2")).asText());
        Assert.assertEquals(3,newDoc.get(new Path("field3")).asInt());
        Assert.assertEquals(4.0,newDoc.get(new Path("field4")).asDouble(),0.001);
        Assert.assertEquals(true,newDoc.get(new Path("field5")).asBoolean());
        Assert.assertNotNull(newDoc.get(new Path("field6")));
        Assert.assertEquals("nvalue1",newDoc.get(new Path("field6.nf1")).asText());
        Assert.assertEquals("nvalue2",newDoc.get(new Path("field6.nf2")).asText());
        Assert.assertEquals(4,newDoc.get(new Path("field6.nf3")).asInt());
        Assert.assertEquals(false,newDoc.get(new Path("field6.nf4")).asBoolean());
        Assert.assertEquals(5,newDoc.get(new Path("field6.nf5.0")).asInt());
        Assert.assertEquals(10,newDoc.get(new Path("field6.nf5.1")).asInt());
        Assert.assertEquals(15,newDoc.get(new Path("field6.nf5.2")).asInt());
        Assert.assertEquals(20,newDoc.get(new Path("field6.nf5.3")).asInt());
        Assert.assertEquals("one",newDoc.get(new Path("field6.nf6.0")).asText());
        Assert.assertEquals("two",newDoc.get(new Path("field6.nf6.1")).asText());
        Assert.assertEquals("three",newDoc.get(new Path("field6.nf6.2")).asText());
        Assert.assertEquals("four",newDoc.get(new Path("field6.nf6.3")).asText());
        Assert.assertEquals("nnvalue1",newDoc.get(new Path("field6.nf7.nnf1")).asText());
        Assert.assertEquals(2,newDoc.get(new Path("field6.nf7.nnf2")).asInt());
        Assert.assertEquals(4,newDoc.get(new Path("field7")).size());
        Assert.assertEquals("elvalue0_1",newDoc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals("elvalue1_1",newDoc.get(new Path("field7.1.elemf1")).asText());
        Assert.assertEquals("elvalue2_1",newDoc.get(new Path("field7.2.elemf1")).asText());
        Assert.assertEquals("elvalue3_1",newDoc.get(new Path("field7.3.elemf1")).asText());
    }
}
