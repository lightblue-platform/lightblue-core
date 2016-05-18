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
package com.redhat.lightblue.metadata.parser;

import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.Hook;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.query.FieldProjection;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ExtensionsTest {

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    public static class TestHookCfg implements HookConfiguration {
        String testName;

        public TestHookCfg(String n) {
            testName = n;
        }
    }

    public static class HookTestCfgParser implements HookConfigurationParser<JsonNode> {
        @Override
        public HookConfiguration parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
            return new TestHookCfg(node.get("testField").asText());
        }

        @Override
        public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, HookConfiguration object) {
            p.putString(emptyNode, "testField", ((TestHookCfg) object).testName);
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public CRUDHook getCRUDHook() {
            return null;
        }

    }

    public static class TestDataStoreParser implements DataStoreParser<JsonNode> {
        @Override
        public DataStore parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
            return new DataStore() {
                @Override
                public String getBackend() {
                    return "test";
                }
            };
        }

        @Override
        public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, DataStore object) {
        }

        @Override
        public String getDefaultName() {
            return null;
        }
    }

    private JsonNode json(String s) throws Exception {
        return JsonUtils.json(s.replace('\'', '\"'));
    }

    @Test
    public void hookTest() throws Exception {
        Extensions<JsonNode> ex = new Extensions<>();
        HookTestCfgParser hookParser = new HookTestCfgParser();
        ex.registerHookConfigurationParser("testHook", hookParser);
        ex.registerDataStoreParser("test", new TestDataStoreParser());
        JsonNode mdJson = json("{'name':'test','datastore':{'backend':'test' }, "
                + "'hooks':[ "
                + "{'name':'testHook','actions':['insert'],"
                + "'projection':{'field':'*','recursive':1},"
                + "'configuration':{'testField':'testValue'} } ] }");
        JSONMetadataParser parser = new JSONMetadataParser(ex, new DefaultTypes(), nodeFactory);
        EntityInfo ei = parser.parseEntityInfo(mdJson);
        Assert.assertNotNull(ei);
        Assert.assertFalse(ei.getHooks().isEmpty());
        Assert.assertEquals("testHook", ei.getHooks().getHooks().get(0).getName());
        Assert.assertTrue(ei.getHooks().getHooks().get(0).isInsert());
        Assert.assertFalse(ei.getHooks().getHooks().get(0).isUpdate());
        Assert.assertFalse(ei.getHooks().getHooks().get(0).isDelete());
        Assert.assertFalse(ei.getHooks().getHooks().get(0).isFind());
        Assert.assertEquals("testValue", ((TestHookCfg) ei.getHooks().getHooks().get(0).getConfiguration()).testName);
    }

    @Test
    public void convertHookTest() throws Exception {
        Extensions<JsonNode> ex = new Extensions<>();
        HookTestCfgParser hookParser = new HookTestCfgParser();
        ex.registerHookConfigurationParser("testHook", hookParser);
        ex.registerDataStoreParser("test", new TestDataStoreParser());

        EntityInfo ei = new EntityInfo("test");
        ArrayList<Hook> hooks = new ArrayList<>();
        Hook hook = new Hook("testHook");
        hook.setInsert(true);
        hook.setProjection(new FieldProjection(new Path("*"), true, true));
        hook.setConfiguration(new TestHookCfg("test"));
        hooks.add(hook);
        ei.getHooks().setHooks(hooks);

        JSONMetadataParser parser = new JSONMetadataParser(ex, new DefaultTypes(), nodeFactory);
        JsonNode node = parser.convert(ei);
        System.out.println(node);
        JSONAssert.assertEquals(node.toString(), json("{'name':'test','datastore':{'backend':'test' }, "
                + "'hooks':[ "
                + "{'name':'testHook','actions':['insert'],"
                + "'projection':{'field':'*','include':true,'recursive':true},"
                + "'configuration':{'testField':'test'} } ] }").toString(), false);

    }
}
