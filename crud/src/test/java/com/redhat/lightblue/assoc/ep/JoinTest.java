package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

import java.util.concurrent.Executors;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.assoc.*;


public class JoinTest {

    public static class TestStep extends Step<ResultDocument> {
        private List<ResultDocument> docs;

        public TestStep(List<ResultDocument> docs) {
            super(null);
            this.docs=docs;
        }

        public TestStep(ResultDocument...docs) {
            super(null);
            this.docs=Arrays.asList(docs);
        }
        
        public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
            return new StepResult<ResultDocument>() {
                public Stream<ResultDocument> stream() {
                    return docs.stream();
                }
            };
        }
    }

    private static ResultDocument resultDoc(String s) throws Exception {
        return new ResultDocument(null,new JsonDoc(JsonUtils.json(s.replaceAll("\'","\""))));
    }
    
    @Test
    public void join2Test() throws Exception {
        TestStep a=new TestStep(resultDoc("{'_id':1}"),
                                resultDoc("{'_id':2}"),
                                resultDoc("{'_id':3}"),
                                resultDoc("{'_id':4}"),
                                resultDoc("{'_id':5}"),
                                resultDoc("{'_id':6}"));
        TestStep b=new TestStep(resultDoc("{'_id':'a'}"),
                                resultDoc("{'_id':'b'}"),
                                resultDoc("{'_id':'c'}"),
                                resultDoc("{'_id':'d'}"));
        Join join=new Join(null,new TestStep[] {a,b});
        ExecutionContext ctx=new ExecutionContext(null,Executors.newSingleThreadExecutor());
        StepResult<List<ResultDocument>> result=join.getResults(ctx);
        Stream<List<ResultDocument>> stream=result.stream();
        stream.forEach(tuple->System.out.println(tuple));
        Iterator<List<ResultDocument>> itr=result.stream().iterator();
        for(int i:new int[] {1,2,3,4,5,6}) {
            for(String j:new String[] {"a","b","c","d"}) {
                List<ResultDocument> tp=itr.next();
                Assert.assertEquals(i,tp.get(0).getDoc().getRoot().get("_id").asInt());
                Assert.assertEquals(j,tp.get(1).getDoc().getRoot().get("_id").asText());
            }
        }
        Assert.assertFalse(itr.hasNext());
    }
    
    @Test
    public void join1Test() throws Exception {
        TestStep a=new TestStep(resultDoc("{'_id':1}"),
                                resultDoc("{'_id':2}"),
                                resultDoc("{'_id':3}"),
                                resultDoc("{'_id':4}"),
                                resultDoc("{'_id':5}"),
                                resultDoc("{'_id':6}"));
        Join join=new Join(null,new TestStep[] {a});
        ExecutionContext ctx=new ExecutionContext(null,Executors.newSingleThreadExecutor());
        StepResult<List<ResultDocument>> result=join.getResults(ctx);
        Stream<List<ResultDocument>> stream=result.stream();
        stream.forEach(tuple->System.out.println(tuple));
        Iterator<List<ResultDocument>> itr=result.stream().iterator();
        for(int i:new int[] {1,2,3,4,5,6}) {
            List<ResultDocument> tp=itr.next();
            Assert.assertEquals(i,tp.get(0).getDoc().getRoot().get("_id").asInt());
        }
        Assert.assertFalse(itr.hasNext());
    }
}

