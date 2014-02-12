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
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class FieldComparisonEvaluatorTest extends AbstractJsonSchemaTest {

    private EntityMetadata md;
    private JsonDoc doc;

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    private JsonDoc getDoc(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        return new JsonDoc(node);
    }

    private EntityMetadata getMd(String fname) throws Exception {
        // verify metadata against schema
        runValidJsonTest("json-schema/metadata/metadata.json", fname);
        
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private QueryExpression json(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Before
    public void setUp() throws Exception {
        md = getMd("./testMetadata.json");
        doc = getDoc("./sample1.json");
    }

    @Test
    public void field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = json("{'field':'field4','op':'>','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = json("{'field':'field4','op':'<','rfield':'field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$parent_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = json("{'field':'field6.$parent.field4','op':'>','rfield':'field6.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$parent_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = json("{'field':'field6.$parent.field4','op':'<','rfield':'field6.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$parent_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = json("{'field':'field6.nf1.$parent.$parent.field4','op':'>','rfield':'field6.nf1.$parent.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$parent_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = json("{'field':'field6.nf1.$parent.$parent.field4','op':'<','rfield':'field6.nf1.$parent.$parent.field3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void one_$this_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = json("{'field':'field7.1.$this.elemf3','op':'>','rfield':'field7.0.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void one_$this_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = json("{'field':'field7.1.$this.elemf3','op':'<','rfield':'field7.0.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }

    @Test
    public void two_$this_field_comparison_returns_true_when_expression_true() throws Exception {
        QueryExpression q = json("{'field':'field7.1.$this.$this.elemf3','op':'>','rfield':'field7.0.$this.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertTrue(ctx.getResult());
    }

    @Test
    public void two_$this_field_comparison_returns_false_when_expression_false() throws Exception {
        QueryExpression q = json("{'field':'field7.1.$this.$this.elemf3','op':'<','rfield':'field7.0.$this.$this.elemf3'}");
        QueryEvaluator qe = QueryEvaluator.getInstance(q, md);

        QueryEvaluationContext ctx = qe.evaluate(doc);

        Assert.assertFalse(ctx.getResult());
    }
}
