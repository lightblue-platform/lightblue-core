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


import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.PredefinedFields;

import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CRUDInsertionResponse;
import com.redhat.lightblue.crud.CRUDSaveResponse;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.validator.EmptyEntityConstraintValidators;

import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.OperationStatus;

import com.redhat.lightblue.util.test.AbstractJsonSchemaTest;

public class MediatorTest extends AbstractJsonSchemaTest {
    private Mediator mediator;
    private final TestMetadata mdManager = new TestMetadata();
    private final MockCrudController mockCrudController = new MockCrudController();

    private static final JsonNodeFactory nodeFactory = JsonNodeFactory.withExactBigDecimals(false);

    private static final class TestMetadata implements Metadata {
        private EntityMetadata md;

        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version, boolean forceVersion) {
            return md;
        }

        @Override
        public String[] getEntityNames() {
            return null;
        }

        @Override
        public Version[] getEntityVersions(String entityName) {
            return null;
        }

        @Override
        public void createNewMetadata(EntityMetadata md) {
        }

        @Override
        public void setMetadataStatus(String entityName,
                                      String version,
                                      MetadataStatus newStatus,
                                      String comment) {
        }

        @Override
        public Response getDependencies() {
            return null;
        }

        @Override
        public Response getDependencies(String entityName) {
            return null;
        }

        @Override
        public Response getDependnecies(String entityName, String version) {
            return null;
        }

        @Override
        public Response getAccess() {
            return null;
        }

        @Override
        public Response getAccess(String entityName) {
            return null;
        }

        @Override
        public Response getAccess(String entityName, String version) {
            return null;
        }
    }

    private static final class MockCrudController implements CRUDController {
        CRUDUpdateResponse updateResponse;
        CRUDDeleteResponse deleteResponse;
        CRUDFindResponse findResponse;

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
    }

    private EntityMetadata getMd(String fname) throws Exception {
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
    public void initMediator() throws Exception {
        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
        factory.addEntityConstraintValidators(new EmptyEntityConstraintValidators());
        factory.addCRUDController("mongo", mockCrudController);
        mdManager.md = getMd("./testMetadata.json");
        mediator = new Mediator(mdManager, factory);
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
}
