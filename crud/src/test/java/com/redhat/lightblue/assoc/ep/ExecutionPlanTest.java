package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.assoc.*;

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
import com.redhat.lightblue.assoc.scorers.IndexedFieldScorer;
import com.redhat.lightblue.assoc.iterators.BruteForceQueryPlanIterator;
import com.redhat.lightblue.assoc.scorers.SimpleScorer;
import com.redhat.lightblue.assoc.iterators.First;

public class ExecutionPlanTest extends AbstractJsonNodeTest {

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
   public void retrieveAandBonly() throws Exception {
       Projection p=projection("[{'field':'*','recursive':1},{'field':'b'}]");
       QueryExpression q=query("{'field':'_id','op':'=','rvalue':'A01'}");
       GMD gmd=new GMD(p,null);
       CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

       Set<CompositeMetadata> minimalTree=new HashSet<>();
       minimalTree.add(md);
       minimalTree.add(md.getChildMetadata(new Path("b")));
       QueryPlan searchQP=new QueryPlanChooser(md,
                                               new BruteForceQueryPlanIterator(),
                                               new IndexedFieldScorer(),
                                               q,
                                               minimalTree).choose();
       ExecutionPlan ep=new ExecutionPlan(q,p,null,md,searchQP,null);
       ObjectNode j=(ObjectNode)ep.toJson();
       ObjectNode root=(ObjectNode)j.get("root");
       
       System.out.println("retrieveAandBonly");
       System.out.println(ep.toString());
       System.out.println("minimal tree:"+minimalTree);

       ArrayNode children=(ArrayNode)root.get("children");
       Assert.assertEquals(1,children.size());
       ObjectNode a0=(ObjectNode)children.get(0);
       Assert.assertEquals("A_0",a0.get("name").asText());
       children=(ArrayNode)a0.get("children");
       Assert.assertEquals(1,children.size());
       ObjectNode b1=(ObjectNode)children.get(0);
       Assert.assertEquals("B_1",b1.get("name").asText());
   }

    @Test
    public void retrieveABC() throws Exception {
       Projection p=projection("[{'field':'*','recursive':1},{'field':'b'},{'field':'obj1.c'}]");
       QueryExpression q=query("{'field':'_id','op':'=','rvalue':'A01'}");
       GMD gmd=new GMD(p,null);
       CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

       Set<CompositeMetadata> minimalTree=new HashSet<>();
       minimalTree.add(md);
       minimalTree.add(md.getChildMetadata(new Path("b")));
       minimalTree.add(md.getChildMetadata(new Path("obj1.c")));
       QueryPlan searchQP=new QueryPlanChooser(md,
                                               new BruteForceQueryPlanIterator(),
                                               new IndexedFieldScorer(),
                                               q,
                                               minimalTree).choose();
       ExecutionPlan ep=new ExecutionPlan(q,p,null,md,searchQP,null);
       ObjectNode j=(ObjectNode)ep.toJson();
       ObjectNode root=(ObjectNode)j.get("root");
       
       System.out.println("retrieveABC");
       System.out.println(ep.toString());
       System.out.println("minimal tree:"+minimalTree);

       ArrayNode children=(ArrayNode)root.get("children");
       Assert.assertEquals(1,children.size());
       ObjectNode a0=(ObjectNode)children.get(0);
       Assert.assertEquals("A_0",a0.get("name").asText());
       children=(ArrayNode)a0.get("children");
       Assert.assertEquals(2,children.size());
       ObjectNode c1=(ObjectNode)children.get(0);
       Assert.assertEquals("C_1",c1.get("name").asText());
       ObjectNode b2=(ObjectNode)children.get(1);
       Assert.assertEquals("B_2",b2.get("name").asText());
   }


