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
package com.redhat.lightblue.metadata;

import java.util.List;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.types.UIDType;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.StringType;
import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.parser.MetadataParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.DataStoreParser;

import com.redhat.lightblue.metadata.constraints.RequiredConstraint;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;

public class IdentityFieldsTest {

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    @Test
    public void userTest1() throws Exception {
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new DataStoreParser<JsonNode>() {
            public String getDefaultName() {
                return "mongo";
            }

            public DataStore parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
                return new DataStore() {
                    public String getBackend() {
                        return "mongo";
                    }
                };
            }

            public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, DataStore object) {
            }
        });
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), nodeFactory);

        JsonNode node = JsonUtils.json(getClass().getResourceAsStream("/usermd.json"));
        EntityMetadata md = parser.parseEntityMetadata(node);

        Field[] idf=md.getEntitySchema().getIdentityFields();
        Assert.assertEquals(1,idf.length);
        Assert.assertEquals("_id",idf[0].getName());
    }

    @Test
    public void userTest2() throws Exception {
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new DataStoreParser<JsonNode>() {
            public String getDefaultName() {
                return "mongo";
            }

            public DataStore parse(String name, MetadataParser<JsonNode> p, JsonNode node) {
                return new DataStore() {
                    public String getBackend() {
                        return "mongo";
                    }
                };
            }

            public void convert(MetadataParser<JsonNode> p, JsonNode emptyNode, DataStore object) {
            }
        });
        JSONMetadataParser parser = new JSONMetadataParser(extensions, new DefaultTypes(), nodeFactory);

        JsonNode node = JsonUtils.json(getClass().getResourceAsStream("/usermdidf.json"));
        EntityMetadata md = parser.parseEntityMetadata(node);

        Field[] idf=md.getEntitySchema().getIdentityFields();
        Assert.assertEquals(2,idf.length);
        Assert.assertEquals("_id",idf[0].getName());
        Assert.assertEquals("contactPermissions.allowEmailContact",idf[1].getFullPath().toString());
    }
}
