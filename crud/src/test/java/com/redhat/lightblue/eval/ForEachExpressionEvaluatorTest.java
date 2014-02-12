package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
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

public class ForEachExpressionEvaluatorTest extends AbstractJsonNodeTest {
    private JsonDoc doc;
    private EntityMetadata md;
    
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
        EntityMetadata md=parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    private UpdateExpression json(String s) throws Exception {
        return UpdateExpression.fromJson(JsonUtils.json(s.replace('\'','\"')));
    }
    
    
    @Before
    public void setUp() throws Exception {
        doc=getDoc("./sample1.json");
        md=getMd("./testMetadata.json");
    }

    @Test
    public void array_foreach_removeall() throws Exception {
        UpdateExpression expr=json("{ '$foreach' : { 'field7' : '$all', '$update' : '$remove' } }");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        
        Assert.assertEquals(0,doc.get(new Path("field7")).size());
        Assert.assertEquals(0,doc.get(new Path("field7#")).asInt());
    }

    @Test
    public void array_foreach_removeone() throws Exception {
        UpdateExpression expr=json("{ '$foreach' : { 'field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : '$remove' } }");        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        
        Assert.assertEquals(3,doc.get(new Path("field7")).size());
        Assert.assertEquals("elvalue1_1",doc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals(3,doc.get(new Path("field7#")).asInt());
        Assert.assertEquals(3,doc.get(new Path("field7")).size());
    }

    @Test
    public void array_foreach_modone() throws Exception {
        UpdateExpression expr=json("{ '$foreach' : { 'field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : {'$set': { 'elemf1':'test'}} } }");        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        
        Assert.assertEquals(4,doc.get(new Path("field7")).size());
        Assert.assertEquals("test",doc.get(new Path("field7.0.elemf1")).asText());
    }
    
    @Test
    public void one_$parent_array_foreach_modone() throws Exception {
        UpdateExpression expr=json("{ '$foreach' : { 'field6.$parent.field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : {'$set': { 'elemf1':'test'}} } }");        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        
        Assert.assertEquals(4,doc.get(new Path("field7")).size());
        Assert.assertEquals("test",doc.get(new Path("field7.0.elemf1")).asText());
    }
    
    @Test
    public void one_$parent_array_foreach_removeone() throws Exception {
        UpdateExpression expr=json("{ '$foreach' : { 'field6.$parent.field7' : { 'field':'elemf1','op':'=','rvalue':'elvalue0_1'} , '$update' : '$remove' } }");        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        
        Assert.assertEquals(3,doc.get(new Path("field7")).size());
        Assert.assertEquals("elvalue1_1",doc.get(new Path("field7.0.elemf1")).asText());
        Assert.assertEquals(3,doc.get(new Path("field7#")).asInt());
        Assert.assertEquals(3,doc.get(new Path("field7")).size());
    }
    
    @Test
    public void one_$parent_array_foreach_removeall() throws Exception {
        UpdateExpression expr=json("{ '$foreach' : { 'field6.$parent.field7' : '$all', '$update' : '$remove' } }");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        
        Assert.assertEquals(0,doc.get(new Path("field7")).size());
        Assert.assertEquals(0,doc.get(new Path("field7#")).asInt());
    }
}
