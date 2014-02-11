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

public class ArrayMatchEvaluatorTest extends AbstractJsonNodeTest {

    private EntityMetadata md;
    private JsonDoc doc;
    
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
    
    @Before
    public void setUp() throws Exception {
        md = getMd("./testMetadata2.json");
        doc=getDoc("./sample2.json");
    }

    @Test(expected=com.redhat.lightblue.eval.EvaluationError.class)
    public void non_array_field_results_in_expression_error() throws Exception {
        QueryExpression expr = json("{'array':'field2','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator.getInstance(expr, md);
    }
    
    
    @Test(expected=com.redhat.lightblue.eval.EvaluationError.class)
    public void simple_values_in_expression_for_object_array_results_in_expression_error() throws Exception {
        QueryExpression expr = json("{'array':'field6.nf9','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator.getInstance(expr, md);
    }
    
    @Test
    public void elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr=json("{'array':'field7','elemMatch':{'field':'elemf3','op':'>','rvalue':10}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr=json("{'array':'field7','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    @Test
    public void one_$parent_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr=json("{'array':'field6.$parent.field7','elemMatch':{'field':'elemf3','op':'>','rvalue':10}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$parent_elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr=json("{'array':'field6.$parent.field7','elemMatch':{'field':'elemf3','op':'>','rvalue':3}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    
    @Test
    public void two_$parent_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr=json("{'array':'field6.nf7.$parent.$parent.field7','elemMatch':{'field':'elemf3','op':'>','rvalue':10}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    
    @Test
    public void one_$this_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr=json("{'array':'field8.nf1.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':50}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void one_$this_elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr=json("{'array':'field8.nf1.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':5}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

    
    @Test
    public void two_$this_elem_match_returns_false_when_no_values_in_array_match() throws Exception {
        QueryExpression expr=json("{'array':'field8.nf1.$this.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':50}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertFalse(context.getResult());
    }

    @Test
    public void two_$this_elem_match_returns_true_when_at_least_one_value_in_array_matches() throws Exception {
        QueryExpression expr=json("{'array':'field8.nf1.$this.$this.nnf4','elemMatch':{'field':'elemf3','op':'>','rvalue':5}}");
        QueryEvaluator eval = QueryEvaluator.getInstance(expr, md);

        QueryEvaluationContext context = eval.evaluate(doc);

        Assert.assertTrue(context.getResult());
    }

}
