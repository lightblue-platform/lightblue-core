package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.junit.Test;
import org.junit.Assert;

import org.skyscreamer.jsonassert.JSONAssert;

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
       ExecutionPlan ep=new ExecutionPlan(p,null,null,null,md,null,searchQP);
       ObjectNode j=(ObjectNode)ep.toJson();

       System.out.println("retrieveAandBonly");
       System.out.println(JsonUtils.prettyPrint(j));
       System.out.println("minimal tree:"+minimalTree);

       JsonNode source=j.get("source");
       JsonNode assemble1=source.get("assemble");
       Assert.assertEquals("A",assemble1.get("entity").asText());
       JsonNode left1=assemble1.get("left");
       Assert.assertNotNull(left1.get("search"));
       
       JsonNode right1=assemble1.get("right");
       JsonNode assemble2=right1.get(0).get("assemble");
       Assert.assertEquals("B",assemble2.get("entity").asText());
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
       QueryPlan retrievalQP=new QueryPlanChooser(md,new First(),new SimpleScorer(),null,minimalTree).choose();
       ExecutionPlan ep=new ExecutionPlan(p,null,null,null,md,searchQP,retrievalQP);
       ObjectNode j=(ObjectNode)ep.toJson();
       
       System.out.println("retrieveABC");
       System.out.println(JsonUtils.prettyPrint(j));
       System.out.println("minimal tree:"+minimalTree);

       JsonNode source=j.get("source");
       JsonNode assemble1=source.get("assemble");
       Assert.assertEquals("A",assemble1.get("entity").asText());
       JsonNode left1=assemble1.get("left");
       Assert.assertNotNull(left1.get("copy"));
       JsonNode right1=assemble1.get("right");
       Assert.assertEquals(2,right1.size());
       JsonNode assemble2=right1.get(0).get("assemble");
       JsonNode assemble3=right1.get(1).get("assemble");
       Assert.assertTrue(assemble2.get("entity").asText().equals("C")||assemble3.get("entity").asText().equals("C"));
       Assert.assertTrue(assemble2.get("entity").asText().equals("B")||assemble3.get("entity").asText().equals("B"));
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
       
       ExecutionPlan ep=new ExecutionPlan(p,null,null,null,md,searchQP,retrievalQP);
       ObjectNode j=(ObjectNode)ep.toJson();
       
       System.out.println("retrieveAandConly_CFirst");
       System.out.println(JsonUtils.prettyPrint(j));
       System.out.println("minimal tree:"+minimalTree);

       JsonNode source=j.get("source");
       JsonNode assemble1=source.get("assemble");
       Assert.assertEquals("A",assemble1.get("entity").asText());
       
       JsonNode left1=assemble1.get("left");
       JsonNode copy=left1.get("copy");
       JsonNode source2=copy.get("source");
       JsonNode jsearch=source2.get("join-search");
       JsonNode join=jsearch.get("join");
       Assert.assertEquals(1,join.size());
       Assert.assertEquals("C",join.get(0).get("entity").asText());
              
       JsonNode right1=assemble1.get("right");
       Assert.assertEquals(1,right1.size());
       Assert.assertEquals("C",right1.get(0).get("assemble").get("entity").asText());
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
        
        ExecutionPlan ep=new ExecutionPlan(p,null,null,null,md,searchQP,retrievalQP);
        ObjectNode j=(ObjectNode)ep.toJson();
        
        System.out.println("rev_search_with_arraycond");
        System.out.println(JsonUtils.prettyPrint(j));
        System.out.println("minimal tree:"+minimalTree);

        JsonNode source=j.get("source");
        Assert.assertEquals("L",source.get("assemble").get("entity").asText());
        Assert.assertEquals("U",source.get("assemble").get("left").get("copy").get("source").get("join-search").get("join").get(0).get("entity").asText());
        Assert.assertEquals(1,source.get("assemble").get("right").size());
    }

}
