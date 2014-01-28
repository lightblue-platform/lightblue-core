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
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.types.TypeResolver;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;


public class QueryEvalTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);
    
    private JsonDoc getDoc(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        return new JsonDoc(node);
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private QueryExpression json(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'','\"')));
    }

    @Test
    public void q_value_comparison() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");
        QueryExpression q=json("{'field':'field4','op':'>','rvalue':3.5}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        q=json("{'field':'field4','op':'<','rvalue':3.5}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
        q=json("{ '$and' : [ {'field':'field4','op':'>','rvalue':3.5},{'field':'field6.nf1','op':'>','rvalue':'nvalue0'}] }");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_field_comparison() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");
        QueryExpression q=json("{'field':'field4','op':'>','rfield':'field3'}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void q_regex() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");
        QueryExpression q=json("{'field':'field1','regex':'val.*'}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        q=json("{'field':'field1','regex':'Val.*','case_insensitive':1}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        q=json("{'field':'field1','regex':'Val.*'}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void q_logical() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");
        QueryExpression q=json("{ '$and' : [{'field':'field1','regex':'Val.*','case_insensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        q=json("{'$not': { '$and' : [{'field':'field1','regex':'Val.*','case_insensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
        q=json("{'$not': { '$or' : [{'field':'field1','regex':'Val.*'},{'field':'field3','op':'$eq','rvalue':3}]}}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

   @Test
    public void q_arr_contains() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");
        QueryExpression q=json("{'array':'field6.nf6','contains':'$any','values':['one','five','six']}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        q=json("{'array':'field6.nf6','contains':'$all','values':['one','five','six']}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
        q=json("{'array':'field6.nf6','contains':'$all','values':['one','two']}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        q=json("{'array':'field6.nf6','contains':'$none','values':['onet','twot']}");
        qe=QueryEvaluator.getInstance(q,md);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

   @Test
    public void q_arr_match() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");
        QueryExpression q=json("{'array':'field7','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
        Assert.assertTrue(!ctx.isMatchingElement(new Path("field7.0")));
        Assert.assertTrue(ctx.isMatchingElement(new Path("field7.1")));
        Assert.assertTrue(ctx.isMatchingElement(new Path("field7.2")));
        Assert.assertTrue(ctx.isMatchingElement(new Path("field7.3")));
    }
}
