package com.redhat.lightblue.assoc;

import java.util.List;

import org.junit.Test;
import org.junit.Assert;

import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.AbstractGetMetadata;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.TestDataStoreParser;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

public class RewriteQueryTest extends AbstractJsonNodeTest {

    private class GMD extends AbstractGetMetadata {
        public GMD(Projection p,QueryExpression q) {
            super(p,q);
        }

        @Override
        protected EntityMetadata retrieveMetadata(Path injectionField,
                                                  String entityName,
                                                  String version) {
            try {
                return getMd("composite/"+entityName+".json");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private EntityMetadata getMd(String fname) {
        try {
            JsonNode node = loadJsonNode(fname);
            Extensions<JsonNode> extensions = new Extensions<>();
            extensions.addDefaultExtensions();
            extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
            TypeResolver resolver = new DefaultTypes();
            JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, JsonNodeFactory.instance);
            EntityMetadata md = parser.parseEntityMetadata(node);
            PredefinedFields.ensurePredefinedFields(md);
            return md;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void testReqQuery() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

        // Process request query at root
        AnalyzeQuery pq=new AnalyzeQuery(md,null);
        
        QueryExpression q=query("{'$and':[ { 'field':'field1','op':'=','rvalue':'x'},{'field':'obj1.c.*.field1','op':'=','rvalue':'y'}]}");
        pq.iterate(q);
        List<QueryFieldInfo> list=pq.getFieldInfo();

        // Rewrite the query at root
        RewriteQuery rw=new RewriteQuery(md,md,list);
        QueryExpression newq=rw.iterate(q);
        List<FieldBinding> bindings=rw.getBindings();
        Assert.assertEquals(0,bindings.size());
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        JSONAssert.assertEquals("{field:field1,op:$eq,rvalue:x}", newq.toString(), false);

        rw=new RewriteQuery(md,md.getChildMetadata(new Path("obj1.c")),list);
        newq=rw.iterate(q);
        bindings=rw.getBindings();
        Assert.assertEquals(0,bindings.size());        
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        JSONAssert.assertEquals("{field:field1,op:$eq,rvalue:y}", newq.toString(), false);      
    }

    @Test
    public void testSimpleAssocQuery() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);
        
        AnalyzeQuery pq=new AnalyzeQuery(md,md.getResolvedReferenceOfField(new Path("obj1.c")));
        QueryExpression q=query("{'field':'_id','op':'$eq','rfield':'$parent.c_ref'}");
        pq.iterate(q);
        List<QueryFieldInfo> list=pq.getFieldInfo();

        // Rewrite for C. This means, A docs are retrieved, now we'll retrieve C docs
        RewriteQuery rw=new RewriteQuery(md,md.getChildMetadata(new Path("obj1.c")),list);
        QueryExpression newq=rw.iterate(q);
        List<FieldBinding> bindings=rw.getBindings();
        Assert.assertEquals(1,bindings.size());
        Assert.assertTrue(bindings.get(0) instanceof ValueBinding);
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        ((ValueBinding)bindings.get(0)).getValue().setValue("x");
        JSONAssert.assertEquals("{field:_id,op:$eq,rvalue:x}",newq.toString(),false);

        // Rewrite for A. This means, C docs are retrieved, and we'll retrieve A docs (reverse relationship)
        rw=new RewriteQuery(md,md,list);
        newq=rw.iterate(q);
        bindings=rw.getBindings();
        Assert.assertEquals(1,bindings.size());
        Assert.assertTrue(bindings.get(0) instanceof ValueBinding);
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        ((ValueBinding)bindings.get(0)).getValue().setValue("x");
        JSONAssert.assertEquals("{field:obj1.c_ref,op:$eq,rvalue:x}",newq.toString(),false);
        
    }

    @Test
    public void testReqQuery_forEach_arr_points_to_ref() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

