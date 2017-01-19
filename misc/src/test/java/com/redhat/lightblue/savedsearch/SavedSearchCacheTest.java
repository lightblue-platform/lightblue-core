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
package com.redhat.lightblue.savedsearch;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;
import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;

import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.mediator.OperationContext;

import com.redhat.lightblue.crud.*;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.Request;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.TestDataStoreParser;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class SavedSearchCacheTest extends AbstractJsonSchemaTest {
    private Mediator mediator;

    private class TestMetadata extends DatabaseMetadata {
        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            return getMd(entityName + ".json");
        }
    }

    private EntityMetadata getMd(String fname) {
        try {
            JsonNode node = loadJsonNode(fname);
            Extensions<JsonNode> extensions = new Extensions<>();
            extensions.addDefaultExtensions();
            extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
            TypeResolver resolver = new DefaultTypes();
            JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, JsonNodeFactory.instance);
            EntityMetadata md = parser.parseEntityMetadata(node);
            PredefinedFields.ensurePredefinedFields(md);
            return md;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final class TestMediator extends Mediator {
        OperationContext ctx;

        public TestMediator(Metadata md,
                            Factory factory) {
            super(md, factory);
        }

        @Override
        protected OperationContext newCtx(Request request, CRUDOperation CRUDOperation) {
            return ctx = super.newCtx(request, CRUDOperation);
        }
    }

    private static OperationContext getLastContext(Mediator m) {
        return ((TestMediator) m).ctx;
    }

    List<JsonDoc> foundDocs=null;
    
    private class TestCrudController implements CRUDController {
        
        @Override
        public CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                            Projection projection) {
            return null;
        }
        
        @Override
        public CRUDSaveResponse save(CRUDOperationContext ctx,
                                     boolean upsert,
                                     Projection projection) {
            return null;
        }
        @Override
        public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                         QueryExpression query,
                                         UpdateExpression update,
                                         Projection projection) {
            return new CRUDUpdateResponse();
        }

        @Override
        public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                         QueryExpression query) {

            return new CRUDDeleteResponse();
        }

        @Override
        public CRUDFindResponse find(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     Projection projection,
                                     Sort sort,
                                     Long from,
                                     Long to) {
            CRUDFindResponse r=new CRUDFindResponse();
            if(foundDocs!=null) {
                r.setSize(foundDocs.size());
                foundDocs.stream().forEach(d->ctx.addDocument(d));
            }
            return r;
        }

        @Override
        public void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc) {}
        @Override public MetadataListener getMetadataListener() {return null;}
    }

    @Before
    public void initMediator() throws Exception {
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        factory.addCRUDController("mongo", new TestCrudController());
        mediator = new TestMediator(new TestMetadata(), factory);
    }

    @Test
    public void findNoneTest() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        Assert.assertNull(cache.getSavedSearch(mediator,null,"testSearch","test","1.0"));
    }

    @Test
    public void findOneTest_with_version() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","1.0.0"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","1.0.0");
        Assert.assertTrue(doc instanceof ObjectNode);
    }

    // Find the search with entity version 1.0.0, there is one search with no version so that should be used for all versions
    @Test
    public void findOneTest_with_version_null_test_ver() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","1.0.0");
        Assert.assertTrue(doc instanceof ObjectNode);
    }

    // Find the search with entity version 1.0.0, there are two searches, one without version, one with 1.0.0. Return the 1.0.0 version
    @Test
    public void findOneTest_two_searches() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}"));
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","1.0.0"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","1.0.0");
        Assert.assertTrue(doc instanceof ObjectNode);
        Assert.assertEquals("1.0.0",doc.get("versions").get(0).asText());
    }

    // Find the search with entity version 1.0.0, there are two searches, one without version, one with 1.0. Return the 1.0.0 version
    @Test
    public void findOneTest_two_searches_partial1() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}"));
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","1.0"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","1.0.0");
        Assert.assertTrue(doc instanceof ObjectNode);
        Assert.assertEquals("1.0",doc.get("versions").get(0).asText());
    }

    // Find the search with entity version 1.0.0, there are two searches, one without version, one with 1. Return the 1 version
    @Test
    public void findOneTest_two_searches_partial2() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}"));
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","1"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","1.0.0");
        Assert.assertTrue(doc instanceof ObjectNode);
        Assert.assertEquals("1",doc.get("versions").get(0).asText());
    }

    // Find the search with entity version 2.0.0, there are two searches, one with version 3, one with 1. Return none
    @Test
    public void findOneTest_two_searches_partial_none() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","3"));
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","1"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","2.0.0");
        Assert.assertNull(doc);
    }

    // Find the search with entity version 2.0.0, there are three searches, one with version 3, one with null, one with 1. Return null search
    @Test
    public void findOneTest_two_searches_partial_default() throws Exception {
        SavedSearchCache cache=new SavedSearchCache(null);
        foundDocs=new ArrayList<JsonDoc>();
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","3"));
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}","1"));
        foundDocs.add(makeSearch("testSearch","test","{\"field\":\"a\",\"op\":\"=\",\"rvalue\":1}"));
        JsonNode doc=cache.getSavedSearch(mediator,null,"testSearch","test","2.0.0");
        Assert.assertEquals(0,doc.get("versions").size());
    }

    private JsonDoc makeSearch(String searchName,String entity,String query, String...versions) {
        JsonDoc doc=new JsonDoc(JsonNodeFactory.instance.objectNode());
        doc.modify(new Path("name"),JsonNodeFactory.instance.textNode(searchName),true);
        doc.modify(new Path("entity"),JsonNodeFactory.instance.textNode(entity),true);
        doc.modify(new Path("versions"),makeArray(versions),true);
        doc.modify(new Path("query"),JsonNodeFactory.instance.textNode(query),true);
        return doc;
    }

    private JsonNode makeArray(String...versions) {
        ArrayNode node=JsonNodeFactory.instance.arrayNode();
        for(String x:versions)
            if(x==null)
                node.add(JsonNodeFactory.instance.nullNode());
            else
                node.add(JsonNodeFactory.instance.textNode(x));
        return node;
    }
}
