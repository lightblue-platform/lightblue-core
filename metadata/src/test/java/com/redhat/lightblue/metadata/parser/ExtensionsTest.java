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

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.HookConfiguration;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityInfo;

import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.util.JsonUtils;

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
        }

    }

    public static class TestDatastoreParser implements DataStoreParser<JsonNode> {
        @Override
        public DataStore parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
            return new DataStore() {
                @Override
                public String getType() {
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
        ex.registerDataStoreParser("test", new TestDatastoreParser());
        JsonNode mdJson = json("{'name':'test','datastore':{'test': { } }, "
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
}
