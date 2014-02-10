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

public class NaryLogicalExpressionEvaluatorTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    private EntityMetadata md;
    private JsonDoc doc;
    
    private QueryExpression json(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'','\"')));
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
    
    @Before
    public void setUp() throws Exception {
        md = getMd("./testMetadata.json");
        doc = getDoc("./sample1.json");
    }
        
    @Test
    public void $and_logical_expression_returns_true_when_all_expressions_true() throws Exception {
        QueryExpression q = json("{'$and': [ {'field':'field3','op':'$gt','rvalue':2},{'field':'field7.0.elemf1','op':'$eq','rvalue':'elvalue0_1'}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$parent_$and_logical_expression_returns_true_when_all_expressions_true() throws Exception {
        QueryExpression q = json("{'$and': [ {'field':'field2.$parent.field3','op':'$gt','rvalue':2},{'field':'field2.$parent.field7.0.elemf1','op':'$eq','rvalue':'elvalue0_1'}]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
}