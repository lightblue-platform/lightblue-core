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

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;
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


    @Test
    public void simpleFieldTest() throws Exception {
        JsonDoc doc=getDoc("./sample1.json");
        EntityMetadata md=getMd("./testMetadata.json");

        UpdateExpression expr=new UpdateExpressionList(
            new SetExpression(UpdateOperator._set, new FieldAndRValue(new Path("field1"),
                                                                      new RValueExpression(new Value("set1")))),
            new SetExpression(UpdateOperator._set, new FieldAndRValue(new Path("field2"),
                                                                      new RValueExpression(new Value("set2")))),
            new SetExpression(UpdateOperator._add, new FieldAndRValue(new Path("field3"),
                                                                      new RValueExpression(new Value(new Integer(1))))));
        
        Updater updater=Updater.getInstance(factory,md,expr);
        Assert.assertTrue(updater.update(doc));
        Assert.assertEquals("set1",doc.get(new Path("field1")).asText());
        Assert.assertEquals("set2",doc.get(new Path("field2")).asText());
        Assert.assertEquals(4,doc.get(new Path("field3")).asInt());
    }
}
