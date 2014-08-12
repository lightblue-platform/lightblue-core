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

public class MongoDataStoreTest {

    MongoDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        dataStore = new MongoDataStore("databaseName", "datasourceName", "collectionName");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testHashCode() {

    }

    @Test
    public void testGetBackend() {
        Assert.assertEquals("mongo", dataStore.getBackend());
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
    public void testEqualsObject() {
        MongoDataStore dataStore2 = new MongoDataStore();
        dataStore2.setCollectionName("collectionName");
        dataStore2.setDatabaseName("databaseName");
        dataStore2.setDatasourceName("datasourceName");

        Assert.assertEquals(dataStore2, dataStore);
    }

}
