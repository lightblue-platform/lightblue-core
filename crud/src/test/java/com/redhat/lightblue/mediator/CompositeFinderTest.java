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
package com.redhat.lightblue.mediator;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;

import com.redhat.lightblue.assoc.QueryPlan;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;

import com.redhat.lightblue.TestDataStoreParser;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.EntityVersion;

public class CompositeFinderTest extends AbstractJsonSchemaTest {

    private Mediator mediator;
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    private class TestMetadata extends DatabaseMetadata {
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            return getMd("composite/"+entityName+".json");
        }
    }

    private EntityMetadata getMd(String fname) {
        try {
            JsonNode node = loadJsonNode(fname);
            Extensions<JsonNode> extensions = new Extensions<>();
            extensions.addDefaultExtensions();
            extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
            TypeResolver resolver = new DefaultTypes();
            JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, nodeFactory);
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
            super(md,factory);
        }

        @Override
        protected OperationContext newCtx(Request request,Operation operation) {
            return ctx=super.newCtx(request,operation);
        }
    }

    private static OperationContext getLastContext(Mediator m) {
        return ((TestMediator)m).ctx;
    }

    @Before
    public void initMediator() throws Exception {
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        factory.addCRUDController("mongo", new TestCrudController(new TestCrudController.GetData() {
                public List<JsonDoc> getData(String entityName) {
                    try {
                        List<JsonDoc> docs=new ArrayList<JsonDoc>();
                        JsonNode node=loadJsonNode("composite/"+entityName+"_data.json");
                        if(node instanceof ArrayNode) {
                            for(Iterator<JsonNode> itr=((ArrayNode)node).elements();itr.hasNext();)
                                docs.add(new JsonDoc(itr.next()));
                        } else
                            docs.add(new JsonDoc(node));
                        return docs;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }));
        mediator = new TestMediator(new TestMetadata(), factory);
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replaceAll("\'","\"")));
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replaceAll("\'","\"")));
    }

   @Test
    public void sanityCheck() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'A01'}"));
        fr.setProjection(projection("{'field':'*','recursive':1}"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        Assert.assertEquals(1,response.getEntityData().size());
        Assert.assertEquals("A01",response.getEntityData().get(0).get("_id").asText());
    }

    @Test
    public void retrieveAandBonly() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'A01'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        Assert.assertEquals(1,response.getEntityData().size());
        Assert.assertEquals("A01",response.getEntityData().get(0).get("_id").asText());
        QueryPlan qplan=(QueryPlan)getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
        // This one must have A -> B
        Assert.assertEquals(1,qplan.getSources().length);
        Assert.assertEquals("A",qplan.getSources()[0].getMetadata().getName());
    }

    @Test
    public void retrieveAandBonly_BFirst() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'b.*.field1','op':'=','rvalue':'GpP8rweso'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        Assert.assertEquals(1,response.getEntityData().size());
        Assert.assertEquals("A09",response.getEntityData().get(0).get("_id").asText());
        QueryPlan qplan=(QueryPlan)getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
         // This one must have B -> A
        Assert.assertEquals(1,qplan.getSources().length);
        Assert.assertEquals("B",qplan.getSources()[0].getMetadata().getName());
   }

    @Test
    public void retrieveAandBonly_2q() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'$and': [ {'field':'_id','op':'=','rvalue':'A09'}, {'field':'b.*.field1','op':'=','rvalue':'GpP8rweso'} ] }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        Assert.assertEquals(1,response.getEntityData().size());
        Assert.assertEquals("A09",response.getEntityData().get(0).get("_id").asText());
        QueryPlan qplan=(QueryPlan)getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
        // This one must have B -> A
        Assert.assertEquals(1,qplan.getSources().length);
        Assert.assertEquals("B",qplan.getSources()[0].getMetadata().getName());
    }

    @Test
    public void retrieveNestedArrayRef() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'ADEEP'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref'}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        Assert.assertEquals(1,response.getEntityData().size());
        Assert.assertEquals("ADEEP",response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals("BDEEP",response.getEntityData().get(0).get("level1.r1.0.ref.0._id"));
    }

}
