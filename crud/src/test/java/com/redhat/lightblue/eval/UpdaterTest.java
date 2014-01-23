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

import org.junit.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.test.AbstractJsonNodeTest;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.JSONMetadataParser;

import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.Extensions;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;


public class UpdaterTest extends AbstractJsonNodeTest {

    private static final JsonNodeFactory factory = JsonNodeFactory.withExactBigDecimals(true);
    
    private JsonDoc getDoc(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        return new JsonDoc(node);
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<JsonNode>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, factory);
        return parser.parseEntityMetadata(node);
    }

    private UpdateExpression json(String s) throws Exception {
        return UpdateExpression.fromJson(JsonUtils.json(s.replace('\'','\"')));
    }

    @Test
    public void setSimpleFieldTest() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");

        UpdateExpression expr=json("[ {'$set' : { 'field1' : 'set1', 'field2':'set2', 'field5': 0, 'field6.nf1':'set6' } }, {'$add' : { 'field3':1 } } ] ");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals("set1",doc.get(new Path("field1")).asText());
        Assert.assertEquals("set2",doc.get(new Path("field2")).asText());
        Assert.assertEquals(4,doc.get(new Path("field3")).asInt());
        Assert.assertFalse(doc.get(new Path("field5")).asBoolean());
        Assert.assertEquals("set6",doc.get(new Path("field6.nf1")).asText());
    }

    @Test
    public void setArrayFieldTest() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");

        UpdateExpression expr=json("{'$set' : { 'field6.nf5.0':'50', 'field6.nf6.1':'blah', 'field7.0.elemf1':'test'}} ");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals(50,doc.get(new Path("field6.nf5.0")).intValue());
        Assert.assertEquals("blah",doc.get(new Path("field6.nf6.1")).asText());
        Assert.assertEquals("test",doc.get(new Path("field7.0.elemf1")).asText());
    }

    @Test
    public void refSet() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");

        UpdateExpression expr=json("{'$set' : { 'field6.nf5.0': { '$valueof' : 'field3' }, 'field7.0' : {}}}");
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc,md.getFieldTreeRoot(),new Path()));
        Assert.assertEquals(doc.get(new Path("field3")).intValue(),doc.get(new Path("field6.nf5.0")).intValue());
        JsonNode node=doc.get(new Path("field7.0"));
        Assert.assertNotNull(node);
        Assert.assertEquals(0,node.size());
        Assert.assertTrue(node instanceof ObjectNode);
    }
}
