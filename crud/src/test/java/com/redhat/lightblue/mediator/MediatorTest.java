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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.*;
import com.redhat.lightblue.crud.*;
import com.redhat.lightblue.crud.interceptors.*;
import com.redhat.lightblue.crud.valuegenerators.*;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;
import com.redhat.lightblue.metadata.*;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.test.DatabaseMetadata;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.extensions.ExtensionSupport;
import com.redhat.lightblue.extensions.Extension;
import com.redhat.lightblue.extensions.valuegenerator.ValueGeneratorSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;

public class MediatorTest extends AbstractJsonSchemaTest {
    private Mediator mediator;
    private final TestMetadata mdManager = new TestMetadata();
    private final MockCrudController mockCrudController = new MockCrudController();

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    private static final class TestMetadata extends DatabaseMetadata {
        EntityMetadata md;

        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            return md;
        }
    }

    private static final class MockCrudController implements CRUDController, ExtensionSupport {
        CRUDUpdateResponse updateResponse;
        CRUDSaveResponse saveResponse;
        CRUDDeleteResponse deleteResponse;
        CRUDFindResponse findResponse;
        CRUDInsertionResponse insertResponse;
        CRUDOperationContext ctx;

        @Override
        public CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                            Projection projection) {
            this.ctx=ctx;
            return insertResponse;
        }

        @Override
        public CRUDSaveResponse save(CRUDOperationContext ctx,
                                     boolean upsert,
                                     Projection projection) {
            this.ctx=ctx;
            return saveResponse;
        }

        @Override
        public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                         QueryExpression query,
                                         UpdateExpression update,
                                         Projection projection) {
            this.ctx=ctx;
            return updateResponse;
        }

        @Override
        public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                         QueryExpression query) {
            return deleteResponse;
        }

        @Override
        public CRUDFindResponse find(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     Projection projection,
                                     Sort sort,
                                     Long from,
                                     Long to) {
            return findResponse;
        }

        @Override
        public MetadataListener getMetadataListener() {
            return null;
        }

        @Override
        public void updatePredefinedFields(CRUDOperationContext ctx,JsonDoc doc) {}

        @Override
        public <E extends Extension> E  getExtensionInstance(Class<? extends Extension> extensionClass) {
            if(extensionClass.equals(ValueGeneratorSupport.class)) {
                return (E)new MockValueGeneratorSupport();
            } else
                return null;
        }

    }

    private static class MockValueGeneratorSupport implements ValueGeneratorSupport {
        public static int v=0;
        
        @Override
        public ValueGenerator.ValueGeneratorType[] getSupportedGeneratorTypes() {
            return new ValueGenerator.ValueGeneratorType[] {ValueGenerator.ValueGeneratorType.IntSequence};
        }

        @Override
        public Object generateValue(EntityMetadata md,ValueGenerator generator) {
            return new Integer(v++);
        }
    }

    private static final class RestClientIdentification extends ClientIdentification {

        private final Set<String> clientRoles;

        public RestClientIdentification(List<String> roles) {
            clientRoles = new HashSet<>();
            clientRoles.addAll(roles);
        }

        @Override
        public String getPrincipal() {
            return "";
        }

        @Override
        public boolean isUserInRole(String role) {
            return clientRoles.contains(role);
        }

        @Override
        public JsonNode toJson() {
            return null;
        }
    }

    private EntityMetadata getMd(String fname) throws Exception {
        JsonNode node = loadJsonNode(fname);
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();
        extensions.registerDataStoreParser("mongo", new TestDataStoreParser<JsonNode>());
        TypeResolver resolver = new DefaultTypes();
        JSONMetadataParser parser = new JSONMetadataParser(extensions, resolver, nodeFactory);
        EntityMetadata md = parser.parseEntityMetadata(node);
        PredefinedFields.ensurePredefinedFields(md);
        return md;
    }

    @Before
    public void initMediator() throws Exception {
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        new UIDInterceptor().register(factory.getInterceptors());
        new GeneratedFieldInterceptor().register(factory.getInterceptors());
        factory.addCRUDController("mongo", mockCrudController);
        mdManager.md = getMd("./testMetadata.json");
        mediator = new Mediator(mdManager, factory);
    }

    @Test
    public void disabledVersionTest() throws Exception {

        mdManager.md.setStatus(MetadataStatus.DISABLED);
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);

        mdManager.md.getAccess().getInsert().setRoles("role1");
        Response response = mediator.insert(req);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(CrudConstants.ERR_DISABLED_METADATA,response.getErrors().get(0).getErrorCode());

    }

    @Test
    public void insertRoleAccessTest() throws Exception {
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);
        req.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));

        Response response = mediator.insert(req);

        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    @Test
    public void insertFieldAccessTest() throws Exception {
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);
        req.setClientId(new RestClientIdentification(Arrays.asList("test.field1-insert", "test-insert")));

        Response response = mediator.insert(req);

        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    @Test
    public void insertDocWithRequiredFieldNull() throws Exception {
        mdManager.md = getMd("./termsmd.json");

        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("terms", "0.14.0-SNAPSHOT"));
        req.setEntityData(loadJsonNode("./termsdata.json"));
        req.setReturnFields(null);
        req.setClientId(new RestClientIdentification(Arrays.asList("test.field1-insert", "test-insert")));
        // lastUpdatedBy is required, set that to null
        ((ObjectNode)req.getEntityData()).set("lastUpdatedBy",JsonNodeFactory.instance.nullNode());
        
        Response response = mediator.insert(req);
        // there should be no errors
    }

    @Test
    public void insertRoleTest() throws Exception {
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);

        mdManager.md.getAccess().getInsert().setRoles("role1");
        Response response = mediator.insert(req);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());

        mdManager.md.getAccess().getInsert().setRoles("anyone");
        response = mediator.insert(req);

        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());

    }

    @Test
    public void saveRoleTest() throws Exception {
        SaveRequest req = new SaveRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);

        mdManager.md.getAccess().getInsert().setRoles("role1");
        mdManager.md.getAccess().getUpdate().setRoles("role1");
        Response response = mediator.save(req);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());

        mdManager.md.getAccess().getInsert().setRoles("anyone");
        mdManager.md.getAccess().getUpdate().setRoles("anyone");
        response = mediator.save(req);

        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());

    }

    @Test
    public void updateRoleTest() throws Exception {
        UpdateRequest req = new UpdateRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setReturnFields(null);

        mdManager.md.getAccess().getUpdate().setRoles("role1");
        mockCrudController.updateResponse = new CRUDUpdateResponse();
        Response response = mediator.update(req);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());

        mdManager.md.getAccess().getUpdate().setRoles("anyone");
        mockCrudController.updateResponse.setNumUpdated(1);
        response = mediator.update(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());
    }


    @Test
    public void updateQueryFieldRoleTest() throws Exception {
        UpdateRequest req = new UpdateRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setReturnFields(null);
        req.setQuery(new ValueComparisonExpression(new Path("field1"),BinaryComparisonOperator._eq,new Value("x")));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-update")));

        mockCrudController.updateResponse = new CRUDUpdateResponse();
        Response response = mediator.update(req);
        Assert.assertEquals(OperationStatus.ERROR,response.getStatus());

        req.setQuery(new ValueComparisonExpression(new Path("field2"),BinaryComparisonOperator._eq,new Value("x")));
        response = mediator.update(req);
        Assert.assertEquals(OperationStatus.COMPLETE,response.getStatus());
    }

    @Test
    public void deleteRoleTest() throws Exception {
        DeleteRequest req = new DeleteRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));

        mdManager.md.getAccess().getDelete().setRoles("role1");
        mockCrudController.deleteResponse = new CRUDDeleteResponse();
        Response response = mediator.delete(req);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());

        mdManager.md.getAccess().getDelete().setRoles("anyone");
        mockCrudController.deleteResponse.setNumDeleted(1);
        response = mediator.delete(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    @Test
    public void deleteQueryFieldRoleTest() throws Exception {
        DeleteRequest req = new DeleteRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setQuery(new ValueComparisonExpression(new Path("field1"),BinaryComparisonOperator._eq,new Value("x")));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-delete")));

        mockCrudController.deleteResponse = new CRUDDeleteResponse();
        Response response = mediator.delete(req);
        Assert.assertEquals(OperationStatus.ERROR,response.getStatus());

        req.setQuery(new ValueComparisonExpression(new Path("field2"),BinaryComparisonOperator._eq,new Value("x")));
        response = mediator.delete(req);
        Assert.assertEquals(OperationStatus.COMPLETE,response.getStatus());
    }


    @Test
    public void findRoleTest() throws Exception {
        FindRequest req = new FindRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));

        mdManager.md.getAccess().getFind().setRoles("role1");
        mockCrudController.findResponse = new CRUDFindResponse();
        Response response = mediator.find(req);

        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());

        mdManager.md.getAccess().getFind().setRoles("anyone");
        mockCrudController.findResponse.setSize(0);
        response = mediator.find(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    @Test
    public void findQueryFieldRoleTest() throws Exception {
        FindRequest req = new FindRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setQuery(new ValueComparisonExpression(new Path("field1"),BinaryComparisonOperator._eq,new Value("x")));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-find")));

        mockCrudController.findResponse = new CRUDFindResponse();
        Response response = mediator.find(req);
        Assert.assertEquals(OperationStatus.ERROR,response.getStatus());

        req.setQuery(new ValueComparisonExpression(new Path("field2"),BinaryComparisonOperator._eq,new Value("x")));
        response = mediator.find(req);
        Assert.assertEquals(OperationStatus.COMPLETE,response.getStatus());
    }

    @Test
    public void optionalQueryTest() throws Exception {
        FindRequest req = new FindRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-find")));

        mockCrudController.findResponse = new CRUDFindResponse();
        Response response = mediator.find(req);
        Assert.assertEquals(OperationStatus.COMPLETE,response.getStatus());
    }

    @Test
    public void generatorTest() throws Exception {
        MockValueGeneratorSupport.v=0;
        mdManager.md = getMd("./generator-md.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("user", "5.0.0"));
        req.setEntityData(loadJsonNode("./userdata.json"));
        req.setReturnFields(new FieldProjection(new Path("*"),true,true));
        mockCrudController.insertResponse=new CRUDInsertionResponse();
        Response response = mediator.insert(req);
        System.out.println(response.getDataErrors());
        JsonDoc doc=mockCrudController.ctx.getDocuments().get(0);
        Assert.assertEquals(0,response.getErrors().size());
        Assert.assertEquals(0,response.getDataErrors().size());
        System.out.println(doc);
        int v=doc.get(new Path("_id")).asInt();
        // There are 13 generated values in this doc
        Assert.assertTrue(v>=0&&v<=13);
        v=doc.get(new Path("requid")).asInt();
        Assert.assertTrue(v>=0&&v<=13);
        v=doc.get(new Path("iduid")).asInt();
        Assert.assertTrue(v>=0&&v<=13);
        v=doc.get(new Path("personalInfo.requid")).asInt();
        Assert.assertTrue(v>=0&&v<=13);
        Assert.assertNull(doc.get(new Path("personalInfo.nonrequid")));
        v=doc.get(new Path("sites.0.streetAddress.requid")).asInt();
        Assert.assertTrue(v>=0&&v<=13);
        v=doc.get(new Path("sites.0.streetAddress.nonrequid")).asInt();
        Assert.assertTrue(v>=0&&v<=13);
        v=doc.get(new Path("uid")).asInt();
        Assert.assertTrue(v>=0&&v<=13);
    }

    @Test
    public void generatorOverwriteTest() throws Exception {
        MockValueGeneratorSupport.v=0;
        mdManager.md = getMd("./overwrite-md.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("user", "5.0.0"));
        JsonNode docNode=loadJsonNode("./gen-overwrite-testdata.json");
        req.setEntityData(docNode);
        req.setReturnFields(new FieldProjection(new Path("*"),true,true));
        mockCrudController.insertResponse=new CRUDInsertionResponse();

        String today=docNode.get("today").asText();
        String id=docNode.get("_id").asText();

        Response response = mediator.insert(req);
        JsonDoc doc=mockCrudController.ctx.getDocuments().get(0);
        System.out.println(doc);
        
        Assert.assertEquals(id,doc.get(new Path("_id")).asText());
        Assert.assertTrue(!today.equals(doc.get(new Path("today")).asText()));
        
    }

    @Test
    public void uidTest() throws Exception {
        mdManager.md = getMd("./usermd.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("user", "5.0.0"));
        req.setEntityData(loadJsonNode("./userdata.json"));
        req.setReturnFields(null);
        mockCrudController.insertResponse=new CRUDInsertionResponse();
        Response response = mediator.insert(req);
        System.out.println(response.getDataErrors());
        Assert.assertEquals(0,response.getErrors().size());
        Assert.assertEquals(0,response.getDataErrors().size());
        System.out.println(mockCrudController.ctx.getDocuments().get(0));
    }

    @Test
    public void uidTermsTest() throws Exception {
        mdManager.md = getMd("./termsmd.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("terms", "0.14.1-SNAPSHOT"));
        req.setEntityData(loadJsonNode("./termsdata.json"));
        req.setReturnFields(null);
        mockCrudController.insertResponse=new CRUDInsertionResponse();
        Response response = mediator.insert(req);
        System.out.println(response.getDataErrors());
        System.out.println(response.getErrors());
        Assert.assertEquals(0,response.getErrors().size());
        Assert.assertEquals(0,response.getDataErrors().size());
        System.out.println(mockCrudController.ctx.getDocuments().get(0));
    }

    @Test
    public void uidTermsSaveTest() throws Exception {
        mdManager.md = getMd("./termsmd.json");
        SaveRequest req = new SaveRequest();
        req.setEntityVersion(new EntityVersion("terms", "0.14.1-SNAPSHOT"));
        req.setEntityData(loadJsonNode("./termsdata.json"));
        req.setReturnFields(null);
        mockCrudController.saveResponse=new CRUDSaveResponse();
        Response response = mediator.save(req);
        System.out.println(response.getDataErrors());
        System.out.println(response.getErrors());
        Assert.assertEquals(0,response.getErrors().size());
        Assert.assertEquals(0,response.getDataErrors().size());
        System.out.println(mockCrudController.ctx.getDocuments().get(0));
    }

    @Test
    public void bulkTest() throws Exception {
        BulkRequest breq=new BulkRequest();
        
        InsertionRequest ireq = new InsertionRequest();
        ireq.setEntityVersion(new EntityVersion("test", "1.0"));
        ireq.setEntityData(loadJsonNode("./sample1.json"));
        ireq.setReturnFields(null);
        ireq.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));
        breq.add(ireq);

        FindRequest freq = new FindRequest();
        freq.setEntityVersion(new EntityVersion("test", "1.0"));

        mdManager.md.getAccess().getFind().setRoles("role1");
        breq.add(freq);

        BulkResponse bresp=mediator.bulkRequest(breq);

        Response response=bresp.getEntries().get(0);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());

        response=bresp.getEntries().get(1);
        
        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(0, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(1, response.getErrors().size());
        Assert.assertEquals(CrudConstants.ERR_NO_ACCESS, response.getErrors().get(0).getErrorCode());
        
    }
}
