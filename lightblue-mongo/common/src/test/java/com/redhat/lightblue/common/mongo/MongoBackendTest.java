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
package com.redhat.lightblue.common.mongo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MongoBackendTest {

    MongoBackend dataStore;

    @Before
    public void setUp() throws Exception {
        dataStore = new MongoBackend("jndiName", "databaseName", "datasourceName", "collectionName");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testHashCode() {

    }

    @Test
    public void testGetType() {
        Assert.assertEquals("mongo", dataStore.getType());
    }

    @Test
    public void testGetClientJndiName() {
        Assert.assertEquals("jndiName", dataStore.getClientJndiName());
    }

    @Test
    public void testSetClientJndiName() {
        dataStore.setClientJndiName("newJndiName");

        Assert.assertEquals("newJndiName", dataStore.getClientJndiName());
    }

    @Test
    public void testGetDatabaseName() {
        Assert.assertEquals("databaseName", dataStore.getDatabaseName());
    }

    @Test
    public void testSetDatabaseName() {
        dataStore.setDatabaseName("newDatabaseName");

        Assert.assertEquals("newDatabaseName", dataStore.getDatabaseName());
    }

    @Test
    public void testGetCollectionName() {
        Assert.assertEquals("collectionName", dataStore.getCollectionName());
    }

    @Test
    public void testSetCollectionName() {
        dataStore.setCollectionName("newCollectionName");

        Assert.assertEquals("newCollectionName", dataStore.getCollectionName());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("datasourceName:databaseName:collectionName@jndiName", dataStore.toString());
    }

    @Test
    public void testEqualsObject() {
        MongoBackend dataStore2 = new MongoBackend();
        dataStore2.setClientJndiName("jndiName");
        dataStore2.setCollectionName("collectionName");
        dataStore2.setDatabaseName("databaseName");
        dataStore2.setDatasourceName("datasourceName");

        Assert.assertEquals(dataStore2, dataStore);
    }

}
