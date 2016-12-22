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

import static org.junit.Assert.assertTrue;

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
import com.redhat.lightblue.crud.CRUDOperation;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.ExplainQuerySupport;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;

import com.redhat.lightblue.assoc.QueryPlan;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.query.ValueComparisonExpression;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.TestDataStoreParser;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.EntityVersion;

public class CompositeFinderTest extends AbstractJsonSchemaTest {

    private Mediator mediator;
    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    // CRUDController returns findError for retrieval of errorEntity
    private static Error findError;
    private static String errorEntity;

    private QueryExpression updateQuery;

    private class TestMetadata extends DatabaseMetadata {
        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            return getMd("composite/" + entityName + ".json");
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

    private class CompositeTestCrudController extends TestCrudController implements ExplainQuerySupport {
        public CompositeTestCrudController(TestCrudController.GetData gd) {
            super(gd);
        }

        @Override
        public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                         QueryExpression query,
                                         UpdateExpression update,
                                         Projection projection) {
            updateQuery = query;
            return new CRUDUpdateResponse();
        }

        @Override
        public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                         QueryExpression query) {

            updateQuery = query;
            return new CRUDDeleteResponse();
        }

        @Override
        public CRUDFindResponse find(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     Projection projection,
                                     Sort sort,
                                     Long from,
                                     Long to) {
            if (findError != null && ctx.getEntityName().equals(errorEntity)) {
                ctx.addError(findError);
                return new CRUDFindResponse();
            } else {
                return super.find(ctx, query, projection, sort, from, to);
            }
        }
        @Override
        public void explain(CRUDOperationContext ctx,
                            QueryExpression query,
                            Projection projection,
                            Sort sort,
                            Long from,
                            Long to,
                            JsonDoc destDoc) {
            destDoc.modify(new Path("testController"),JsonNodeFactory.instance.textNode("test"),true);
        }
    }

    @Before
    public void initMediator() throws Exception {
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        factory.addCRUDController("mongo", new CompositeTestCrudController(new TestCrudController.GetData() {
            public List<JsonDoc> getData(String entityName) {
                try {
                    List<JsonDoc> docs = new ArrayList<>();
                    JsonNode node = loadJsonNode("composite/" + entityName + "_data.json");
                    if (node instanceof ArrayNode) {
                        for (Iterator<JsonNode> itr = ((ArrayNode) node).elements(); itr.hasNext();) {
                            docs.add(new JsonDoc(itr.next()));
                        }
                    } else {
                        docs.add(new JsonDoc(node));
                    }
                    return docs;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
        mediator = new TestMediator(new TestMetadata(), factory);
        updateQuery = null;
        findError = null;
        errorEntity = null;
    }

    private QueryExpression query(String s) throws Exception {
        return QueryExpression.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    private Projection projection(String s) throws Exception {
        return Projection.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    private Sort sort(String s) throws Exception {
        return Sort.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    private UpdateExpression update(String s) throws Exception {
        return UpdateExpression.fromJson(JsonUtils.json(s.replaceAll("\'", "\"")));
    }

    @Test
    public void sanityCheck() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'A01'}"));
        fr.setProjection(projection("{'field':'*','recursive':1}"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("A01", response.getEntityData().get(0).get("_id").asText());
    }

    @Test
    public void findSelfReference_Invalid() throws Exception{
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'base_images.*._id','op':'=','rvalue':'1'}"));
        fr.setEntityVersion(new EntityVersion("self_ref_err", "1.0.0"));
        Response response = mediator.find(fr);
        assertTrue(response.getErrors().get(0).getErrorCode().equals(CrudConstants.ERR_METADATA_APPEARS_TWICE));
    }

    @Test
    public void findSelfReference() throws Exception{
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'1'}"));
        fr.setEntityVersion(new EntityVersion("self_ref", "1.0.0"));
        Response response = mediator.find(fr);
        assertTrue(response.getErrors().isEmpty());

        fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'1'}"));
        fr.setEntityVersion(new EntityVersion("self_ref_default", "1.0.0"));
        response = mediator.find(fr);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    public void retrieveAandBonly() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'A01'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("A01", response.getEntityData().get(0).get("_id").asText());
        QueryPlan qplan = (QueryPlan) getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
        // This one must have A -> B
        Assert.assertEquals(1, qplan.getSources().length);
        Assert.assertEquals("A", qplan.getSources()[0].getMetadata().getName());
    }

    @Test
    public void retrieveAandB_noproject() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'obj1.c.*._id','op':'=','rvalue':'CDEEP2'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("ADEEP2", response.getEntityData().get(0).get("_id").asText());
    }


    public void retrieveAandBonly_defaultVersion() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'A01'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A_def", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("A01", response.getEntityData().get(0).get("_id").asText());
        QueryPlan qplan = (QueryPlan) getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
        // This one must have A -> B
        Assert.assertEquals(1, qplan.getSources().length);
        Assert.assertEquals("A_def", qplan.getSources()[0].getMetadata().getName());
    }

    @Test
    public void retrieveAandBonly_manyA() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'$in','values':['A01','A02','A03']}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setSort(sort("{'_id':'$asc'}"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(3, response.getEntityData().size());
        Assert.assertEquals("A01", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals(1, response.getEntityData().get(0).get("b").size());
        Assert.assertEquals("A02", response.getEntityData().get(1).get("_id").asText());
        Assert.assertEquals(1, response.getEntityData().get(1).get("b").size());
        Assert.assertEquals("A03", response.getEntityData().get(2).get("_id").asText());
        Assert.assertEquals(1, response.getEntityData().get(2).get("b").size());
    }

    @Test
    public void retrieveAandBonly_manyA_nullproj() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'$in','values':['A01','A02','A03']}"));
        fr.setSort(sort("{'_id':'$asc'}"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        System.out.println("Null proj:" + response);
        Assert.assertEquals(3, response.getEntityData().size());
    }

    @Test
    public void retrieveAandBonly_manyA_noq() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(null);
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setSort(sort("{'_id':'$asc'}"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);

        fr.setQuery(query("{'field':'objectType','op':'=','rvalue':'A'}"));
        Response response2 = mediator.find(fr);

        Assert.assertEquals(response2.getEntityData().size(), response.getEntityData().size());
    }

    @Test
    public void retrieveAandBonly_manyB() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'$in','values':['MANYB1','MANYB2']}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'nonid_b'}]"));
        fr.setSort(sort("{'_id':'$asc'}"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(2, response.getEntityData().size());
        Assert.assertEquals(2, response.getMatchCount());
        Assert.assertEquals("MANYB1", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals(2, response.getEntityData().get(0).get("nonid_b").size());
        Assert.assertEquals("MANYB2", response.getEntityData().get(1).get("_id").asText());
        Assert.assertEquals(2, response.getEntityData().get(1).get("nonid_b").size());
    }

    @Test
    public void retrieveAandBonly_manyB_range() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'$in','values':['MANYB1','MANYB2']}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'nonid_b'}]"));
        fr.setSort(sort("{'_id':'$asc'}"));
        fr.setFrom(0l);
        fr.setTo(0l);
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("MANYB1", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals(2, response.getEntityData().get(0).get("nonid_b").size());
    }

    @Test
    public void retrieveAandBonly_manyB_range_incorrect_count() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'$in','values':['MANYB1','MANYB2']}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'nonid_b'}]"));
        fr.setSort(sort("{'_id':'$asc'}"));
        fr.setFrom(0l);
        fr.setTo(0l);
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(2, response.getMatchCount());
    }

    @Test
    public void retrieveAandConly_CFirst() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'obj1.c.*.field1','op':'=','rvalue':'ABFPwrjyx-o5DQWWZmSEfKf3W1z'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'obj1.c'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("A09", response.getEntityData().get(0).get("_id").asText());
        System.out.println(response.getEntityData().get(0));
        QueryPlan qplan = (QueryPlan) getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
        // This one must have C -> A
        Assert.assertEquals(1, qplan.getSources().length);
        Assert.assertEquals("C", qplan.getSources()[0].getMetadata().getName());
    }

    @Test
    public void retrieveAandConly_CFirst_range() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'obj1.c.*.objectType','op':'=','rvalue':'C'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'obj1.c'}]"));
        fr.setFrom(0l);
        fr.setTo(0l);
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
    }

    @Test
    public void retrieveAandConly_CFirst_range_explain() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'obj1.c.*.objectType','op':'=','rvalue':'C'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'obj1.c'}]"));
        fr.setFrom(0l);
        fr.setTo(0l);
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.explain(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        JsonNode doc=response.getEntityData().get(0);
        // Make sure explain descends all the way to the  controller
        Assert.assertTrue(doc.toString().indexOf("testController")!=-1);
    }

    @Test
    public void retrieveAandBonly_2q() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'$and': [ {'field':'_id','op':'=','rvalue':'A09'}, {'field':'b.*.field1','op':'=','rvalue':'GpP8rweso'} ] }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("A09", response.getEntityData().get(0).get("_id").asText());
        QueryPlan qplan = (QueryPlan) getLastContext(mediator).getProperty(Mediator.CTX_QPLAN);
        // This one must have B -> A
        Assert.assertEquals(1, qplan.getSources().length);
        Assert.assertEquals("B", qplan.getSources()[0].getMetadata().getName());
    }

    @Test
    public void retrieveOneAndNotOther() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'ADEEP'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'b'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("ADEEP", response.getEntityData().get(0).get("_id").asText());
        Assert.assertNull(response.getEntityData().get(0).get("level1").get("arr1").get(0).get("ref"));
    }

    @Test
    public void retrieveNestedArrayRef() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'ADEEP'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));

        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("ADEEP", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals("BDEEP1", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.0.ref.0._id")).asText());
        Assert.assertEquals("BDEEP2", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.1.ref.0._id")).asText());
    }

    @Test
    public void emptyAssocArrays_396() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'ADEEP'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref','match':{'field':'field1','op':'=','rvalue':'nothing'}}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));

        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertNull(JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.0.ref")));
    }

    @Test
    public void assocTest2_365() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'A.*.objectType','op':'=','rvalue':'jA'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'A.*','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("jB", "1.0.1-SNAPSHOT"));

        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(2, response.getEntityData().size());
    }

    @Test
    public void assocTestProjection_369() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'A.*.objectType','op':'=','rvalue':'jA'}"));
        fr.setProjection(projection("[{'field':'A.*','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("jB", "1.0.1-SNAPSHOT"));

        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(2, response.getEntityData().size());
        Assert.assertNull(JsonDoc.get(response.getEntityData().get(0), new Path("_id")));
    }

    @Test
    public void retrieveNestedArrayRef_reversed() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'level1.arr1.*.ref.*.field1','op':'=','rvalue':'bdeep1'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("ADEEP", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals("BDEEP1", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.0.ref.0._id")).asText());
        Assert.assertEquals("BDEEP2", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.1.ref.0._id")).asText());
    }

    @Test
    public void retrieveNestedArrayRef_reversed_elemMatch() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'level1.arr1', 'elemMatch': {'field':'ref.*.field1','op':'=','rvalue':'bdeep1'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("ADEEP", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals("BDEEP1", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.0.ref.0._id")).asText());
        Assert.assertEquals("BDEEP2", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.1.ref.0._id")).asText());
    }

    @Test
    public void retrieveNestedArrayRef_reversed_elemMatchAtRef() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'level1.arr1.*.ref', 'elemMatch': {'field':'field1','op':'=','rvalue':'bdeep1'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref'}]"));
        fr.setEntityVersion(new EntityVersion("A", "1.0.0"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("ADEEP", response.getEntityData().get(0).get("_id").asText());
        Assert.assertEquals("BDEEP1", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.0.ref.0._id")).asText());
        Assert.assertEquals("BDEEP2", JsonDoc.get(response.getEntityData().get(0), new Path("level1.arr1.1.ref.0._id")).asText());
    }

    @Test
    public void assocRetrievalWithElemMatch() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'code1','op':'=','rvalue':'A'}"));
        fr.setProjection(projection("{'field':'relationships.*','recursive':1}"));
        fr.setEntityVersion(new EntityVersion("parent_w_elem", "1.0.0"));

        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
   }

    @Test
    public void assocRetrievalWithElemMatch_0range() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'code1','op':'=','rvalue':'A'}"));
        fr.setProjection(projection("{'field':'relationships.*','recursive':1}"));
        fr.setEntityVersion(new EntityVersion("parent_w_elem", "1.0.0"));

        fr.setFrom(0l);
        fr.setTo(-1l);
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
   }

    @Test
    public void deepRoles() throws Exception {
        DefaultMetadataResolver r = new DefaultMetadataResolver(new TestMetadata());
        r.initialize("parent_w_elem_w_roles", "1.0.0", query("{'field':'code1','op':'=','rvalue':'A'}"), projection("{'field':'relationships.*','recursive':1}"));

        System.out.println(r.getMetadataRoles());
        Assert.assertTrue(r.getMetadataRoles().contains("a"));
        Assert.assertTrue(r.getMetadataRoles().contains("b"));
        Assert.assertTrue(r.getMetadataRoles().contains("c"));
        Assert.assertTrue(r.getMetadataRoles().contains("d"));
        Assert.assertTrue(r.getMetadataRoles().contains("e"));
        Assert.assertTrue(r.getMetadataRoles().contains("f"));
        Assert.assertTrue(r.getMetadataRoles().contains("g"));
        Assert.assertTrue(r.getMetadataRoles().contains("h"));
    }

    @Test
    public void dontReturnTooDeep() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'code1','op':'=','rvalue':'A'}"));
        fr.setProjection(projection("{'field':'relationships.*','recursive':1}"));
        fr.setEntityVersion(new EntityVersion("parent_w_elem", "1.0.0"));

        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("child1", JsonDoc.get(response.getEntityData().get(0), new Path("relationships.0._id")).asText());
        Assert.assertEquals("A", JsonDoc.get(response.getEntityData().get(0), new Path("relationships.0.tree.0.child.code1")).asText());
        Assert.assertNull(JsonDoc.get(response.getEntityData().get(0), new Path("relationships.0.tree.0.child.ref")));

        fr = new FindRequest();
        fr.setQuery(query("{'field':'code1','op':'=','rvalue':'A'}"));
        fr.setProjection(projection("[{'field':'relationships.*','recursive':1},{'field':'relationships.*.tree.*.child.ref'}]"));
        fr.setEntityVersion(new EntityVersion("parent_w_elem", "1.0.0"));
        response = mediator.find(fr);
        System.out.println("Deep retrieval:" + response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("child1", JsonDoc.get(response.getEntityData().get(0), new Path("relationships.0._id")).asText());
        Assert.assertEquals("A", JsonDoc.get(response.getEntityData().get(0), new Path("relationships.0.tree.0.child.code1")).asText());
        Assert.assertEquals("A", JsonDoc.get(response.getEntityData().get(0), new Path("relationships.0.tree.0.child.ref.0.code1")).asText());
    }

    @Test
    public void elemMatchTopLevelArr() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'authentications','elemMatch':{ '$and':[ { 'field':'principal','op':'$in','values':['a']}, {'field':'providerName','op':'$eq','rvalue':'p'} ] } }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'legalEntities.*.legalEntity' }]"));
        fr.setEntityVersion(new EntityVersion("U", "0.0.1"));

        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("l1", JsonDoc.get(response.getEntityData().get(0), new Path("legalEntities.0.legalEntity.0.name")).asText());
    }

    @Test
    public void projectSimpleArrayElements() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'1'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'legalEntities.*.legalEntity' }]"));
        fr.setEntityVersion(new EntityVersion("U", "0.0.1"));

        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals("l1", JsonDoc.get(response.getEntityData().get(0), new Path("legalEntities.0.legalEntity.0.name")).asText());
        Assert.assertEquals("arr0", JsonDoc.get(response.getEntityData().get(0), new Path("legalEntities.0.legalEntity.0.arr.0")).asText());
    }

    @Test
    public void updateWithAssocq() throws Exception {
        UpdateRequest urq = new UpdateRequest();
        urq.setQuery(query("{'array':'level1.arr1', 'elemMatch': {'field':'ref.*.field1','op':'=','rvalue':'bdeep1'}}"));
        urq.setReturnFields(projection("{'field':'*','recursive':1}"));
        urq.setUpdateExpression(update("{'$set':{'field1':1}}"));
        urq.setEntityVersion(new EntityVersion("A", "1.0.0"));

        Response response = mediator.update(urq);
        Assert.assertNotNull(updateQuery);
        Assert.assertTrue(updateQuery instanceof ValueComparisonExpression);
    }

    @Test
    public void deleteWithAssocq() throws Exception {
        DeleteRequest drq = new DeleteRequest();
        drq.setQuery(query("{'array':'level1.arr1', 'elemMatch': {'field':'ref.*.field1','op':'=','rvalue':'bdeep1'}}"));
        drq.setEntityVersion(new EntityVersion("A", "1.0.0"));

        Response response = mediator.delete(drq);
        Assert.assertNotNull(updateQuery);
        Assert.assertTrue(updateQuery instanceof ValueComparisonExpression);
    }

    @Test
    public void rev_search_with_arraycond() throws Exception {
        /**
         * This results in a query plan where U -> L, and the relationship L->U
         * is defined with an array elemMatch query
         * {array:x,elemMatch:{field:y,op:=,rfield:rf}} Once bound, this should
         * be rewritten as: {field:localized(rf),op:=,rvalues:x.i.y} In other
         * words, array elem match with array X and field Y is treated like a
         * search on X.*.Y
         *
         */
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'us.*.authentications','elemMatch':{ '$and':[ { 'field':'principal','op':'$in','values':['a']}, {'field':'providerName','op':'$eq','rvalue':'p'} ] } }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'us','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("L", "0.0.1"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(2, response.getEntityData().get(0).get("us").size());
    }

   @Test
    public void rev_search_with_arraycond_matchcount() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'us.*.authentications','elemMatch':{ '$and':[ { 'field':'principal','op':'$in','values':['a']}, {'field':'providerName','op':'$eq','rvalue':'p'} ] } }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'us','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("L", "0.0.1"));
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

   @Test
    public void rev_search_with_arraycond_matchcount_0range() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'us.*.authentications','elemMatch':{ '$and':[ { 'field':'principal','op':'$in','values':['a']}, {'field':'providerName','op':'$eq','rvalue':'p'} ] } }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'us','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("L", "0.0.1"));
        fr.setFrom(0l);
        fr.setTo(-1l);
        Response response = mediator.find(fr);
        Assert.assertEquals(1, response.getMatchCount());
        Assert.assertEquals(0, response.getEntityData().size());
    }

    @Test
    public void elem_match_forward() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'userRedHatPrincipal','op':'=','rvalue':'a'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'users','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("UC", "0.0.1"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    @Test
    public void elem_match_forward_0range() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'userRedHatPrincipal','op':'=','rvalue':'a'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'users','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("UC", "0.0.1"));
        fr.setFrom(0l);
        fr.setTo(-1l);
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    @Test
    public void elem_match_backward() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'users.*.legalEntities.*.emails','elemMatch':{'field':'address','op':'=','rvalue':'email@x.com'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'users','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("UC", "0.0.1"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    @Test
    public void elem_match_backward_0range() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'array':'users.*.legalEntities.*.emails','elemMatch':{'field':'address','op':'=','rvalue':'email@x.com'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'users','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("UC", "0.0.1"));
        fr.setFrom(0l);
        fr.setTo(-1l);
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    // Errors during association retrieval should propagate
    @Test
    public void assoc_errors_propagate() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':1}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'us','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("L", "0.0.1"));
        findError = Error.get("NoAccess", "blah");
        errorEntity = "U";
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getErrors().size());

        findError = Error.get("NoAccess", "blah");
        errorEntity = "L";
        response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getErrors().size());

    }

    @Test
    public void assocQWithNull() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'$and': [ {'array':'authentications','elemMatch':{'field':'providerName','op':'$nin','values':['x',null]}}, {'field':'_id','op':'$in','values':[1,2]}]}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'legalEntities.*.legalEntity','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("U", "0.0.1"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
    }

    @Test
    public void assocQWithNull2() throws Exception {
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'$and': [ {'field':'authentications.*.providerName','op':'$nin','values':['x',null]},{'field':'_id','op':'$in','values':[1,2]}]}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'legalEntities.*.legalEntity','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("U", "0.0.1"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
    }

    @Test
    public void dont_do_outer_joins() throws Exception {
        // We need to use a_with_index in this test, so the execution plan becomes A->B instead of B-> A
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'$and': [ {'field':'_id','op':'$in','values':['A99','ADEEP']}, {'field':'level1.arr1.*.ref.*.field1','op':'=','rvalue':'bdeep1'} ] }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("A_with_index", "1.0.0"));
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    @Test
    public void dont_do_outer_joins_0range() throws Exception {
        // We need to use a_with_index in this test, so the execution plan becomes A->B instead of B-> A
        FindRequest fr = new FindRequest();
        fr.setQuery(query("{'$and': [ {'field':'_id','op':'$in','values':['A99','ADEEP']}, {'field':'level1.arr1.*.ref.*.field1','op':'=','rvalue':'bdeep1'} ] }"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'level1.arr1.*.ref','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("A_with_index", "1.0.0"));
        fr.setFrom(0l);
        fr.setTo(-1l);
        Response response = mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    @Test
    public void array_in_reference_fullarr() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'1'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'ref'}]"));
        fr.setEntityVersion(new EntityVersion("arr_parent","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(3,response.getEntityData().get(0).get("ref").size());
    }

    @Test
    public void array_in_reference_emptyarr() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'2'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'ref'}]"));
        fr.setEntityVersion(new EntityVersion("arr_parent","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertNull(response.getEntityData().get(0).get("ref"));
    }

    @Test
    public void array_in_reference_nullarr() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'3'}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'ref'}]"));
        fr.setEntityVersion(new EntityVersion("arr_parent","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertNull(response.getEntityData().get(0).get("ref"));
    }


    @Test
    public void three_level_search() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'array':'obj1.c','elemMatch':{'field':'b.*.field1','op':'=','rvalue':'F, BLYO4OjLMAT aG.4qJ'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
        Assert.assertEquals(1,response.getEntityData().get(0).get("obj1").get("c").size());
        Assert.assertEquals(1,response.getEntityData().get(0).get("obj1").get("c").get(0).get("b").size());
    }

    @Test
    public void two_level_search_three_level_fetch() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'array':'obj1.c','elemMatch':{'field':'_id','op':'=','rvalue':'C50'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'obj1.c.*.b'}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
        Assert.assertEquals(1,response.getEntityData().get(0).get("obj1").get("c").size());
        Assert.assertEquals(1,response.getEntityData().get(0).get("obj1").get("c").get(0).get("b").size());
    }

    @Test
    public void two_level_search_three_level_fetch_nested_projection() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'array':'obj1.c','elemMatch':{'field':'_id','op':'=','rvalue':'C50'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1},{'field':'obj1.c', 'match':{'field':'_id','op':'!=','rvalue':''},'projection':{'field':'b','match':{'field':'_id','op':'!=','rvalue':''},'projection':{'field':'field1'}}}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
        Assert.assertEquals(1,response.getEntityData().get(0).get("obj1").get("c").size());
        Assert.assertEquals(1,response.getEntityData().get(0).get("obj1").get("c").get(0).get("b").size());
        Assert.assertNotNull(response.getEntityData().get(0).get("obj1").get("c").get(0).get("b").get(0).get("field1"));
    }

    @Test
    public void three_level_search2() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'array':'obj1.c','elemMatch':{'$and':[{'field':'obj1.d.*.field1','regex':'lw'}]}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
        Assert.assertTrue(response.getEntityData().get(0).get("obj1").get("c").get(0).get("obj1").get("d").get(0).get("field1").asText()!=null);
    }

    @Test
    public void three_level_search_w_0range() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'array':'obj1.c','elemMatch':{'field':'b.*.field1','op':'=','rvalue':'F, BLYO4OjLMAT aG.4qJ'}}"));
        fr.setProjection(projection("[{'field':'*','recursive':1}]"));
        fr.setEntityVersion(new EntityVersion("A","1.0.0"));
        fr.setFrom(0l);
        fr.setTo(-1l);
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(0, response.getEntityData().size());
        Assert.assertEquals(1, response.getMatchCount());
    }

    @Test
    public void looping_entities_with_or_query_704() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'$and':["+
                          "           {'field':'field1','regex':'f1value'},"+
                          "           {'field':'refchild.*.refparent.*.field2','op':'=','rvalue':true},"+
                          "           {'$or':["+
                          "                     {'$and':["+
                          "                                {'field':'field3','op':'!=','rvalue':'f3value'},"+
                          "                                {'field':'refchild.*.field1','op':'=','rvalue':true}"+
                          "                             ]},"+
                          "                     {'field':'field3','op':'=','rvalue':'f3value'}"+
                          "                  ]}"+
                          "]}"));
        fr.setProjection(projection("[{'field':'*'},{'field':'refchild'}]"));
        fr.setEntityVersion(new EntityVersion("root_loop","1.0.0."));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(4,response.getMatchCount());
    }

    @Test
    public void query_processing_error_707() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'$and':["+
                          "          {'$or':["+
                          "             {'field':'field1','op':'=','rvalue':'f1value'},"+
                          "             {'array':'refchild.*.array','elemMatch':{"+
                          "                       '$and':["+
                          "                                 {'field':'field1','op':'=','rvalue':'value1'},"+
                          "                                 {'field':'field2','regex':'value2'}"+
                          "                              ]"+
                          "                   }"+
                          "             }"+
                          "          ]},"+
                          "          {'field':'refchild.*.refparent.*.field2','op':'=','rvalue':true}"+
                          "]}"));
        fr.setProjection(projection("[{'field':'*'},{'field':'refchild'}]"));
        fr.setEntityVersion(new EntityVersion("root_loop","1.0.0."));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(5,response.getMatchCount());
    }

    @Test
    public void looping_entities_with_or_query_709_w_n_to_n_refs() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'$and':["+
                          "           {'field':'repository','regex':'repo.*'},"+
                          "           {'field':'images.*.repositories.*.published','op':'=','rvalue':true},"+
                          "           {'$or':["+
                          "                     {'$and':["+
                          "                                {'field':'vendorLabel','op':'!=','rvalue':'Red Hat'},"+
                          "                                {'field':'images.*.certified','op':'=','rvalue':true}"+
                          "                             ]},"+
                          "                     {'field':'vendorLabel','op':'=','rvalue':'Red Hat'}"+
                          "                  ]}"+
                          "]}"));
        fr.setProjection(projection("[{'field':'*'},{'field':'refchild'}]"));
        fr.setEntityVersion(new EntityVersion("containerRepository","1.0.0."));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(3,response.getMatchCount());
    }

    @Test
    public void looping_entities_with_or_query_709_w_n_to_n_refs_max() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'$and':["+
                          "           {'field':'repository','regex':'repo.*'},"+
                          "           {'field':'images.*.repositories.*.published','op':'=','rvalue':true},"+
                          "           {'$or':["+
                          "                     {'$and':["+
                          "                                {'field':'vendorLabel','op':'!=','rvalue':'Red Hat'},"+
                          "                                {'field':'images.*.certified','op':'=','rvalue':true}"+
                          "                             ]},"+
                          "                     {'field':'vendorLabel','op':'=','rvalue':'Red Hat'}"+
                          "                  ]}"+
                          "]}"));
        fr.setProjection(projection("[{'field':'*'},{'field':'refchild'}]"));
        fr.setEntityVersion(new EntityVersion("containerRepository","1.0.0."));
        fr.setFrom(0l);
        fr.setTo(1l);
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(3,response.getMatchCount());
        Assert.assertEquals(2,response.getEntityData().size());
    }

    @Test
    public void test_self_ref_array_contains() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'!=','rvalue':''}"));
        fr.setProjection(projection("[{'field':'*','recursive':true},{'field':'test_reference'}]"));
        fr.setEntityVersion(new EntityVersion("self_ref_array_contains","0.0.1-SNAPSHOT"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        // Every doc should have all the others in reference
        for(JsonNode doc:response.getEntityData()) {
            String id=doc.get("_id").asText();
            Assert.assertEquals(3,doc.get("test_reference").size());
            for(Iterator<JsonNode> itr=doc.get("test_reference").elements();itr.hasNext();) {
                JsonNode node=itr.next();
                Assert.assertTrue(!id.equals(node.get("_id")));
            }
        }
    }

    @Test
    public void test_self_ref_array_not_contains() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'!=','rvalue':''}"));
        fr.setProjection(projection("[{'field':'*','recursive':true},{'field':'test_reference'}]"));
        fr.setEntityVersion(new EntityVersion("self_ref_array_not_contains","0.0.1-SNAPSHOT"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        for(JsonNode doc:response.getEntityData()) {
            String id=doc.get("_id").asText();
            if("Q".equals(id)) {
                Assert.assertEquals(2,doc.get("test_reference").size());
                Assert.assertTrue("ST".indexOf(doc.get("test_reference").get(0).get("_id").asText())!=-1);
                Assert.assertTrue("ST".indexOf(doc.get("test_reference").get(1).get("_id").asText())!=-1);
            } else if("R".equals(id)) {
                Assert.assertEquals(2,doc.get("test_reference").size());
                Assert.assertTrue("ST".indexOf(doc.get("test_reference").get(0).get("_id").asText())!=-1);
                Assert.assertTrue("ST".indexOf(doc.get("test_reference").get(1).get("_id").asText())!=-1);
            } else if("S".equals(id)) {
                Assert.assertEquals(1,doc.get("test_reference").size());
                Assert.assertEquals("T",doc.get("test_reference").get(0).get("_id").asText());
            } else {
                Assert.assertNull(doc.get("test_reference"));
            }
        }
    }

    @Test
    public void test_self_ref_with_parents() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'field':'_id','op':'=','rvalue':'img1'}"));
        fr.setProjection(projection("[{'field':'*','recursive':true},{'field':'vulnerabilities.*.packages.*.fixed_by_images.*'}]"));
        fr.setEntityVersion(new EntityVersion("containerImage-self","0.0.1"));
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(1,response.getEntityData().size());
        Assert.assertTrue(response.getEntityData().get(0).get("vulnerabilities").get(0).get("packages").get(0).get("fixed_by_images").size()==1);
    }

    @Test
    public void limited_retrieval_with_queries_on_sub() throws Exception {
        FindRequest fr=new FindRequest();
        fr.setQuery(query("{'$and':[{'array':'repositories','elemMatch':{'field':'repositories.*._id','op':'>','rvalue':'1'}},"+
                          "{'array':'repositories','elemMatch':{'field':'published','op':'=','rvalue':true}}]}"));        
        fr.setProjection(projection("[{'field':'*','recursive':true},{'field':'repositories.*.repositories.*.vendors','recursive':true}]"));
        fr.setEntityVersion(new EntityVersion("containerImage","1.0.0"));
        fr.setTo(2l);
        Response response=mediator.find(fr);
        System.out.println(response.getEntityData());
        Assert.assertEquals(3,response.getEntityData().size());
    }
}
