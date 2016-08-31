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
package com.redhat.lightblue.eval;

import java.util.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.eval.SortFieldInfo;
import com.redhat.lightblue.eval.SortableItem;
import com.redhat.lightblue.TestDataStoreParser;

public class SortTest extends AbstractJsonNodeTest {

    private static JsonNode json(String q) {
        try {
            return JsonUtils.json(q.replace('\'', '\"'));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, JsonNodeFactory.instance);
        return parser.parseEntityMetadata(node);
    }

    private Sort sort(String s) throws Exception {
        return Sort.fromJson(JsonUtils.json(s.replace('\'', '\"')));
    }

    @Test
    public void sortTest() throws Exception {
        EntityMetadata md = getMd("usermd.json");
        // Create some docs
        List<JsonNode> docs = new ArrayList<>();
        // Each doc will have _ids: 0,1,2,3,4,5,6,7,8,9
        // Sites: [ 00,10,20,30,40,50]
        //        [ 01,11,21,31,41,51]
        //        [ 02,12,22,32,42,52] ...
        //
        // So, ascending sort by siteid should match _id order
        //     descending sort by siteid should match reverse _id order
        for (int i = 0; i < 9; i++) {
            ObjectNode root = JsonNodeFactory.instance.objectNode();
            root.set("_id", JsonNodeFactory.instance.textNode("" + i));
            ArrayNode sites = JsonNodeFactory.instance.arrayNode();
            root.set("sites", sites);
            for (int s = 0; s < 5; s++) {
                ObjectNode site = JsonNodeFactory.instance.objectNode();
                site.set("siteId", JsonNodeFactory.instance.textNode("" + s + i));
                sites.add(site);
            }
            docs.add(root);
        }

        Sort sort = sort("{'sites.*.siteId':'$asc'}");
        SortFieldInfo[] si = SortFieldInfo.buildSortFields(sort, md);
        Assert.assertEquals(1, si.length);
        List<SortableItem> list = new ArrayList<>();
        for (JsonNode doc : docs) {
            list.add(new SortableItem(doc, si));
        }

        Collections.sort(list);

        String last = null;
        for (SortableItem doc : list) {
            String id = doc.getNode().get("_id").asText();
            if (last == null) {
                last = id;
            } else {
                Assert.assertTrue(id.compareTo(last) > 0);
            }
        }

        sort = sort("{'sites.*.siteId':'$desc'}");
        si = SortFieldInfo.buildSortFields(sort, md);
        Assert.assertEquals(1, si.length);
        list = new ArrayList<>();
        for (JsonNode doc : docs) {
            list.add(new SortableItem(doc, si));
        }

        Collections.sort(list);

        last = null;
        for (SortableItem doc : list) {
            String id = doc.getNode().get("_id").asText();
            if (last == null) {
                last = id;
            } else {
                Assert.assertTrue(id.compareTo(last) < 0);
            }
        }

    }

}
