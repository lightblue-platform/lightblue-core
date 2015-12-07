package com.redhat.lightblue.assoc;

import java.util.List;

import org.junit.Test;
import org.junit.Assert;

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

public class AnalyzeQueryTest extends AbstractJsonNodeTest {

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
    public void testReqQueryInfo() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

        // Process request query at root
        AnalyzeQuery pq=new AnalyzeQuery(md,null);
        QueryExpression q=query("{'$and':[ { 'field':'field1','op':'=','rvalue':'x'},{'field':'obj1.c.*.field1','op':'=','rvalue':'y'}]}");
        pq.iterate(q);
        List<QueryFieldInfo> list=pq.getFieldInfo();
        Assert.assertEquals(new Path("field1"),list.get(0).getFieldNameInClause());
        Assert.assertEquals(new Path("field1"),list.get(0).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("field1"))==list.get(0).getFieldMd());
        Assert.assertTrue(md==list.get(0).getFieldEntity());
        Assert.assertEquals(new Path("field1"),list.get(0).getEntityRelativeFieldName());
        
        Assert.assertEquals(new Path("obj1.c.*.field1"),list.get(1).getFieldNameInClause());
        Assert.assertEquals(new Path("obj1.c.*.field1"),list.get(1).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c.*.field1"))==list.get(1).getFieldMd());
        Assert.assertTrue(md.getChildMetadata(new Path("obj1.c"))==list.get(1).getFieldEntity());
        Assert.assertEquals(new Path("field1"),list.get(1).getEntityRelativeFieldName());
    }
    
    @Test
    public void testAssocQueryInfo() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

        // Process query at root
        AnalyzeQuery pq=new AnalyzeQuery(md,md.getResolvedReferenceOfField(new Path("obj1.c")));
        QueryExpression q=query("{'field':'_id','op':'$eq','rfield':'$parent.c_ref'}");
        pq.iterate(q);
        
        List<QueryFieldInfo> list=pq.getFieldInfo();
        Assert.assertEquals(new Path("_id"),list.get(0).getFieldNameInClause());
        Assert.assertEquals(new Path("_id"),list.get(0).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c.*._id"))==list.get(0).getFieldMd());
        Assert.assertTrue(md.getChildMetadata(new Path("obj1.c"))==list.get(0).getFieldEntity());
        Assert.assertEquals(new Path("_id"),list.get(0).getEntityRelativeFieldName());
        
        Assert.assertEquals(new Path("$parent.c_ref"),list.get(1).getFieldNameInClause());
        Assert.assertEquals(new Path("$parent.c_ref"),list.get(1).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c_ref"))==list.get(1).getFieldMd());
        Assert.assertTrue(md==list.get(1).getFieldEntity());
        Assert.assertEquals(new Path("obj1.c_ref"),list.get(1).getEntityRelativeFieldName());
    }

    @Test
    public void testAssocQueryInfo_secondNode() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

        // Process query at root
        AnalyzeQuery pq=new AnalyzeQuery(md,md.getResolvedReferenceOfField(new Path("obj1.c")));
        QueryExpression q=query("{'field':'_id','op':'$eq','rfield':'$parent.c_ref'}");
        pq.iterate(q);
        
        List<QueryFieldInfo> list=pq.getFieldInfo();
        Assert.assertEquals(new Path("_id"),list.get(0).getFieldNameInClause());
        Assert.assertEquals(new Path("_id"),list.get(0).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c.*._id"))==list.get(0).getFieldMd());
        Assert.assertTrue(md.getChildMetadata(new Path("obj1.c"))==list.get(0).getFieldEntity());
        Assert.assertEquals(new Path("_id"),list.get(0).getEntityRelativeFieldName());
        
        Assert.assertEquals(new Path("$parent.c_ref"),list.get(1).getFieldNameInClause());
        Assert.assertEquals(new Path("$parent.c_ref"),list.get(1).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c_ref"))==list.get(1).getFieldMd());
        Assert.assertTrue(md==list.get(1).getFieldEntity());
        Assert.assertEquals(new Path("obj1.c_ref"),list.get(1).getEntityRelativeFieldName());
    }

    @Test
    public void testReqQueryInfo_forEach() throws Exception {
        GMD gmd=new GMD(projection("{'field':'obj1.c','include':1}"),null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

        // Process query at root
        AnalyzeQuery pq=new AnalyzeQuery(md,null);
        QueryExpression q=query("{'array' : 'obj1.c', 'elemMatch':{'field':'_id','op':'$eq','rfield':'$parent.c_ref'}}");
        pq.iterate(q);
        
        List<QueryFieldInfo> list=pq.getFieldInfo();
        Assert.assertEquals(new Path("_id"),list.get(0).getFieldNameInClause());
        Assert.assertEquals(new Path("obj1.c.*._id"),list.get(0).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c.*._id"))==list.get(0).getFieldMd());
        Assert.assertTrue(md.getChildMetadata(new Path("obj1.c"))==list.get(0).getFieldEntity());
        Assert.assertEquals(new Path("_id"),list.get(0).getEntityRelativeFieldName());
        
        Assert.assertEquals(new Path("$parent.c_ref"),list.get(1).getFieldNameInClause());
        Assert.assertEquals(new Path("obj1.c.*.$parent.c_ref"),list.get(1).getFullFieldName());
        Assert.assertTrue(md.resolve(new Path("obj1.c_ref"))==list.get(1).getFieldMd());
        Assert.assertTrue(md==list.get(1).getFieldEntity());
        Assert.assertEquals(new Path("obj1.c_ref"),list.get(1).getEntityRelativeFieldName());
    }
    
}
