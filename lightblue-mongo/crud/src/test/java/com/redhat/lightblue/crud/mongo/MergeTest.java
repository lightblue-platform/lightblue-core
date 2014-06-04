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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.mongodb.DBObject;
import com.redhat.lightblue.crud.MetadataResolver;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class MergeTest extends AbstractJsonSchemaTest {
    
    private EntityMetadata md;
    private Merge merge;
    private static JsonNodeFactory nodeFactory=JsonNodeFactory.withExactBigDecimals(true);

    private class Resolver implements MetadataResolver {
        EntityMetadata md;

        public Resolver(EntityMetadata md) {
            this.md=md;
        }
        
        @Override
        public EntityMetadata getEntityMetadata(String entityName) {
            return md;
        }
    }

    public EntityMetadata getMd(String fname) throws Exception {
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

    @Before
    public void init() throws Exception {
        md=getMd("./testMetadata.json");
        merge=new Merge(md);
    }

    @Test
    public void invisibleFieldsTest_nothing() throws Exception {
        JsonNode node=loadJsonNode("./testdata1.json");
        Translator t=new Translator(new Resolver(md),nodeFactory);
        DBObject doc=t.toBson(new JsonDoc(node));
        merge.findInvisibleFields(doc);
        Assert.assertTrue(merge.getInvisibleFields().isEmpty());
    }

    @Test
    public void invisibleFieldsTest_nonarray() throws Exception {
        JsonNode node=loadJsonNode("./testdata1.json");
        Translator t=new Translator(new Resolver(md),nodeFactory);
        DBObject doc=t.toBson(new JsonDoc(node));
        doc.put("inv1","val1");
        ((DBObject)doc.get("field6")).put("inv2","val2");
        merge.findInvisibleFields(doc);
        Assert.assertEquals(2,merge.getInvisibleFields().size());
        Assert.assertEquals("val1",get(merge.getInvisibleFields(),"inv1"));
        Assert.assertEquals("val2",get(merge.getInvisibleFields(),"field6.inv2"));
    }

    @Test
    public void invisibleFieldsTest_array() throws Exception {
        JsonNode node=loadJsonNode("./testdata1.json");
        Translator t=new Translator(new Resolver(md),nodeFactory);
        DBObject doc=t.toBson(new JsonDoc(node));
        ((DBObject)((List)doc.get("field7")).get(1)).put("inv1","val1");
        merge.findInvisibleFields(doc);
        Assert.assertEquals(1,merge.getInvisibleFields().size());
        Assert.assertEquals("val1",get(merge.getInvisibleFields(),"field7.1.inv1"));
    }

    @Test
    public void merge_simple() throws Exception {
        JsonNode node=loadJsonNode("./testdata1.json");
        Translator t=new Translator(new Resolver(md),nodeFactory);
        DBObject oldDoc=t.toBson(new JsonDoc(node));
        DBObject newDoc=t.toBson(new JsonDoc(node));
        oldDoc.put("inv1","val1");
        ((DBObject)oldDoc.get("field6")).put("inv2","val2");
        Assert.assertNull(newDoc.get("inv1"));
        Assert.assertNull(((DBObject)newDoc.get("field6")).get("inv2"));
        merge.merge(oldDoc,newDoc);
        Assert.assertEquals(oldDoc.get("inv1"),newDoc.get("inv1"));
        Assert.assertEquals(((DBObject)oldDoc.get("field6")).get("inv2"),((DBObject)newDoc.get("field6")).get("inv2"));
    }

    @Test
    public void merge_fail_simple() throws Exception {
        JsonNode node=loadJsonNode("./testdata1.json");
        Translator t=new Translator(new Resolver(md),nodeFactory);
        DBObject oldDoc=t.toBson(new JsonDoc(node));
        DBObject newDoc=t.toBson(new JsonDoc(node));
        oldDoc.put("inv1","val1");
        ((DBObject)oldDoc.get("field6")).put("inv2","val2");
        newDoc.removeField("field6");
        try {
            merge.merge(oldDoc,newDoc);
            Assert.fail();
        } catch (Error e) {
        }
    }

    private Object get(List<Merge.IField> list,String path) {
        Path p=new Path(path);
        for(Merge.IField f:list)
            if(f.getPath().equals(p))
                return f.getValue();
        return null;
    }
}
