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

public class NaryRelationalExpressionEvaluatorTest extends AbstractJsonNodeTest {

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
    public void nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field2.$parent.field6.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$parent_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field2.$parent.field6.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void one_$parent_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field2.$parent.field6.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$parent_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field2.$parent.field6.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf7.$parent.$parent.field6.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void two_$parent_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf7.$parent.$parent.field6.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void two_$parent_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf7.$parent.$parent.field6.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void two_$parent_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.nf7.$parent.$parent.field6.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }    
   
    @Test
    public void one_$this_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$this_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void one_$this_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void one_$this_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void two_$this_nary_in_int_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.$this.nf3','op':'$in','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void two_$this_nary_nin_int_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.$this.nf3','op':'$nin','values':[1,2,3,4]}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
    @Test
    public void two_$this_nary_in_string_array_returns_true_when_field_value_matches_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.$this.nf1','op':'$in','values':['blah','yada','nvalue1']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertTrue(ctx.getResult());
    }
    
    @Test
    public void two_$this_nary_nin_string_array_returns_false_when_field_value_does_not_match_expression() throws Exception {
        QueryExpression q = json("{'field':'field6.$this.$this.nf1','op':'$in','values':['blah','yada','nothere']}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);
        
        QueryEvaluationContext ctx = qe.evaluate(doc);
        
        Assert.assertFalse(ctx.getResult());
    }
    
}
