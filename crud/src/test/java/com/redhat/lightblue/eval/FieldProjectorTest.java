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

public class FieldProjectorTest extends AbstractJsonNodeTest {

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
    public void project_field_without_recursion() throws Exception {
        Projection p=json("[{'field':'field2'},{'field':'field6.*'}]");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{}}}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);

        Assert.assertEquals(expectedNode.toString(),pdoc.toString());
    }

    @Test
    public void project_field_with_recursion() throws Exception {
        Projection p=json("[{'field':'field2'},{'field':'field6.*','recursive':true}]");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[5,10,15,20],'nf6':['one','two','three','four'],'nf7':{'nnf1':'nnvalue1','nnf2':2}}}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());
    }
 
    @Test
    public void one_$parent_project_field_without_recursion() throws Exception {
        Projection p=json("[{'field':'field7.$parent.field2'},{'field':'field7.$parent.field6.*'}]");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{}}}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);

        Assert.assertEquals(expectedNode.toString(),pdoc.toString());
    }

    @Test
    public void one_$parent_project_field_with_recursion() throws Exception {
        Projection p=json("[{'field':'field7.$parent.field2'},{'field':'field7.$parent.field6.*','recursive':true}]");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[5,10,15,20],'nf6':['one','two','three','four'],'nf7':{'nnf1':'nnvalue1','nnf2':2}}}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());
    }
    
    @Test
    public void two_$parent_project_field_without_recursion() throws Exception {
        Projection p=json("[{'field':'field6.nf7.$parent.$parent.field2'},{'field':'field6.nf7.$parent.$parent.field6.*'}]");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{}}}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);

        Assert.assertEquals(expectedNode.toString(),pdoc.toString());
    }

    @Test
    public void two_$parent_project_field_with_recursion() throws Exception {
        Projection p=json("[{'field':'field6.nf7.$parent.$parent.field2'},{'field':'field6.nf7.$parent.$parent.field6.*','recursive':true}]");
        Projector projector=Projector.getInstance(p,md);
        QueryEvaluationContext ctx=new QueryEvaluationContext(doc.getRoot());
        
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[5,10,15,20],'nf6':['one','two','three','four'],'nf7':{'nnf1':'nnvalue1','nnf2':2}}}".replace('\'','\"'));
        
        JsonDoc pdoc=projector.project(doc,factory,ctx);
        
        Assert.assertEquals(expectedNode.toString(),pdoc.toString());
    }
    
}