   @Test
   public void retrieveAandConly_CFirst() throws Exception {
       Projection p=projection("[{'field':'*','recursive':1},{'field':'obj1.c'}]");
       QueryExpression q=query("{'field':'obj1.c.*.field1','op':'=','rvalue':'ABFPwrjyx-o5DQWWZmSEfKf3W1z'}");
       GMD gmd=new GMD(p,null);
       CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/A.json"),gmd);

       Set<CompositeMetadata> minimalTree=new HashSet<>();
       minimalTree.add(md);
       minimalTree.add(md.getChildMetadata(new Path("obj1.c")));
       QueryPlan searchQP=new QueryPlanChooser(md,
                                               new BruteForceQueryPlanIterator(),
                                               new IndexedFieldScorer(),
                                               q,
                                               minimalTree).choose();
       QueryPlan retrievalQP=new QueryPlanChooser(md,
                                                  new First(),
                                                  new SimpleScorer(),
                                                  q,
                                                  minimalTree).choose();
       
       ExecutionPlan ep=new ExecutionPlan(q,p,null,md,searchQP,retrievalQP);
       ObjectNode j=(ObjectNode)ep.toJson();
       ObjectNode root=(ObjectNode)j.get("root");
       
       System.out.println("retrieveAandConly_CFirst");
       System.out.println(ep.toString());
       System.out.println("minimal tree:"+minimalTree);

       ArrayNode children=(ArrayNode)root.get("children");
       Assert.assertEquals(1,children.size());
       ObjectNode c1=(ObjectNode)children.get(0);
       Assert.assertEquals("C_1",c1.get("name").asText());
       children=(ArrayNode)c1.get("children");
       Assert.assertEquals(1,children.size());
       ObjectNode a0=(ObjectNode)children.get(0);
       Assert.assertEquals("A_0",a0.get("name").asText());
       children=(ArrayNode)a0.get("children");
       Assert.assertEquals(1,children.size());
       ObjectNode f=(ObjectNode)children.get(0);
       children=(ArrayNode)f.get("children");
       Assert.assertEquals(1,children.size());
       a0=(ObjectNode)children.get(0);
       Assert.assertEquals("A_0",a0.get("name").asText());
       children=(ArrayNode)a0.get("children");
       Assert.assertEquals(1,children.size());
       c1=(ObjectNode)children.get(0);
       Assert.assertEquals("C_1",c1.get("name").asText());
   }


    @Test
    public void rev_search_with_arraycond() throws Exception {
        /**
          This results in a query plan where U -> L, and the relationship L->U is defined with an array elemMatch query
             {array:x,elemMatch:{field:y,op:=,rfield:rf}}
          Once bound, this should be rewritten as:
             {field:localized(rf),op:=,rvalues:x.i.y}
          In other words, array elem match with array X and field Y is treated like a search on X.*.Y

         */
        Projection p=projection("[{'field':'*','recursive':1},{'field':'us','recursive':1}]");
        QueryExpression q=query("{'array':'us.*.authentications','elemMatch':{ '$and':[ { 'field':'principal','op':'$in','values':['a']}, {'field':'providerName','op':'$eq','rvalue':'p'} ] } }");
        GMD gmd=new GMD(p,null);
        CompositeMetadata md=CompositeMetadata.buildCompositeMetadata(getMd("composite/L.json"),gmd);
        
        Set<CompositeMetadata> minimalTree=new HashSet<>();
        minimalTree.add(md);
        minimalTree.add(md.getChildMetadata(new Path("us")));
        QueryPlan searchQP=new QueryPlanChooser(md,
                                                new BruteForceQueryPlanIterator(),
                                                new IndexedFieldScorer(),
                                                q,
                                                minimalTree).choose();
        QueryPlan retrievalQP=new QueryPlanChooser(md,
                                                   new First(),
                                                   new SimpleScorer(),
                                                   q,
                                                   minimalTree).choose();
        
        ExecutionPlan ep=new ExecutionPlan(q,p,null,md,searchQP,retrievalQP);
        ObjectNode j=(ObjectNode)ep.toJson();
        ObjectNode root=(ObjectNode)j.get("root");
        
        System.out.println("rev_search_with_arraycond");
        System.out.println(ep.toString());
        System.out.println("minimal tree:"+minimalTree);

        ArrayNode children=(ArrayNode)root.get("children");
        Assert.assertEquals(1,children.size());
        ObjectNode u1=(ObjectNode)children.get(0);
        Assert.assertEquals("U_1",u1.get("name").asText());
        children=(ArrayNode)u1.get("children");
        Assert.assertEquals(1,children.size());
        ObjectNode l0=(ObjectNode)children.get(0);
        Assert.assertEquals("L_0",l0.get("name").asText());
        children=(ArrayNode)l0.get("children");
        Assert.assertEquals(1,children.size());
        ObjectNode f=(ObjectNode)children.get(0);
        children=(ArrayNode)f.get("children");
        Assert.assertEquals(1,children.size());
        l0=(ObjectNode)children.get(0);
        Assert.assertEquals("L_0",l0.get("name").asText());
        children=(ArrayNode)l0.get("children");
        Assert.assertEquals(1,children.size());
        u1=(ObjectNode)children.get(0);
        Assert.assertEquals("U_1",u1.get("name").asText());
    }

}
