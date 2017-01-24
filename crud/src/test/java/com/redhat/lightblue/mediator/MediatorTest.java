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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.ResultMetadata;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.crud.WithRange;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;

public class MediatorTest extends AbstractMediatorTest {

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
        Assert.assertEquals(CrudConstants.ERR_DISABLED_METADATA, response.getErrors().get(0).getErrorCode());

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

    private ResultMetadata getRmd(String ver) {
        ResultMetadata md=new ResultMetadata();
        md.setDocumentVersion(ver);
        return md;
    }

    @Test
    public void insertResultMetadataTest() throws Exception {
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);
        req.setClientId(new RestClientIdentification(Arrays.asList("test-insert", "test-update")));
        mockCrudController.insertCb=ctx->{ctx.getDocuments().get(0).setResultMetadata(getRmd("1"));};

        Response response = mediator.insert(req);

        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1,response.getResultMetadata().size());
        Assert.assertEquals("1",response.getResultMetadata().get(0).getDocumentVersion());
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
        req.setEntityVersion(new EntityVersion("terms", "0.14.1-SNAPSHOT"));
        req.setEntityData(loadJsonNode("./termsdata.json"));
        req.setReturnFields(null);
        req.setClientId(new RestClientIdentification(Arrays.asList("test.field1-insert", "test-insert")));
        // lastUpdatedBy is required, set that to null
        ((ObjectNode) req.getEntityData()).set("lastUpdatedBy", JsonNodeFactory.instance.nullNode());

        Response response = mediator.insert(req);
        // there should be no errors
        // Response should return the entity name:version
        Assert.assertEquals("terms",response.getEntity().getEntity());
        Assert.assertEquals("0.14.1-SNAPSHOT",response.getEntity().getVersion());
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
    public void saveResultMdTest() throws Exception {
        SaveRequest req = new SaveRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setEntityData(loadJsonNode("./sample1.json"));
        req.setReturnFields(null);
        mockCrudController.saveCb=ctx->{ctx.getDocuments().get(0).setResultMetadata(getRmd("1"));};

        mdManager.md.getAccess().getInsert().setRoles("anyone");
        mdManager.md.getAccess().getUpdate().setRoles("anyone");
        Response response = mediator.save(req);

        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(1,response.getResultMetadata().size());
        Assert.assertEquals("1",response.getResultMetadata().get(0).getDocumentVersion());
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
        mockCrudController.updateResponse.setNumMatched(1);
        response = mediator.update(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(1, response.getModifiedCount());
        Assert.assertEquals(1, response.getMatchCount());
        Assert.assertEquals(0, response.getDataErrors().size());
        Assert.assertEquals(0, response.getErrors().size());
    }

    @Test
    public void updateQueryFieldRoleTest() throws Exception {
        UpdateRequest req = new UpdateRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setReturnFields(null);
        req.setQuery(new ValueComparisonExpression(new Path("field1"), BinaryComparisonOperator._eq, new Value("x")));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-update")));

        mockCrudController.updateResponse = new CRUDUpdateResponse();
        Response response = mediator.update(req);
        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());

        req.setQuery(new ValueComparisonExpression(new Path("field2"), BinaryComparisonOperator._eq, new Value("x")));
        response = mediator.update(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
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
        // Response should return the entity name:version
        Assert.assertEquals("test",response.getEntity().getEntity());
        Assert.assertEquals("1.0",response.getEntity().getVersion());
    }

    @Test
    public void deleteQueryFieldRoleTest() throws Exception {
        DeleteRequest req = new DeleteRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setQuery(new ValueComparisonExpression(new Path("field1"), BinaryComparisonOperator._eq, new Value("x")));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-delete")));

        mockCrudController.deleteResponse = new CRUDDeleteResponse();
        Response response = mediator.delete(req);
        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());

        req.setQuery(new ValueComparisonExpression(new Path("field2"), BinaryComparisonOperator._eq, new Value("x")));
        response = mediator.delete(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
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
        // Response should return the entity name:version
        Assert.assertEquals("test",response.getEntity().getEntity());
        Assert.assertEquals("1.0",response.getEntity().getVersion());
    }

    @Test
    public void findResultMdTest() throws Exception {
        FindRequest req = new FindRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));

        mdManager.md.getAccess().getFind().setRoles("anyone");
        mockCrudController.findResponse = new CRUDFindResponse();
        mockCrudController.findResponse.setSize(10);
        mockCrudController.findCb=ctx->{
            for(int i=0;i<10;i++)
                ctx.addDocument(new JsonDoc(JsonNodeFactory.instance.objectNode()),getRmd(Integer.toString(i)));
        };
        Response response = mediator.find(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
        Assert.assertEquals(0, response.getModifiedCount());
        Assert.assertEquals(10, response.getMatchCount());
        for(int i=0;i<10;i++) {
            Assert.assertEquals(Integer.toString(i),response.getResultMetadata().get(i).getDocumentVersion());
        }
    }

    @Test
    public void findQueryFieldRoleTest() throws Exception {
        FindRequest req = new FindRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setQuery(new ValueComparisonExpression(new Path("field1"), BinaryComparisonOperator._eq, new Value("x")));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-find")));

        mockCrudController.findResponse = new CRUDFindResponse();
        Response response = mediator.find(req);
        Assert.assertEquals(OperationStatus.ERROR, response.getStatus());

        req.setQuery(new ValueComparisonExpression(new Path("field2"), BinaryComparisonOperator._eq, new Value("x")));
        response = mediator.find(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
    }

    @Test
    public void optionalQueryTest() throws Exception {
        FindRequest req = new FindRequest();
        req.setEntityVersion(new EntityVersion("test", "1.0"));
        req.setClientId(new RestClientIdentification(Arrays.asList("test-find")));

        mockCrudController.findResponse = new CRUDFindResponse();
        Response response = mediator.find(req);
        Assert.assertEquals(OperationStatus.COMPLETE, response.getStatus());
    }

    @Test
    public void generatorTest() throws Exception {
        MockValueGeneratorSupport.v = 0;
        mdManager.md = getMd("./generator-md.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("user", "5.0.0"));
        req.setEntityData(loadJsonNode("./userdata.json"));
        req.setReturnFields(new FieldProjection(new Path("*"), true, true));
        mockCrudController.insertResponse = new CRUDInsertionResponse();
        Response response = mediator.insert(req);
        System.out.println(response.getDataErrors());
        JsonDoc doc = mockCrudController.ctx.getDocuments().get(0);
        Assert.assertEquals(0, response.getErrors().size());
        Assert.assertEquals(0, response.getDataErrors().size());
        System.out.println(doc);
        int v = doc.get(new Path("_id")).asInt();
        // There are 13 generated values in this doc
        Assert.assertTrue(v >= 0 && v <= 13);
        v = doc.get(new Path("requid")).asInt();
        Assert.assertTrue(v >= 0 && v <= 13);
        v = doc.get(new Path("iduid")).asInt();
        Assert.assertTrue(v >= 0 && v <= 13);
        v = doc.get(new Path("personalInfo.requid")).asInt();
        Assert.assertTrue(v >= 0 && v <= 13);
        Assert.assertNull(doc.get(new Path("personalInfo.nonrequid")));
        v = doc.get(new Path("sites.0.streetAddress.requid")).asInt();
        Assert.assertTrue(v >= 0 && v <= 13);
        v = doc.get(new Path("sites.0.streetAddress.nonrequid")).asInt();
        Assert.assertTrue(v >= 0 && v <= 13);
        v = doc.get(new Path("uid")).asInt();
        Assert.assertTrue(v >= 0 && v <= 13);
    }

    @Test
    public void generatorOverwriteTest() throws Exception {
        MockValueGeneratorSupport.v = 0;
        mdManager.md = getMd("./overwrite-md.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("user", "5.0.0"));
        JsonNode docNode = loadJsonNode("./gen-overwrite-testdata.json");
        req.setEntityData(docNode);
        req.setReturnFields(new FieldProjection(new Path("*"), true, true));
        mockCrudController.insertResponse = new CRUDInsertionResponse();

        String today = docNode.get("today").asText();
        String id = docNode.get("_id").asText();

        Response response = mediator.insert(req);
        JsonDoc doc = mockCrudController.ctx.getDocuments().get(0);
        System.out.println(doc);

        Assert.assertEquals(id, doc.get(new Path("_id")).asText());
        Assert.assertTrue(!today.equals(doc.get(new Path("today")).asText()));

    }

    @Test
    public void uidTest() throws Exception {
        mdManager.md = getMd("./usermd.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("user", "5.0.0"));
        req.setEntityData(loadJsonNode("./userdata.json"));
        req.setReturnFields(null);
        mockCrudController.insertResponse = new CRUDInsertionResponse();
        Response response = mediator.insert(req);
        System.out.println(response.getDataErrors());
        Assert.assertEquals(0, response.getErrors().size());
        Assert.assertEquals(0, response.getDataErrors().size());
        System.out.println(mockCrudController.ctx.getDocuments().get(0));
    }

    @Test
    public void uidTermsTest() throws Exception {
        mdManager.md = getMd("./termsmd.json");
        InsertionRequest req = new InsertionRequest();
        req.setEntityVersion(new EntityVersion("terms", "0.14.1-SNAPSHOT"));
        req.setEntityData(loadJsonNode("./termsdata.json"));
        req.setReturnFields(null);
        mockCrudController.insertResponse = new CRUDInsertionResponse();
        Response response = mediator.insert(req);
        System.out.println(response.getDataErrors());
        System.out.println(response.getErrors());
        Assert.assertEquals(0, response.getErrors().size());
        Assert.assertEquals(0, response.getDataErrors().size());
        System.out.println(mockCrudController.ctx.getDocuments().get(0));
    }

    @Test
    public void uidTermsSaveTest() throws Exception {
        mdManager.md = getMd("./termsmd.json");
        SaveRequest req = new SaveRequest();
        req.setEntityVersion(new EntityVersion("terms", "0.14.1-SNAPSHOT"));
        req.setEntityData(loadJsonNode("./termsdata.json"));
        req.setReturnFields(null);
        mockCrudController.saveResponse = new CRUDSaveResponse();
        Response response = mediator.save(req);
        System.out.println(response.getDataErrors());
        System.out.println(response.getErrors());
        Assert.assertEquals(0, response.getErrors().size());
        Assert.assertEquals(0, response.getDataErrors().size());
        System.out.println(mockCrudController.ctx.getDocuments().get(0));
    }

    @Test
    public void testApplyRange_FromNull_ToNull() {
        List<JsonDoc> responseDocuments = new ArrayList<>();
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(1)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(2)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(3)));

        List<JsonDoc> modified = mediator.applyRange(
                new WithRange() {
            @Override
            public Long getFrom() {
                return null;
            }

            @Override
            public Long getTo() {
                return null;
            }
        }, responseDocuments);

        Assert.assertNotNull(modified);
        Assert.assertEquals(3, modified.size());
        Assert.assertEquals(1, modified.get(0).getRoot().asInt());
        Assert.assertEquals(2, modified.get(1).getRoot().asInt());
        Assert.assertEquals(3, modified.get(2).getRoot().asInt());
    }

    @Test
    public void testApplyRange_FromNull_ToOne() {
        List<JsonDoc> responseDocuments = new ArrayList<>();
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(1)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(2)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(3)));

        List<JsonDoc> modified = mediator.applyRange(
                new WithRange() {
            @Override
            public Long getFrom() {
                return null;
            }

            @Override
            public Long getTo() {
                return 1L;
            }
        }, responseDocuments);

        Assert.assertNotNull(modified);
        Assert.assertEquals(2, modified.size());
        Assert.assertEquals(1, modified.get(0).getRoot().asInt());
        Assert.assertEquals(2, modified.get(1).getRoot().asInt());
    }

    @Test
    public void testApplyRange_FromZero_ToOne() {
        List<JsonDoc> responseDocuments = new ArrayList<>();
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(1)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(2)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(3)));

        List<JsonDoc> modified = mediator.applyRange(
                new WithRange() {
            @Override
            public Long getFrom() {
                return 0L;
            }

            @Override
            public Long getTo() {
                return 1L;
            }
        }, responseDocuments);

        Assert.assertNotNull(modified);
        Assert.assertEquals(2, modified.size());
        Assert.assertEquals(1, modified.get(0).getRoot().asInt());
        Assert.assertEquals(2, modified.get(1).getRoot().asInt());
    }

    @Test
    public void testApplyRange_FromOne_ToNull() {
        List<JsonDoc> responseDocuments = new ArrayList<>();
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(1)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(2)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(3)));

        List<JsonDoc> modified = mediator.applyRange(
                new WithRange() {
            @Override
            public Long getFrom() {
                return 1L;
            }

            @Override
            public Long getTo() {
                return null;
            }
        }, responseDocuments);

        Assert.assertNotNull(modified);
        Assert.assertEquals(2, modified.size());
        Assert.assertEquals(2, modified.get(0).getRoot().asInt());
        Assert.assertEquals(3, modified.get(1).getRoot().asInt());
    }

    @Test
    public void testApplyRange_FromOne_ToTwo() {
        List<JsonDoc> responseDocuments = new ArrayList<>();
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(1)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(2)));
        responseDocuments.add(new JsonDoc(JsonNodeFactory.instance.numberNode(3)));

        List<JsonDoc> modified = mediator.applyRange(
                new WithRange() {
            @Override
            public Long getFrom() {
                return 1L;
            }

            @Override
            public Long getTo() {
                return 2L;
            }
        }, responseDocuments);

        Assert.assertNotNull(modified);
        Assert.assertEquals(2, modified.size());
        Assert.assertEquals(2, modified.get(0).getRoot().asInt());
        Assert.assertEquals(3, modified.get(1).getRoot().asInt());
    }
}
