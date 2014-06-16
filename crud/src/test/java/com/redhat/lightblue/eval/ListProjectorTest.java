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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;
import com.redhat.lightblue.TestBackendParser;

public class ListProjectorTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);

    private EntityMetadata md;
    private JsonDoc doc;

    private static JsonNode json(String q) {
        try {
            return JsonUtils.json(q.replace('\'', '\"'));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerBackendParser("mongo", new TestBackendParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private JsonDoc getDoc(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        return new JsonDoc(node);
    }

    private Projector projector(String str, EntityMetadata md) {
        Projection p = Projection.fromJson(json(str));
        return Projector.getInstance(p, md);
    }

    @Before
    public void setUp() throws Exception {
        md = getMd("./testMetadata.json");
        doc = getDoc("./sample1.json");
    }

    @Test
    public void projection_list() throws Exception {
        String pr = "[{'field':'field6.*','include':1},{'field':'field5'}]";
        Projector projector = projector(pr, md);
        JsonNode expectedNode = JsonUtils.json("{'field5':true,'field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{},'nf8':[],'nf9':[],'nf10':[]}}".replace('\'', '\"'));

        JsonDoc newDoc = projector.project(doc, factory);

        Assert.assertEquals(expectedNode.toString(), newDoc.toString());
    }

    @Test
    public void one_$parent_projection_list() throws Exception {
        String pr = "[{'field':'field7.$parent.field6.*','include':1},{'field':'field5'}]";
        Projector projector = projector(pr, md);
        JsonNode expectedNode = JsonUtils.json("{'field5':true,'field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{},'nf8':[],'nf9':[],'nf10':[]}}".replace('\'', '\"'));

        JsonDoc newDoc = projector.project(doc, factory);

        Assert.assertEquals(expectedNode.toString(), newDoc.toString());
    }

    @Test
    public void two_$parent_projection_list() throws Exception {
        String pr = "[{'field':'field7.$parent.field6.*','include':1},{'field':'field5'}]";
        Projector projector = projector(pr, md);
        JsonNode expectedNode = JsonUtils.json("{'field5':true,'field6':{'nf1':'nvalue1','nf2':'nvalue2','nf3':4,'nf4':false,'nf5':[],'nf6':[],'nf7':{},'nf8':[],'nf9':[],'nf10':[]}}".replace('\'', '\"'));

        JsonDoc newDoc = projector.project(doc, factory);

        Assert.assertEquals(expectedNode.toString(), newDoc.toString());
    }

}
