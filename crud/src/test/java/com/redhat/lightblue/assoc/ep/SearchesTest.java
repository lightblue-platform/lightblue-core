package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import java.util.concurrent.Executors;

import java.util.stream.Stream;

import org.junit.Test;
import org.junit.Assert;

import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.query.*;
import com.redhat.lightblue.assoc.*;
import com.redhat.lightblue.assoc.scorers.*;
import com.redhat.lightblue.assoc.iterators.*;
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import com.redhat.lightblue.TestDataStoreParser;

public class SearchesTest extends AbstractJsonSchemaTest {

    private class TestMetadata extends DatabaseMetadata {
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            return getMd(entityName);
        }
    }

    private EntityMetadata getMd(String fname) {
        try {
            JsonNode node = loadJsonNode("composite/" + fname + ".json");
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

    private CompositeMetadata getCmd(String fname, Projection p) {
        EntityMetadata md = getMd(fname);
        return CompositeMetadata.buildCompositeMetadata(md, new GMD(p, null));
    }

    private class GMD extends AbstractGetMetadata {
        public GMD(Projection p, QueryExpression q) {
            super(p, q);
        }

        @Override
        protected EntityMetadata retrieveMetadata(Path injectionField,
                                                  String entityName,
                                                  String version) {
            try {
                return getMd(entityName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class TestStep extends Step<ResultDocument> {
        private List<ResultDocument> docs;

        public TestStep(ExecutionBlock block, List<ResultDocument> docs) {
            super(block);
            this.docs = docs;
        }

        public TestStep(ExecutionBlock block, ResultDocument... docs) {
            super(block);
            this.docs = Arrays.asList(docs);
        }

        public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
            return new StepResult<ResultDocument>() {
                public Stream<ResultDocument> stream() {
                    return docs.stream();
                }
            };
        }

        public JsonNode toJson() {
            return null;
        }
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    private static ResultDocument resultDoc(ExecutionBlock block, String s) throws Exception {
        return new ResultDocument(block, new JsonDoc(JsonUtils.json(s.replaceAll("\'", "\""))));
    }

    @Test
    public void writeChildQueriesTest() throws Exception {
        CompositeMetadata amd = getCmd("A", projection("[{'field':'obj1.c'},{'field':'obj1.c.*.b'}]"));
        CompositeMetadata cmd = amd.getChildMetadata(new Path("obj1.c"));
        CompositeMetadata bmd = cmd.getChildMetadata(new Path("obj1.c.*.b"));
        QueryPlanChooser chooser = new QueryPlanChooser(amd, new First(),
                new SimpleScorer(), query("{'field':'_id','op':'=','rvalue':1}"),
                null);
        QueryPlan qp = chooser.choose();
        ExecutionBlock ablock = new ExecutionBlock(amd, qp.getNode(amd));
        ExecutionBlock bblock = new ExecutionBlock(amd, qp.getNode(bmd));
        ExecutionBlock cblock = new ExecutionBlock(amd, qp.getNode(cmd));
        QueryPlanData data = qp.getEdgeData(qp.getNode(amd), qp.getNode(cmd));
        AssociationQuery a1 = new AssociationQuery(amd, cmd, data.getReference(), data.getConjuncts());
        data = qp.getEdgeData(qp.getNode(cmd), qp.getNode(bmd));
        AssociationQuery a2 = new AssociationQuery(amd, cmd, data.getReference(), data.getConjuncts());
        cblock.setAssociationQuery(ablock, a1);
        cblock.setAssociationQuery(bblock, a2);
        // Metadata: a -> c -> b
        // QueryPlan: a ->c
        //             b->c
        qp.flip(qp.getNode(bmd), qp.getNode(cmd));
        cblock.addSourceBlock(ablock);
        cblock.addSourceBlock(bblock);

        ablock.linkBlocks();
        bblock.linkBlocks();
        cblock.linkBlocks();

        Map<ChildSlot, QueryExpression> map = Searches.
                writeChildQueriesFromParentDoc(a1, resultDoc(ablock, "{'id':'a','obj1':{'c_ref':'ref'}}"));
        System.out.println(map);
        Assert.assertEquals(1, map.size());
        ChildSlot slot = map.keySet().iterator().next();
        Assert.assertEquals("obj1", slot.getLocalContainerName().toString());
        Assert.assertEquals("c", slot.getReferenceFieldName());
        JSONAssert.assertEquals("{field:_id,op:$eq,rvalue:ref}", map.values().iterator().next().toString(), false);

    }

    @Test
    public void writeChildQueriesTest_parentArray() throws Exception {
        CompositeMetadata umd = getCmd("U", projection("[{'field':'legalEntities.*.legalEntity'}]"));
        CompositeMetadata lmd = umd.getChildMetadata(new Path("legalEntities.*.legalEntity"));
        QueryPlanChooser chooser = new QueryPlanChooser(umd, new First(),
                new SimpleScorer(), query("{'field':'_id','op':'=','rvalue':1}"),
                null);
        QueryPlan qp = chooser.choose();
        ExecutionBlock ublock = new ExecutionBlock(umd, qp.getNode(umd));
        ExecutionBlock lblock = new ExecutionBlock(umd, qp.getNode(lmd));
        QueryPlanData data = qp.getEdgeData(qp.getNode(umd), qp.getNode(lmd));
        AssociationQuery a1 = new AssociationQuery(umd, lmd, data.getReference(), data.getConjuncts());
        lblock.setAssociationQuery(ublock, a1);

        lblock.addSourceBlock(ublock);

        ublock.linkBlocks();
        lblock.linkBlocks();

        Map<ChildSlot, QueryExpression> map = Searches.
                writeChildQueriesFromParentDoc(a1, resultDoc(ublock, "{'_id':1,'legalEntities':[{'legalEntityId':1,'title':'a'},{'legalEntityId':2,'title':'b'}]}"));
        System.out.println(map);
        Assert.assertEquals(2, map.size());
        ChildSlot slot = findSlot(map, "legalEntities.0");
        Assert.assertNotNull(slot);
        JSONAssert.assertEquals("{field:_id,op:$eq,rvalue:1}", map.get(slot).toString(), false);
        slot = findSlot(map, "legalEntities.1");
        Assert.assertNotNull(slot);
        JSONAssert.assertEquals("{field:_id,op:$eq,rvalue:2}", map.get(slot).toString(), false);
    }

    @Test
    public void writeQueriesForJoin1() throws Exception {
        CompositeMetadata umd = getCmd("U", projection("[{'field':'legalEntities.*.legalEntity'}]"));
        CompositeMetadata lmd = umd.getChildMetadata(new Path("legalEntities.*.legalEntity"));
        QueryPlanChooser chooser = new QueryPlanChooser(umd, new First(),
                new SimpleScorer(), query("{'field':'_id','op':'=','rvalue':1}"),
                null);
        QueryPlan qp = chooser.choose();
        // Flip it to L -> U
        qp.flip(qp.getNode(umd), qp.getNode(lmd));

        ExecutionBlock ublock = new ExecutionBlock(umd, qp.getNode(umd));
        ExecutionBlock lblock = new ExecutionBlock(umd, qp.getNode(lmd));
        QueryPlanData data = qp.getEdgeData(qp.getNode(umd), qp.getNode(lmd));
        AssociationQuery a1 = new AssociationQuery(umd, umd, data.getReference(), data.getConjuncts());
        ublock.setAssociationQuery(lblock, a1);

        ublock.addSourceBlock(lblock);

        ublock.linkBlocks();
        lblock.linkBlocks();

        ArrayList<ResultDocument> l = new ArrayList<>();
        l.add(resultDoc(lblock, "{'_id':1,'name':'a'}"));
        JoinTuple tuple = new JoinTuple(null, null, l);
        List<QueryExpression> queries = Searches.writeQueriesForJoinTuple(tuple, ublock);
        System.out.println(queries);
        Assert.assertEquals(1, queries.size());
        JSONAssert.assertEquals("{field:legalEntities.*.legalEntityId,op:$eq,rvalue:1}", queries.get(0).toString(), false);
    }

    @Test
    public void writeQueriesForJoin2() throws Exception {
        CompositeMetadata umd = getCmd("U", projection("[{'field':'legalEntities.*.legalEntity'},{'field':'legalEntities.*.legalEntity.*.us'}]"));
        CompositeMetadata lmd = umd.getChildMetadata(new Path("legalEntities.*.legalEntity"));
        CompositeMetadata u2md = lmd.getChildMetadata(new Path("legalEntities.*.legalEntity.*.us"));
        QueryPlanChooser chooser = new QueryPlanChooser(umd, new First(),
                new SimpleScorer(), query("{'field':'_id','op':'=','rvalue':1}"),
                null);
        QueryPlan qp = chooser.choose();
        // setup:
        //    U -> L
        //    U2 -> L
        qp.flip(qp.getNode(u2md), qp.getNode(lmd));

        ExecutionBlock ublock = new ExecutionBlock(umd, qp.getNode(umd));
        ExecutionBlock u2block = new ExecutionBlock(umd, qp.getNode(u2md));
        ExecutionBlock lblock = new ExecutionBlock(umd, qp.getNode(lmd));

        QueryPlanData data = qp.getEdgeData(qp.getNode(umd), qp.getNode(lmd));
        AssociationQuery a1 = new AssociationQuery(umd, lmd, data.getReference(), data.getConjuncts());
        lblock.setAssociationQuery(ublock, a1);
        data = qp.getEdgeData(qp.getNode(u2md), qp.getNode(lmd));
        AssociationQuery a2 = new AssociationQuery(u2md, lmd, data.getReference(), data.getConjuncts());
        lblock.setAssociationQuery(u2block, a2);

        lblock.addSourceBlock(ublock);
        lblock.addSourceBlock(u2block);

        ublock.linkBlocks();
        u2block.linkBlocks();
        lblock.linkBlocks();

        ResultDocument parentDoc = resultDoc(ublock, "{'_id':1,'legalEntities':[{'legalEntityId':1},{'legalEntityId':2}]}");
        ArrayList<ResultDocument> ulist = new ArrayList<>();
        ulist.add(resultDoc(u2block, "{'_id':2,'legalEntities':[{'legalEntityId':3},{'legalEntityId':4}]}"));
        JoinTuple tuple = new JoinTuple(parentDoc, parentDoc.getSlots().get(a1.getReference()).get(0), ulist);
        List<QueryExpression> queries = Searches.writeQueriesForJoinTuple(tuple, lblock);
        System.out.println(queries);
        Assert.assertEquals(2, queries.size());
        JSONAssert.assertEquals("{$and:[{field:_id,op:$eq,rvalue:1},{field:_id,op:$eq,rvalue:3}]}",
                queries.get(0).toString(), false);
        JSONAssert.assertEquals("{$and:[{field:_id,op:$eq,rvalue:1},{field:_id,op:$eq,rvalue:4}]}",
                queries.get(1).toString(), false);
    }

    private ChildSlot findSlot(Map<ChildSlot, QueryExpression> map, String container) {
        Path c = new Path(container);
        for (ChildSlot s : map.keySet()) {
            if (s.getLocalContainerName().equals(c)) {
                return s;
            }
        }
        return null;
    }
}
