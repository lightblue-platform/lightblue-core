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

public class UpdateExpressionListEvaluatorTest extends AbstractJsonNodeTest {

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
    public void setSimpleFieldTest() throws Exception {

        UpdateExpression expr=json("[ {'$set' : { 'field1' : 'set1', 'field2':'set2', 'field5': 0, 'field6.nf1':'set6' } }, {'$add' : { 'field3':1 } } ] ");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals("set1",doc.get(new Path("field1")).asText());
        Assert.assertEquals("set2",doc.get(new Path("field2")).asText());
        Assert.assertEquals(4,doc.get(new Path("field3")).asInt());
        Assert.assertFalse(doc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6",doc.get(new Path("field6.nf1")).asText());
    }

    @Test
    public void one_$parent_setSimpleFieldTest() throws Exception {

        UpdateExpression expr=json("[ {'$set' : { 'field2.$parent.field1' : 'set1', 'field3.$parent.field2':'set2', 'field2.$parent.field5': 0, 'field2.$parent.field6.nf1':'set6' } }, {'$add' : { 'field2.$parent.field3':1 } } ] ");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals("set1",doc.get(new Path("field1")).asText());
        Assert.assertEquals("set2",doc.get(new Path("field2")).asText());
        Assert.assertEquals(4,doc.get(new Path("field3")).asInt());
        Assert.assertFalse(doc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6",doc.get(new Path("field6.nf1")).asText());
    }
    
}
