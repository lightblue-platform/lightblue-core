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

import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.eval.QueryEvaluationContext;
import com.redhat.lightblue.eval.QueryEvaluator;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.JSONMetadataParser;

import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.Extensions;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;

import com.redhat.lightblue.query.QueryExpression;
import java.io.IOException;

public class QueryTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    private static class QTestCase {
        final QueryExpression query;
        final boolean match;
        final Path[] matching;

        public QTestCase(QueryExpression query, boolean match, Path... matching) {
            this.query = query;
            this.match = match;
            this.matching = matching;
        }

        public QTestCase(JsonNode query, boolean match, Path... matching) {
            this(QueryExpression.fromJson(query), match, matching);
        }

        public QTestCase(String query, boolean match, Path... matching) {
            this(json(query), match, matching);
        }

    }

    private static Path[] path(String... s) {
        Path[] ret;
        if (s != null) {
            ret = new Path[s.length];
            for (int i = 0; i < s.length; i++) {
                ret[i] = new Path(s[i]);
            }
        } else {
            ret = null;
        }
        return ret;
    }

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
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private JsonDoc getDoc(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        return new JsonDoc(node);
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    private QueryEvaluationContext runQuery(EntityMetadata md,
                                            JsonDoc doc,
                                            QueryExpression query) throws Exception {
        QueryEvaluator eval = QueryEvaluator.getInstance(query, md);
        return eval.evaluate(doc);
    }

    private static final QTestCase[] fieldTests = new QTestCase[]{
        new QTestCase("{'field':'field1','op':'$eq','rvalue':'value1'}", true),
        new QTestCase("{'field':'field1','op':'$eq','rvalue':'value2'}", false),
        new QTestCase("{'field':'field1','op':'$neq','rvalue':'value2'}", true),
        new QTestCase("{'field':'field5','op':'$eq','rvalue':true}", true),
        new QTestCase("{'field':'field5','op':'$eq','rvalue':'true'}", true),
        new QTestCase("{'field':'field5','op':'$eq','rvalue':1}", true),
        new QTestCase("{'$and': [ {'field':'field3','op':'$gt','rvalue':2},{'field':'field7.0.elemf1','op':'$eq','rvalue':'elvalue0_1'}]}",
        true),
        new QTestCase("{'field':'field7.0.elemf3','op':'<','rfield':'field7.1.elemf3'}", true),
        new QTestCase("{'field':'field6.nf3','op':'$in','values':[1,2,3,4]}", true),
        new QTestCase("{'field':'field6.nf3','op':'$nin','values':[1,2,3,4]}", false),
        new QTestCase("{'field':'field6.nf1','op':'$in','values':['blah','yada','nvalue1']}", true),
        new QTestCase("{'field':'field6.nf1','regex':'nvalue.*'}", true),
        new QTestCase("{'field':'field6.nf1','regex':'Nvalue.*'}", false),
        new QTestCase("{'field':'field6.nf1','regex':'Nvalue.*','case_insensitive':true}", true)
    };

    @Test
    public void simpleFieldTests() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        JsonDoc doc = getDoc("./sample1.json");
        for (QTestCase t : fieldTests) {
            System.out.println(t.query);
            Assert.assertEquals(t.query.toString(), t.match, runQuery(md, doc, t.query).getResult());
        }
    }

    private static final QTestCase[] arrayTests = new QTestCase[]{
        new QTestCase("{'array':'field6.nf6','contains':'$any','values':['zero','one']}", true, path("field6.nf6.0")),
        new QTestCase("{'array':'field6.nf6','contains':'$any','values':['zero','blah']}", false),
        new QTestCase("{'array':'field6.nf6','contains':'$all','values':['zero','one']}", false),
        new QTestCase("{'array':'field6.nf6','contains':'$all','values':['three','one']}", true, path("field6.nf6.0", "field6.nf6.2")),
        new QTestCase("{'array':'field6.nf6','contains':'$none','values':['zero','one']}", false),
        new QTestCase("{'array':'field6.nf6','contains':'$none','values':['zero','blah']}", true),
        new QTestCase("{'array':'field7','elemMatch': { 'field':'elemf1','op':'$eq','rvalue':'elvalue1_1' }}", true, path("field7.1")),};

    @Test
    public void arrayFieldTests() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        JsonDoc doc = getDoc("./sample1.json");
        for (QTestCase t : arrayTests) {
            System.out.println(t.query);
            QueryEvaluationContext ctx = runQuery(md, doc, t.query);
            Assert.assertEquals(t.query.toString(), t.match, ctx.getResult());
            if (t.matching != null) {
                System.out.println("Excluded elements:" + ctx.getExcludedArrayElements());
                for (Path x : t.matching) {
                    Assert.assertTrue(t.query.toString() + " expect match " + x, ctx.isMatchingElement(x));
                }
            }
        }
    }
}
