/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.rest.metadata.hystrix;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.Version;

/**
 *
 * @author nmalik
 */
public abstract class AbstractRestCommandTest {
    public static class TestMetadata implements Metadata {
        public Object[] args;

        @Override
        public Response getDependencies(String entityName, String version) {
            args = new Object[]{entityName, version};
            return new Response();
        }

        @Override
        public Response getAccess(String entityName, String version) {
            args = new Object[]{entityName, version};
            return new Response();
        }

        @Override
        public EntityMetadata getEntityMetadata(String entityName, String version) {
            args = new Object[]{entityName, version};
            return new EntityMetadata(entityName);
        }

        @Override
        public EntityInfo getEntityInfo(String entityName) {
            args = new Object[]{entityName};
            return new EntityInfo(entityName);
        }

        @Override
        public String[] getEntityNames() {
            args = new Object[]{};
            return new String[]{};
        }

        @Override
        public Version[] getEntityVersions(String entityName) {
            args = new Object[]{entityName};
            return new Version[]{};
        }

        @Override
        public void createNewMetadata(EntityMetadata md) {
            args = new Object[]{md};
        }

        @Override
        public void createNewSchema(EntityMetadata md) {
            args = new Object[]{md};
        }

        @Override
        public void updateEntityInfo(EntityInfo ei) {
            args = new Object[]{ei};
        }

        @Override
        public void setMetadataStatus(String entityName, String version, MetadataStatus newStatus, String comment) {
            args = new Object[]{entityName, version, newStatus, comment};
        }
    }

    protected TestMetadata metadata = new TestMetadata();
}
