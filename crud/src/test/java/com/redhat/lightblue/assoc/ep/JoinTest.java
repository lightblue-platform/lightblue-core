package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashSet;

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
import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.*;

import com.redhat.lightblue.query.*;
import com.redhat.lightblue.crud.*;
import com.redhat.lightblue.assoc.*;
import com.redhat.lightblue.assoc.scorers.*;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import com.redhat.lightblue.TestDataStoreParser;

public class JoinTest extends AbstractJsonSchemaTest {

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
        public JsonNode explain(ExecutionContext ctx) {
            return null;
        }
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    private static ResultDocument resultDoc(ExecutionBlock block, String s) throws Exception {
        return new ResultDocument(block, new JsonDoc(JsonUtils.json(s.replaceAll("\'", "\""))));
    }

    @Test
    public void join2Test_1parent_1child() throws Exception {
        CompositeMetadata amd = getCmd("A", projection("[{'field':'obj1.c'},{'field':'obj1.c.*.b'}]"));
        CompositeMetadata cmd = amd.getChildMetadata(new Path("obj1.c"));
        CompositeMetadata bmd = cmd.getChildMetadata(new Path("obj1.c.*.b"));
        Assert.assertNotNull(bmd);
        Assert.assertNotNull(cmd);
        QueryPlan qp = new QueryPlan(amd, new SimpleScorer());
        ExecutionBlock ablock = new ExecutionBlock(amd, qp.getNode(amd));
        ExecutionBlock bblock = new ExecutionBlock(amd, qp.getNode(bmd));
        ExecutionBlock cblock = new ExecutionBlock(amd, qp.getNode(cmd));
        cblock.setAssociationQuery(ablock, new AssociationQuery(amd, cmd, (ResolvedReferenceField) amd.resolve(new Path("obj1.c")),
                new ArrayList<Conjunct>()));
        cblock.setAssociationQuery(bblock, new AssociationQuery(amd, cmd, (ResolvedReferenceField) cmd.resolve(new Path("b")),
                new ArrayList<Conjunct>()));
        // Metadata: a -> c -> b
        // QueryPlan: a ->c
        //             b->c
        qp.flip(qp.getNode(bmd), qp.getNode(cmd));
        cblock.addSourceBlock(ablock);
        cblock.addSourceBlock(bblock);

        ablock.linkBlocks();
        bblock.linkBlocks();
        cblock.linkBlocks();

        TestStep a = new TestStep(ablock,
                resultDoc(ablock, "{'_id':1}"),
                resultDoc(ablock, "{'_id':2}"),
                resultDoc(ablock, "{'_id':3}"),
                resultDoc(ablock, "{'_id':4}"),
                resultDoc(ablock, "{'_id':5}"),
                resultDoc(ablock, "{'_id':6}"));
        TestStep b = new TestStep(bblock,
                resultDoc(bblock, "{'_id':'a'}"),
                resultDoc(bblock, "{'_id':'b'}"),
                resultDoc(bblock, "{'_id':'c'}"),
                resultDoc(bblock, "{'_id':'d'}"));
        Join join = new Join(cblock, new Source[]{new Source<>(a), new Source<>(b)});
        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1"));
        OperationContext opctx = new OperationContext(freq,
                null,
                new Factory(),
                CRUDOperation.FIND,
                null,
                null,
                new HashSet<String>(),
                null);
        ExecutionContext ctx = new ExecutionContext(opctx, Executors.newSingleThreadExecutor());
        StepResult<JoinTuple> result = join.getResults(ctx);
        Stream<JoinTuple> stream = result.stream();
        stream.forEach(tuple -> System.out.println(tuple));
        Iterator<JoinTuple> itr = result.stream().iterator();
        for (int i : new int[]{1, 2, 3, 4, 5, 6}) {
            for (String j : new String[]{"a", "b", "c", "d"}) {
                JoinTuple tp = itr.next();
                Assert.assertEquals(i, tp.getParentDocument().getDoc().getRoot().get("_id").asInt());
                Assert.assertEquals("obj1", tp.getParentDocumentSlot().getLocalContainerName().toString());
                Assert.assertEquals(j, tp.getChildTuple().get(0).getDoc().getRoot().get("_id").asText());
            }
        }
        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void join1Test() throws Exception {
        CompositeMetadata amd = getCmd("A", projection("[{'field':'obj1.c'}]"));
        CompositeMetadata cmd = amd.getChildMetadata(new Path("obj1.c"));
        QueryPlan qp = new QueryPlan(amd, new SimpleScorer());
        ExecutionBlock ablock = new ExecutionBlock(amd, qp.getNode(amd));
        ExecutionBlock cblock = new ExecutionBlock(amd, qp.getNode(cmd));
        cblock.setAssociationQuery(ablock, new AssociationQuery(amd, cmd, (ResolvedReferenceField) amd.resolve(new Path("obj1.c")),
                new ArrayList<Conjunct>()));
        /// a -> c
        cblock.addSourceBlock(ablock);
        ablock.linkBlocks();
        cblock.linkBlocks();
        TestStep a = new TestStep(ablock,
                resultDoc(ablock, "{'_id':1}"),
                resultDoc(ablock, "{'_id':2}"),
                resultDoc(ablock, "{'_id':3}"),
                resultDoc(ablock, "{'_id':4}"),
                resultDoc(ablock, "{'_id':5}"),
                resultDoc(ablock, "{'_id':6}"));
        Join join = new Join(cblock, new Source[]{new Source<>(a)});
        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1"));
        OperationContext opctx = new OperationContext(freq,
                null,
                new Factory(),
                CRUDOperation.FIND,
                null,
                null,
                new HashSet<String>(),
                null);
        ExecutionContext ctx = new ExecutionContext(opctx, Executors.newSingleThreadExecutor());
        StepResult<JoinTuple> result = join.getResults(ctx);
        Stream<JoinTuple> stream = result.stream();
        stream.forEach(tuple -> System.out.println(tuple));
        Iterator<JoinTuple> itr = result.stream().iterator();
        for (int i : new int[]{1, 2, 3, 4, 5, 6}) {
            JoinTuple tp = itr.next();
            Assert.assertEquals(i, tp.getParentDocument().getDoc().getRoot().get("_id").asInt());
        }
        Assert.assertFalse(itr.hasNext());
    }
}
