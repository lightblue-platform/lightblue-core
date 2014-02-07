package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
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

public class SetExpressionEvaluatorTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);
    
    JsonDoc doc;
    EntityMetadata md;
    
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
        doc = getDoc("./sample1.json");
        md = getMd("./testMetadata.json");
    }

    @Test
    public void nullify_simple_field() throws Exception {
        UpdateExpression expr=json("[ {'$set' : { 'field1' : '$null' } }] ");
        Updater updater=Updater.getInstance(factory,md,expr);

        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals(NullNode.class,doc.get(new Path("field1")).getClass());
    }
    
    @Test
    public void nullify_object_field() throws Exception {
        UpdateExpression expr=json("[ {'$set' : { 'field6' : '$null' } }] ");
        Updater updater=Updater.getInstance(factory,md,expr);

        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals(NullNode.class,doc.get(new Path("field6")).getClass());
    }
    
    @Test(expected=EvaluationError.class)
    public void nullify_array_not_supported() throws Exception {
        UpdateExpression expr=json("[ {'$set' : { 'field7' : '$null' } }] ");  
        Updater updater=Updater.getInstance(factory,md,expr);
        
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals(NullNode.class,doc.get(new Path("field6.nf5")).getClass());
    }

    @Test
    public void nullify_array_element() throws Exception {
        UpdateExpression expr=json("[ {'$set' : { 'field7.1' : '$null' } }] ");
        Updater updater=Updater.getInstance(factory,md,expr);

        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals(NullNode.class,doc.get(new Path("field7.1")).getClass());
    }

    
}
