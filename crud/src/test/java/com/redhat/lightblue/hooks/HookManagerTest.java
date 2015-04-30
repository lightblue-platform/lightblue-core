/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.hooks;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.Hook;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.TestDataStoreParser;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.FieldProjection;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDOperation;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class HookManagerTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    public static class TestOperationContext extends CRUDOperationContext {
        EntityMetadata md;

        public TestOperationContext(EntityMetadata md,
                                    CRUDOperation op,
                                    Factory f,
                                    List<JsonDoc> docs) {
            super(op, "test", f, docs);
            this.md = md;
            if (CRUDOperation.UPDATE.equals(op) || CRUDOperation.DELETE.equals(op)) {
                // for update and delete setup the original document so pre isn't null in hooks
                for (DocCtx dc : getDocuments()) {
                    dc.startModifications();
                }
            }
        }

        @Override
        public EntityMetadata getEntityMetadata(String name) {
            return md;
        }
    }

    public static abstract class AbstractHook implements CRUDHook {
        private final String name;
        EntityMetadata md;
        HookConfiguration cfg;
        List<HookDoc> processed;

        public AbstractHook(String n) {
            name = n;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void processHook(EntityMetadata md,
                                HookConfiguration cfg,
                                List<HookDoc> processedDocuments) {
            this.md = md;
            this.cfg = cfg;
            this.processed = processedDocuments;
        }
    }

    public static class TestHook1Config implements HookConfiguration {
    }

    public static class TestHook1 extends AbstractHook {
        public TestHook1() {
            super("hook1");
        }
    }

    public static class TestHook2Config implements HookConfiguration {
    }

    public static class TestHook2 extends AbstractHook {
        public TestHook2() {
            super("hook2");
        }
    }

    public static class TestMediatorHookConfig implements HookConfiguration {
    }

    public static class TestMediatorHook extends AbstractHook implements MediatorHook {
        public TestMediatorHook() {
            super("MH");
        }
    }

    private TestHook1 hook1;
    private TestHook2 hook2;
    private TestMediatorHook mediatorHook;

    private HookResolver resolver;

    public static class TestHookResolver implements HookResolver {
        Map<String, CRUDHook> map = new HashMap<>();

        public TestHookResolver(CRUDHook... h) {
            for (CRUDHook x : h) {
                map.put(x.getName(), x);
            }
        }

        @Override
        public CRUDHook getHook(String name) {
            return map.get(name);
        }
    }

    @Before
    public void setup() {
        hook1 = new TestHook1();
        hook2 = new TestHook2();
        mediatorHook = new TestMediatorHook();
        resolver = new TestHookResolver(hook1, hook2, mediatorHook);
    }

    private EntityMetadata getMD(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
        TypeResolver typeResolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, typeResolver, nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    private void addHook(EntityMetadata md,
                         String name,
                         Projection projection,
                         HookConfiguration config,
                         String... actions) {
        Hook hook = new Hook(name);
        hook.setProjection(projection);
        hook.setConfiguration(config);
        for (String a : actions) {
            switch (a) {
                case "insert":
                    hook.setInsert(true);
                    break;
                case "update":
                    hook.setUpdate(true);
                    break;
                case "find":
                    hook.setFind(true);
                    break;
                case "delete":
                    hook.setDelete(true);
                    break;
            }
        }
        List<Hook> l = md.getHooks().getHooks();
        l.add(hook);
        md.getHooks().setHooks(l);
    }

    private List<JsonDoc> getSomeDocs(int n) throws Exception {
        List<JsonDoc> ret = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            JsonNode node = loadJsonNode("./sample1.json");
            JsonDoc doc = new JsonDoc(node);
            doc.modify(new Path("field1"), nodeFactory.textNode("field" + i), false);
            ret.add(doc);
        }
        return ret;
    }

    private TestOperationContext setupContext(CRUDOperation op) throws Exception {
        EntityMetadata md = getMD("./testMetadata.json");
        addHook(md, "hook1", null, new TestHook1Config(), "insert", "update");
        addHook(md, "hook2", null, new TestHook2Config(), "find", "delete", "update");
        addHook(md, "MH", null, new TestMediatorHookConfig(), "insert", "update", "delete");
        TestOperationContext ctx = new TestOperationContext(md,
                op,
                new Factory(),
                getSomeDocs(10));
        for (DocCtx doc : ctx.getDocuments()) {
            doc.setCRUDOperationPerformed(op);
        }
        return ctx;
    }

    @Test
    public void crudInsertQueueTest() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.INSERT);

        // Only hook1 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertEquals(ctx.md, hook1.md);
        Assert.assertTrue(hook1.cfg instanceof TestHook1Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook1.processed.size());
        Assert.assertNull(hook2.md);
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void crudUpdateQueueTest() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.UPDATE);

        // hook1 and hook2 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertEquals(ctx.md, hook1.md);
        Assert.assertTrue(hook1.cfg instanceof TestHook1Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook1.processed.size());
        for (HookDoc doc : hook1.processed) {
            Assert.assertNotNull(doc.getPreDoc());
            Assert.assertNotNull(doc.getPostDoc());
            Assert.assertEquals(CRUDOperation.UPDATE, doc.getCRUDOperation());
        }

        Assert.assertEquals(ctx.md, hook2.md);
        Assert.assertTrue(hook2.cfg instanceof TestHook2Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook2.processed.size());
        Assert.assertNull(mediatorHook.md);
        for (HookDoc doc : hook2.processed) {
            Assert.assertNotNull(doc.getPreDoc());
            Assert.assertNotNull(doc.getPostDoc());
            Assert.assertEquals(CRUDOperation.UPDATE, doc.getCRUDOperation());
        }
    }

    @Test
    public void crudDeleteQueueTest() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.DELETE);

        //  hook2 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertNull(hook1.md);
        Assert.assertEquals(ctx.md, hook2.md);
        Assert.assertTrue(hook2.cfg instanceof TestHook2Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook2.processed.size());
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void crudFindQueueTest() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.FIND);

        //  hook2 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertNull(hook1.md);
        Assert.assertEquals(ctx.md, hook2.md);
        Assert.assertTrue(hook2.cfg instanceof TestHook2Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook2.processed.size());
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void crudMixedQueueTest() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.FIND);
        ctx.getDocuments().get(0).setCRUDOperationPerformed(CRUDOperation.INSERT);
        ctx.getDocuments().get(1).setCRUDOperationPerformed(CRUDOperation.UPDATE);
        ctx.getDocuments().get(2).setCRUDOperationPerformed(CRUDOperation.DELETE);

        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertEquals(ctx.md, hook1.md);
        Assert.assertTrue(hook1.cfg instanceof TestHook1Config);
        Assert.assertEquals(2, hook1.processed.size());

        Assert.assertEquals(ctx.md, hook2.md);
        Assert.assertTrue(hook2.cfg instanceof TestHook2Config);
        Assert.assertEquals(9, hook2.processed.size());

        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void mediatorMixedQueueTest() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.FIND);
        ctx.getDocuments().get(0).setCRUDOperationPerformed(CRUDOperation.INSERT);
        ctx.getDocuments().get(1).setCRUDOperationPerformed(CRUDOperation.UPDATE);
        ctx.getDocuments().get(2).setCRUDOperationPerformed(CRUDOperation.DELETE);

        hooks.queueMediatorHooks(ctx);
        hooks.callQueuedHooks();
        Assert.assertNull(hook1.md);
        Assert.assertNull(hook2.md);
        Assert.assertEquals(ctx.md, mediatorHook.md);
        Assert.assertTrue(mediatorHook.cfg instanceof TestMediatorHookConfig);
        Assert.assertEquals(3, mediatorHook.processed.size());

    }

    @Test
    public void projectionTestInsert() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.INSERT);
        // Add projection to one of the hooks
        for (Hook h : ctx.md.getHooks().getHooks()) {
            if (h.getName().equals("hook1")) {
                h.setProjection(new FieldProjection(new Path("field1"), true, false));
            }
        }

        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        // hook1 only should have field1 projected
        Assert.assertEquals(ctx.md, hook1.md);
        Assert.assertTrue(hook1.cfg instanceof TestHook1Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook1.processed.size());
        for (HookDoc h : hook1.processed) {
            Assert.assertNull(h.getPreDoc());
            Assert.assertTrue(h.getPostDoc().get(new Path("field1")) != null);
            Assert.assertTrue(h.getPostDoc().get(new Path("field2")) == null);
        }
    }

    @Test
    public void projectionTestUpdate() throws Exception {
        HookManager hooks = new HookManager(resolver, nodeFactory);
        TestOperationContext ctx = setupContext(CRUDOperation.UPDATE);
        // Add projection to one of the hooks
        for (Hook h : ctx.md.getHooks().getHooks()) {
            if (h.getName().equals("hook1")) {
                h.setProjection(new FieldProjection(new Path("field1"), true, false));
            }
        }

        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        // hook1 only should have field1 projected
        Assert.assertEquals(ctx.md, hook1.md);
        Assert.assertTrue(hook1.cfg instanceof TestHook1Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook1.processed.size());
        for (HookDoc h : hook1.processed) {
            Assert.assertNotNull(h.getPreDoc());
            Assert.assertTrue(h.getPostDoc().get(new Path("field1")) != null);
            Assert.assertTrue(h.getPostDoc().get(new Path("field2")) == null);
        }

        // hook2 should have field1 and others
        Assert.assertEquals(ctx.md, hook2.md);
        Assert.assertTrue(hook2.cfg instanceof TestHook2Config);
        Assert.assertEquals(ctx.getDocuments().size(), hook2.processed.size());
        for (HookDoc h : hook2.processed) {
            Assert.assertNotNull(h.getPreDoc());
            Assert.assertTrue(h.getPostDoc().get(new Path("field1")) != null);
            Assert.assertTrue(h.getPostDoc().get(new Path("field2")) != null);
        }
    }

}
