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
import java.util.HashSet;

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

import com.redhat.lightblue.query.Projection;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;

import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class HooksTest extends AbstractJsonNodeTest  {

    private static JsonNodeFactory nodeFactory=JsonNodeFactory.withExactBigDecimals(false);

    public static class OpCtx extends CRUDOperationContext {
        EntityMetadata md;

        public OpCtx(EntityMetadata md,
                     Operation op,
                     Factory f,
                     List<JsonDoc> docs) {
            super(op,"test",f,nodeFactory,new HashSet<String>(),docs);
            this.md=md;
        }
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
            name=n;
        }

        public String getName() {
            return name;
        }

        public void processHook(EntityMetadata md,
                                HookConfiguration cfg,
                                List<HookDoc> processedDocuments) {
            this.md=md;
            this.cfg=cfg;
            this.processed=processedDocuments;
        }
    }

    public static class CH1Config implements HookConfiguration {}
    public static class CH1 extends AbstractHook {
        public CH1() {
            super("hook1");
        }
    }


    public static class CH2Config implements HookConfiguration {}
    public static class CH2 extends AbstractHook {
        public CH2() {
            super("hook2");
        }
    }

    public static class MHConfig implements HookConfiguration {}
    public static class MH extends AbstractHook implements MediatorHook {
        public MH() {
            super("MH");
        }
    }

    private CH1 hook1;
    private CH2 hook2;
    private MH mediatorHook;

    private HookResolver resolver;

    public static class Resolver implements HookResolver {
        Map<String,CRUDHook> map=new HashMap<>();

        public Resolver(CRUDHook... h) {
            for(CRUDHook x:h)
                map.put(x.getName(),x);
        }

        public CRUDHook getHook(String name) {
            return map.get(name);
        }
    }

    @Before
    public void setup() {
        hook1=new CH1();
        hook2=new CH2();
        mediatorHook=new MH();
        resolver=new Resolver(hook1,hook2,mediatorHook);
    }

    private EntityMetadata getMD(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    private void addHook(EntityMetadata md,
                         String name,
                         Projection projection,
                         HookConfiguration config,
                         String... actions) {
        Hook hook=new Hook(name);
        hook.setProjection(projection);
        hook.setConfiguration(config);
        for(String a:actions)
            if("insert".equals(a))
                hook.setInsert(true);
            else if("update".equals(a))
                hook.setUpdate(true);
            else if("find".equals(a))
                hook.setFind(true);
            else if("delete".equals(a))
                hook.setDelete(true);
        List<Hook> l=md.getHooks().getHooks();
        l.add(hook);
        md.getHooks().setHooks(l);
    }

    private List<JsonDoc> getSomeDocs(int n) throws Exception {
        List<JsonDoc> ret=new ArrayList<>();
        for(int i=0;i<n;i++) {
            JsonNode node=loadJsonNode("./sample1.json");
            JsonDoc doc=new JsonDoc(node);
            doc.modify(new Path("field1"),nodeFactory.textNode("field"+i),false);
            ret.add(doc);
        }
        return ret;
    }

    private OpCtx setupContext(Operation op) throws Exception {
        EntityMetadata md=getMD("./testMetadata.json");
        addHook(md,"hook1",null,new CH1Config(),"insert","update");
        addHook(md,"hook2",null,new CH2Config(),"find","delete","update");
        addHook(md,"MH",null,new MHConfig(),"insert","update","delete");
        OpCtx ctx=new OpCtx(md,
                            op,
                            new Factory(),
                            getSomeDocs(10));
        for(DocCtx doc:ctx.getDocuments())
            doc.setOperationPerformed(op);
        return ctx;
    }

    @Test
    public void crudInsertQueueTest() throws Exception {
        Hooks hooks=new Hooks(resolver, nodeFactory);
        OpCtx ctx=setupContext(Operation.INSERT);
        
        // Only hook1 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertEquals(ctx.md,hook1.md);
        Assert.assertTrue(hook1.cfg instanceof CH1Config);
        Assert.assertEquals(ctx.getDocuments().size(),hook1.processed.size());
        Assert.assertNull(hook2.md);
        Assert.assertNull(mediatorHook.md);
    }
    
    @Test
    public void crudUpdateQueueTest() throws Exception {
        Hooks hooks=new Hooks(resolver, nodeFactory);
        OpCtx ctx=setupContext(Operation.UPDATE);
        
        // hook1 and hook2 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertEquals(ctx.md,hook1.md);
        Assert.assertTrue(hook1.cfg instanceof CH1Config);
        Assert.assertEquals(ctx.getDocuments().size(),hook1.processed.size());
        Assert.assertEquals(ctx.md,hook2.md);
        Assert.assertTrue(hook2.cfg instanceof CH2Config);
        Assert.assertEquals(ctx.getDocuments().size(),hook2.processed.size());
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void crudDeleteQueueTest() throws Exception {
        Hooks hooks=new Hooks(resolver, nodeFactory);
        OpCtx ctx=setupContext(Operation.DELETE);
        
        //  hook2 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertNull(hook1.md);
        Assert.assertEquals(ctx.md,hook2.md);
        Assert.assertTrue(hook2.cfg instanceof CH2Config);
        Assert.assertEquals(ctx.getDocuments().size(),hook2.processed.size());
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void crudFindQueueTest() throws Exception {
        Hooks hooks=new Hooks(resolver, nodeFactory);
        OpCtx ctx=setupContext(Operation.FIND);
        
        //  hook2 should be called
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertNull(hook1.md);
        Assert.assertEquals(ctx.md,hook2.md);
        Assert.assertTrue(hook2.cfg instanceof CH2Config);
        Assert.assertEquals(ctx.getDocuments().size(),hook2.processed.size());
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void crudMixedQueueTest() throws Exception {
        Hooks hooks=new Hooks(resolver, nodeFactory);
        OpCtx ctx=setupContext(Operation.FIND);
        ctx.getDocuments().get(0).setOperationPerformed(Operation.INSERT);
        ctx.getDocuments().get(1).setOperationPerformed(Operation.UPDATE);
        ctx.getDocuments().get(2).setOperationPerformed(Operation.DELETE);
        
        hooks.queueHooks(ctx);
        hooks.callQueuedHooks();

        Assert.assertEquals(ctx.md,hook1.md);
        Assert.assertTrue(hook1.cfg instanceof CH1Config);
        Assert.assertEquals(2,hook1.processed.size());


        Assert.assertEquals(ctx.md,hook2.md);
        Assert.assertTrue(hook2.cfg instanceof CH2Config);
        Assert.assertEquals(9,hook2.processed.size());
        
        Assert.assertNull(mediatorHook.md);
    }

    @Test
    public void mediatorMixedQueueTest() throws Exception {
        Hooks hooks=new Hooks(resolver, nodeFactory);
        OpCtx ctx=setupContext(Operation.FIND);
        ctx.getDocuments().get(0).setOperationPerformed(Operation.INSERT);
        ctx.getDocuments().get(1).setOperationPerformed(Operation.UPDATE);
        ctx.getDocuments().get(2).setOperationPerformed(Operation.DELETE);
        
        hooks.queueMediatorHooks(ctx);
        hooks.callQueuedHooks();
        Assert.assertNull(hook1.md);
        Assert.assertNull(hook2.md);
        Assert.assertEquals(ctx.md,mediatorHook.md);
        Assert.assertTrue(mediatorHook.cfg instanceof MHConfig);
        Assert.assertEquals(3,mediatorHook.processed.size());

    }
}
