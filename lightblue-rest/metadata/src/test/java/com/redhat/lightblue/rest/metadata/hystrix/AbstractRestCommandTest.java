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
package com.redhat.lightblue.rest.metadata.hystrix;

import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.VersionInfo;

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
        public String[] getEntityNames(MetadataStatus... statuses) {
            args = new Object[]{};
            return new String[]{};
        }

        @Override
        public VersionInfo[] getEntityVersions(String entityName) {
            args = new Object[]{entityName};
            return new VersionInfo[]{};
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

        @Override
        public void removeEntity(String entityName) {
            args = new Object[]{entityName};
        }
    }

    protected TestMetadata metadata = new TestMetadata();
}
