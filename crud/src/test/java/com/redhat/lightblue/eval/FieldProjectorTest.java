package com.redhat.lightblue.eval;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class FieldProjectorTest extends AbstractJsonNodeTest {

    EntityMetadata md;

    @Before
    public void setUp() throws Exception {
        doc = EvalTestContext.getDoc("./sample1.json");
        md = EvalTestContext.getMd("./testMetadata.json");
    }

    @Test
    public void project_field_without_recursion() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field2'},{'field':'field6.*'}]");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{},'nf8':[],'nf9':[],'nf10':[]}}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(doc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void project_field_with_recursion() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field2'},{'field':'field6.*','recursive':true}]");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());

        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[5,10,15,20],'nf6':['one','two','three','four'],'nf7':{'nnf1':'nnvalue1','nnf2':2},'nf8':['four','three','two','one'],'nf9':[20,15,10,5],'nf10':[20.1,15.2,10.3,5.4]}}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(doc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_project_field_without_recursion() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field7.$parent.field2'},{'field':'field7.$parent.field6.*'}]");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{},'nf8':[],'nf9':[],'nf10':[]}}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(doc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void one_$parent_project_field_with_recursion() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field7.$parent.field2'},{'field':'field7.$parent.field6.*','recursive':true}]");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());

        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[5,10,15,20],'nf6':['one','two','three','four'],'nf7':{'nnf1':'nnvalue1','nnf2':2},'nf8':['four','three','two','one'],'nf9':[20,15,10,5],'nf10':[20.1,15.2,10.3,5.4]}}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(doc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_project_field_without_recursion() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field6.nf7.$parent.$parent.field2'},{'field':'field6.nf7.$parent.$parent.field6.*'}]");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());
        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{},'nf8':[],'nf9':[],'nf10':[]}}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(doc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

    @Test
    public void two_$parent_project_field_with_recursion() throws Exception {
        Projection p = EvalTestContext.projectionFromJson("[{'field':'field6.nf7.$parent.$parent.field2'},{'field':'field6.nf7.$parent.$parent.field6.*','recursive':true}]");
        Projector projector = Projector.getInstance(p, md);
        QueryEvaluationContext ctx = new QueryEvaluationContext(doc.getRoot());

        JsonNode expectedNode = JsonUtils.json("{'field2':'value2','field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[5,10,15,20],'nf6':['one','two','three','four'],'nf7':{'nnf1':'nnvalue1','nnf2':2},'nf8':['four','three','two','one'],'nf9':[20,15,10,5],'nf10':[20.1,15.2,10.3,5.4]}}".replace('\'', '\"'));

        JsonDoc pdoc = projector.project(doc, JSON_NODE_FACTORY, ctx);

        Assert.assertEquals(expectedNode.toString(), pdoc.toString());
    }

}
