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
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ArrayAddExpressionEvaluatorTest extends AbstractJsonNodeTest {

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

    private UpdateExpression json(String s) throws Exception {
        return UpdateExpression.fromJson(JsonUtils.json(s.replace('\'','\"')));
    }
    
    @Before
    public void setUp() throws Exception {
        md = getMd("./testMetadata2.json");
        doc=getDoc("./sample2.json");
    }

    
    @Test(expected=com.redhat.lightblue.eval.EvaluationError.class)
    public void expression_evaluator_not_created_when_specified_field_is_not_an_array() throws Exception {
        UpdateExpression expr=json("{ '$append' : { 'field5' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater.getInstance(factory,md,expr);
    }
    
    @Test(expected=com.redhat.lightblue.eval.EvaluationError.class)
    public void array_insert_without_index_results_in_evaluation_error() throws Exception {
        UpdateExpression expr=json("{ '$insert' : { 'field6.nf6.$parent.nf8' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater.getInstance(factory,md,expr);
    }
    
    @Test(expected=com.redhat.lightblue.eval.EvaluationError.class)
    public void assignment_of_invalid_type_results_in_evaluation_error() throws Exception {
        UpdateExpression expr=json("{ '$insert' : { 'field6.nf7.nnf1.$parent.$parent.nf11.5' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);        
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
    }
    
    @Test
    public void string_array_append_with_1_$parent_relative_path() throws Exception {
        String[] expectedValues = {"four", "three", "two", "one", "five", "six", "value2"};
        JsonNode expectedNode = stringArrayNode(expectedValues);
        UpdateExpression expr=json("{ '$append' : { 'field6.nf6.$parent.nf8' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
        
        Assert.assertEquals(expectedNode, doc.get(new Path("field6.nf8")));
    }
    
    @Test
    public void string_array_append_with_2_$parent_relative_path() throws Exception {
        String[] expectedValues = {"four", "three", "two", "one", "five", "six", "value2"};
        UpdateExpression expr=json("{ '$append' : { 'field6.nf7.nnf1.$parent.$parent.nf8' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
        
        Assert.assertEquals(stringArrayNode(expectedValues), doc.get(new Path("field6.nf8")));
    }
        
    @Test
    public void int_array_append_with_1_$parent_relative_path() throws Exception {
        Integer[] expectedValues = {5, 10, 15, 20, 1, 2, 3};
        UpdateExpression expr=json("{ '$append' : { 'field6.nf6.$parent.nf5' : [ 1,2,{'$valueof':'field3' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
       
        Assert.assertTrue(arrayNodesHaveSameValues(intArrayNode(expectedValues), doc.get(new Path("field6.nf5"))));
    }
    
    @Test
    public void double_array_append_with_1_$parent_relative_path() throws Exception {
        Double[] expectedValues = {20.1, 15.2, 10.3, 5.4, 1.5, 2.6, 4.7};
        UpdateExpression expr=json("{ '$append' : { 'field6.nf6.$parent.nf10' : [ 1.5,2.6,{'$valueof':'field4' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
        
        Assert.assertTrue(arrayNodesHaveSameValues(doubleArrayNode(expectedValues), doc.get(new Path("field6.nf10"))));
    }

    @Test
    public void string_array_insert_with_1_$parent_relative_path() throws Exception {
        String[] expectedValues = {"four", "three", "five", "six", "value2", "two", "one"};
        UpdateExpression expr=json("{ '$insert' : { 'field6.nf6.$parent.nf8.2' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
        
        Assert.assertTrue(arrayNodesHaveSameValues(stringArrayNode(expectedValues), doc.get(new Path("field6.nf8"))));
    }
    
    @Test
    public void string_array_insert_with_2_$parent_relative_path() throws Exception {
        String[] expectedValues = {"four", "three", "five", "six", "value2", "two", "one"};
        UpdateExpression expr=json("{ '$insert' : { 'field6.nf7.nnf1.$parent.$parent.nf8.2' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
        
        Assert.assertEquals(stringArrayNode(expectedValues), doc.get(new Path("field6.nf8")));
    }
    
    @Test
    public void string_array_insert_with_2_$parent_relative_path_and_array_expansion() throws Exception {
        String[] expectedValues = {"four", "three", "two", "one", null, "five", "six", "value2"};
        UpdateExpression expr=json("{ '$insert' : { 'field6.nf7.nnf1.$parent.$parent.nf8.5' : [ 'five','six',{'$valueof':'field2' }] } }");
        Updater updater=Updater.getInstance(factory,md,expr);
        
        updater.update(doc,md.getFieldTreeRoot(),new Path());
        
        Assert.assertEquals(stringArrayNode(expectedValues), doc.get(new Path("field6.nf8")));
    }
    
}
