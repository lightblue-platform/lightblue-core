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
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class ArrayRangeProjectorTest extends AbstractJsonNodeTest {

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

    private Projection json(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replace('\'','\"')));
    }
    
    @Before
    public void setUp() throws Exception {
        doc=getDoc("./sample1.json");
        md=getMd("./testMetadata.json");
    }

    @Test
    public void array_range_projection_with_match() throws Exception {
        Projection p=json("{'field':'field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf3':4},{'elemf3':5}]}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());        
    }
    
    @Test
    public void array_range_projection_with_no_match_returns_empty_node() throws Exception {
        Projection p=json("{'field':'field7','range':[5,6],'project':{'field':'elemf3'}}");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[]}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        System.out.println(pdoc);
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());        
    }

    @Test
    public void one_$parent_array_range_projection_with_match() throws Exception {
        Projection p=json("{'field':'field6.$parent.field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf3':4},{'elemf3':5}]}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());        
    }
    
    @Test
    public void one_$parent_array_range_projection_with_no_match_returns_empty_node() throws Exception {
        Projection p=json("{'field':'field6.$parent.field7','range':[5,6],'project':{'field':'elemf3'}}");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[]}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        System.out.println(pdoc);
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());        
    }
    
    @Test
    public void two_$parent_array_range_projection_with_match() throws Exception {
        Projection p=json("{'field':'field6.nf7.$parent.$parent.field7','range':[1,2],'project':{'field':'elemf3'}}");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[{'elemf3':4},{'elemf3':5}]}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());        
    }
    
    @Test
    public void two_$parent_array_range_projection_with_no_match_returns_empty_node() throws Exception {
        Projection p=json("{'field':'field6.nf7.$parent.$parent.field7','range':[5,6],'project':{'field':'elemf3'}}");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field7':[]}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        System.out.println(pdoc);
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());        
    }
    
}
