/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.test.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;

public class FakeMetadataTest {

    @Test
    public void testEntityInfo_VersionDoesNotExist() {
        assertFalse(new FakeMetadata().checkVersionExists("fake", "1.0.0"));
    }

    @Test
    public void testEntityInfo_VersionDoesExist() {
        String entityName = "fake";
        String version1 = "1.0.0";

        FakeMetadata metadata = new FakeMetadata();

        EntityInfo entityInfo = new EntityInfo(entityName);
        metadata.setEntityInfo(entityInfo);

        assertFalse(metadata.checkVersionExists(entityName, version1));

        metadata.setEntityMetadata(entityName, version1, new EntityMetadata("fake EntityMetadata"));

        assertTrue(metadata.checkVersionExists(entityName, version1));
    }

    @Test(expected = IllegalStateException.class)
    public void testEntityInfo_DoesNotExist() {
        String entityName = "fake";
        String version1 = "1.0.0";

        FakeMetadata metadata = new FakeMetadata();

        metadata.setEntityMetadata(entityName, version1, new EntityMetadata("fake EntityMetadata"));
    }

    @Test
    public void testRemoveEntity() {
        String entityName = "fake";

        FakeMetadata metadata = new FakeMetadata();

        EntityInfo entityInfo = new EntityInfo(entityName);
        metadata.setEntityInfo(entityInfo);

        assertNotNull(metadata.getEntityInfo(entityName));

        metadata.removeEntity(entityName);

        assertNull(metadata.getEntityInfo(entityName));
    }

    @Test
    public void testRemoveEntity_ButDoesNotExist() {
        FakeMetadata metadata = new FakeMetadata();

        metadata.removeEntity("fake");

        //Nothing should happen and no exception should be thrown.
    }

    @Test
    public void testUpdateEntityInfo() {
        String entityName = "fake";

        FakeMetadata metadata = new FakeMetadata();

        EntityInfo entityInfo1 = new EntityInfo(entityName);
        metadata.setEntityInfo(entityInfo1);

        assertEquals(entityInfo1, metadata.getEntityInfo(entityName));

        EntityInfo entityInfo2 = new EntityInfo(entityName);
        metadata.updateEntityInfo(entityInfo2);

        assertEquals(entityInfo2, metadata.getEntityInfo(entityName));
    }

    @Test(expected = IllegalStateException.class)
    public void testUpdateEntityInfo_ThatDoesNotExist() {
        FakeMetadata metadata = new FakeMetadata();

        metadata.updateEntityInfo(new EntityInfo("fake"));
    }

    @Test
    public void testEntityMetadata() {
        String entityName = "fake";
        String version1 = "1.0.0";
        String version2 = "2.0.0";

        FakeMetadata metadata = new FakeMetadata();

        EntityInfo entityInfo = new EntityInfo(entityName);
        metadata.setEntityInfo(entityInfo);

        assertEquals(entityInfo, metadata.getEntityInfo(entityName));

        EntityMetadata entityMetadata = new EntityMetadata("fake EntityMetadata");
        metadata.setEntityMetadata(entityName, version1, entityMetadata);

        assertEquals(entityMetadata, metadata.getEntityMetadata(entityName, version1));
        assertNull(metadata.getEntityMetadata(entityName, version2));
    }

    @Test
    public void testDependencies() {
        String entityName = "fake";
        String version1 = "1.0.0";
        String version2 = "2.0.0";

        FakeMetadata metadata = new FakeMetadata();

        EntityInfo entityInfo = new EntityInfo(entityName);
        metadata.setEntityInfo(entityInfo);

        assertEquals(entityInfo, metadata.getEntityInfo(entityName));

        Response dependencies = new Response(JsonNodeFactory.instance, OperationStatus.ERROR);
        metadata.setDependencies(entityName, version1, dependencies);

        assertEquals(dependencies, metadata.getDependencies(entityName, version1));
        assertNull(metadata.getDependencies(entityName, version2));
    }

    @Test
    public void testAccess() {
        String entityName = "fake";
        String version1 = "1.0.0";
        String version2 = "2.0.0";

        FakeMetadata metadata = new FakeMetadata();

        EntityInfo entityInfo = new EntityInfo(entityName);
        metadata.setEntityInfo(entityInfo);

        assertEquals(entityInfo, metadata.getEntityInfo(entityName));

        Response access = new Response(JsonNodeFactory.instance, OperationStatus.ERROR);
        metadata.setAccess(entityName, version1, access);

        assertEquals(access, metadata.getAccess(entityName, version1));
        assertNull(metadata.getAccess(entityName, version2));
    }

}
