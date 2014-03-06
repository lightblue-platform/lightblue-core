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
package com.redhat.lightblue.crud.mongo;

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;

import com.redhat.lightblue.crud.MetadataResolver;

import com.redhat.lightblue.query.UpdateExpression;

import com.redhat.lightblue.util.JsonUtils;

public class TranslatorUpdateTest extends AbstractJsonSchemaTest {

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(true);

    private Translator translator;

    @Before
    public void init() throws Exception {
        translator = new Translator(new MetadataResolver() {
            public EntityMetadata getEntityMetadata(String entityName) {
                try {
                    return getMd("./testMetadata.json");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, nodeFactory);
    }

    private EntityMetadata getMd(String fname) throws Exception {
        runValidJsonTest("json-schema/metadata/metadata.json", fname);
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new MongoDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    private JsonNode json(String s) throws Exception {
        return JsonUtils.json(s.replace('\'', '\"'));
    }

    private UpdateExpression update(String s) throws Exception {
        return UpdateExpression.fromJson(json(s));
    }

    @Test
    public void setTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$set': { 'field1':'blah', 'field2':'two'} }"));
        Assert.assertNotNull(obj);
        Assert.assertEquals("blah", ((DBObject) obj.get("$set")).get("field1"));
        Assert.assertEquals("two", ((DBObject) obj.get("$set")).get("field2"));
    }

    @Test
    public void nestedSetTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$set': { 'field6.nf1':'blah', 'field6.nf2':'two'} }"));
        Assert.assertNotNull(obj);
        Assert.assertEquals("blah", ((DBObject) obj.get("$set")).get("field6.nf1"));
        Assert.assertEquals("two", ((DBObject) obj.get("$set")).get("field6.nf2"));
    }

    @Test
    public void incTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$add': { 'field3':1, 'field4': -100} }"));
        Assert.assertNotNull(obj);
        Assert.assertEquals("1", ((DBObject) obj.get("$inc")).get("field3").toString());
        Assert.assertEquals("-100", ((DBObject) obj.get("$inc")).get("field4").toString());
    }

    @Test
    public void unsetTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        DBObject obj = translator.translate(md, update("{ '$unset': [ 'field3', 'field4'] }"));
        Assert.assertNotNull(obj);
        Assert.assertNotNull(((DBObject) obj.get("$unset")).get("field3"));
        Assert.assertNotNull(((DBObject) obj.get("$unset")).get("field4"));
    }

    @Test
    public void arrTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        try {
            DBObject obj = translator.translate(md, update("{ '$set': { 'field7.0.elemf1': 'blah'} }"));
            Assert.fail();
        } catch (CannotTranslateException e) {
        }
    }

    @Test
    public void arrTest2() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        try {
            DBObject obj = translator.translate(md, update("{ '$set': { 'field7': '$null'} }"));
            Assert.fail();
        } catch (CannotTranslateException e) {
        }
    }

    @Test
    public void objTest() throws Exception {
        EntityMetadata md = getMd("./testMetadata.json");
        try {
            DBObject obj = translator.translate(md, update("{ '$set': { 'field6': '$null'} }"));
            Assert.fail();
        } catch (CannotTranslateException e) {
        }
    }

}