        AnalyzeQuery pq=new AnalyzeQuery(md,null);
        QueryExpression q=query("{'array' : 'obj1.c', 'elemMatch':{'field':'_id','op':'$eq','rfield':'$parent.c_ref'}}");
        pq.iterate(q);
        List<QueryFieldInfo> list=pq.getFieldInfo();

        // Rewrite for C. This means, A docs are retrieved, now we'll retrieve C docs
        RewriteQuery rw=new RewriteQuery(md,md.getChildMetadata(new Path("obj1.c")),list);
        QueryExpression newq=rw.iterate(q);
        List<FieldBinding> bindings=rw.getBindings();
        Assert.assertEquals(1,bindings.size());
        Assert.assertTrue(bindings.get(0) instanceof ValueBinding);
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        ((ValueBinding)bindings.get(0)).getValue().setValue("x");
        JSONAssert.assertEquals("{field:_id,op:$eq,rvalue:x}",newq.toString(),false);

        // Rewrite for A. This means, C docs are retrieved, and we'll retrieve A docs (reverse relationship)
        rw=new RewriteQuery(md,md,list);
        newq=rw.iterate(q);
        bindings=rw.getBindings();
        Assert.assertEquals(1,bindings.size());
        Assert.assertTrue(bindings.get(0) instanceof ValueBinding);
        Assert.assertTrue(newq instanceof ValueComparisonExpression);
        ((ValueBinding)bindings.get(0)).getValue().setValue("x");
        JSONAssert.assertEquals("{field:obj1.c_ref,op:$eq,rvalue:x}",newq.toString(),false);
        
    }

    @Test
    public void testAssocQuery_forEach_arr_points_to_ref() throws Exception {
        GMD gmd=new GMD(projection("{'field':'users','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/UC.json"),gmd);

        AnalyzeQuery pq=new AnalyzeQuery(md,md.getResolvedReferenceOfField(new Path("users")));
        QueryExpression q=query("{'$and':[ { 'field': '_id','op': '$eq','rfield': '$parent.userId' },"+
                                "{'array': 'authentications','elemMatch': {'$and': ["+
                                "{'field': 'providerName','op': '$eq','rvalue': 'p'},"+
                                "{'field': 'principal','op': '$eq','rfield': '$parent.$parent.userRedHatPrincipal'}]} } ] }");
        pq.iterate(q);
        List<QueryFieldInfo> list=pq.getFieldInfo();

        // UC is the parent, U is the child
        
        // Rewrite for U. This means, UC docs are retrieved, now we'll retrieve U docs
        // This is the trivial rewrite case
        RewriteQuery rw=new RewriteQuery(md,md.getChildMetadata(new Path("users")),list);
        QueryExpression newq=rw.iterate(q);
        List<FieldBinding> bindings=rw.getBindings();
        Assert.assertEquals(2,bindings.size());
        Assert.assertTrue(bindings.get(0) instanceof ValueBinding);
        ((ValueBinding)bindings.get(0)).getValue().setValue("x");
        ((ValueBinding)bindings.get(1)).getValue().setValue("y");
        JSONAssert.assertEquals("{$and:[{field:_id,op:$eq,rvalue:x},"+
                                "{array:authentications,elemMatch:{$and:["+
                                "{field:providerName,op:$eq,rvalue:p},"+
                                "{field:principal,op:$eq,rvalue:y}]}}]}",newq.toString(),false);

        // Rewrite for UC. That means, U docs are retrieved, and we'll retrieve UC
        // This is the reverse case
        rw=new RewriteQuery(md,md,list);
        newq=rw.iterate(q);
        System.out.println(newq);
        bindings=rw.getBindings();
        Assert.assertEquals(2,bindings.size());
        ((ValueBinding)bindings.get(0)).getValue().setValue("x");
        ((ValueBinding)bindings.get(1)).getValue().setValue("y");
        JSONAssert.assertEquals("{$and:[{field:userId,op:$eq,rvalue:x},"+
                                "{field:userRedHatPrincipal,op:$eq,rvalue:y}]}",newq.toString(),false);
    }
    
}
