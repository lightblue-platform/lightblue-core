/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    }
}
