package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class UnaryLogicalExpressionEvaluatorTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);
    
    private JsonDoc doc;
    private EntityMetadata md;
    
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
    
    @Before
    public void setUp() throws Exception {
        doc=getDoc("./sample1.json");
        md=getMd("./testMetadata.json");
    }

    @Test
    public void $and_expression_case_insensitive_returns_true() throws Exception {
        QueryExpression q=json("{ '$and' : [{'field':'field1','regex':'Val.*','case_insensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void $and_expression_case_insensitive_returns_false() throws Exception {
        QueryExpression q=json("{'$not': { '$and' : [{'field':'field1','regex':'Val.*','case_insensitive':1},{'field':'field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void $and_expression_case_sensitiveinsensitive_returns_true() throws Exception {
        QueryExpression q=json("{'$not': { '$or' : [{'field':'field1','regex':'Val.*'},{'field':'field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$parent_$and_expression_case_insensitive_returns_true() throws Exception {
        QueryExpression q=json("{ '$and' : [{'field':'field2.$parent.field1','regex':'Val.*','case_insensitive':1},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$parent_$and_expression_case_insensitive_returns_false() throws Exception {
        QueryExpression q=json("{'$not': { '$and' : [{'field':'field2.$parent.field1','regex':'Val.*','case_insensitive':1},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void one_$parent_$and_expression_case_sensitiveinsensitive_returns_true() throws Exception {
        QueryExpression q=json("{'$not': { '$or' : [{'field':'field2.$parent.field1','regex':'Val.*'},{'field':'field2.$parent.field3','op':'$eq','rvalue':3}]}}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

}
