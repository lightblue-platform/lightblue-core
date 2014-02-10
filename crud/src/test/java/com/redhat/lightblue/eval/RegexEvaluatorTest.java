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

public class RegexEvaluatorTest extends AbstractJsonNodeTest {

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
    public void regex_on_field_partial_string_match_works() throws Exception {
        QueryExpression q=json("{'field':'field1','regex':'val.*'}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void regex_on_field_partial_string_case_insensitive_match_works() throws Exception {
        QueryExpression q=json("{'field':'field1','regex':'Val.*','case_insensitive':1}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void regex_expr_without_case_insensitive_returns_false() throws Exception {
        QueryExpression q=json("{'field':'field1','regex':'Val.*'}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void one_$parent_regex_on_field_partial_string_match_works() throws Exception {
        QueryExpression q=json("{'field':'field2.$parent.field1','regex':'val.*'}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }

   
    @Test
    public void one_$parent_regex_on_field_partial_string_case_insensitive_match_works() throws Exception {
        QueryExpression q=json("{'field':'field2.$parent.field1','regex':'Val.*','case_insensitive':1}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        ctx=qe.evaluate(doc);
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$parent_regex_expr_without_case_insensitive_returns_false() throws Exception {
        QueryExpression q=json("{'field':'field2.$parent.field1','regex':'Val.*'}");
        QueryEvaluator qe=QueryEvaluator.getInstance(q,md);
        QueryEvaluationContext ctx=qe.evaluate(doc);
        ctx=qe.evaluate(doc);
        Assert.assertFalse(ctx.getResult());
    }
    
}
