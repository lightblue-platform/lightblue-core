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
package com.redhat.lightblue.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.VersionInfo;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import static com.redhat.lightblue.util.JsonUtils.json;
import com.redhat.lightblue.util.test.FileUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Test the AbstractMetadataConfiguration functionality.
 *
 * @author nmalik
 */
public class MetadataConfigurationTest {

    private static class TestMetadata implements Metadata {

        public Extensions extensions;

        public TestMetadata(Extensions ext) {
            extensions = ext;
        }

        @Override
        public Response getDependencies(String entityName, String version) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Response getAccess(String entityName, String version) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public EntityInfo getEntityInfo(String entityName) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public String[] getEntityNames(MetadataStatus... statuses) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public VersionInfo[] getEntityVersions(String entityName) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void createNewMetadata(EntityMetadata md) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void createNewSchema(EntityMetadata md) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void updateEntityInfo(EntityInfo ei) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void setMetadataStatus(String entityName, String version, MetadataStatus newStatus, String comment) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void removeEntity(String entityName) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<String, List<String>> getMappedRoles() {
            return null;
        }

    }

    private static class TestConfig extends AbstractMetadataConfiguration {

        @Override
        public Metadata createMetadata(DataSourcesConfiguration ds, JSONMetadataParser parser, LightblueFactory mgr) {
            Extensions ext = new Extensions();
            registerWithExtensions(ext);
            return new TestMetadata(ext);
        }
    }

    @Test
    public void testExtensions() throws Exception {
        // load configuration
        String jsonString = FileUtil.readFile(MetadataConfiguration.FILENAME);
        JsonNode node = json(jsonString);

        // initialize config
        TestConfig config = new TestConfig();
        config.initializeFromJson(node);

        // create metadata
        TestMetadata metadata = (TestMetadata) config.createMetadata(null, null, null);

        // verify extensions
        Assert.assertNotNull(metadata.extensions.getHookConfigurationParser(TestHookConfigurationParser.HOOK_NAME));

        Assert.assertNotNull(metadata.extensions.getPropertyParser("TestPropertyParser"));
        Assert.assertNotNull(metadata.extensions.getDataStoreParser("TestDataStoreParser"));
    }
}
