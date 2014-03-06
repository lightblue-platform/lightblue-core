package com.redhat.lightblue.metadata.mongo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MongoDataStoreTest {

    MongoDataStore dataStore;

    @Before
    public void setUp() throws Exception {
        dataStore = new MongoDataStore("jndiName", "databaseName", "collectionName");
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
        Assert.assertEquals("databaseName:collectionName@jndiName", dataStore.toString());
    }

    @Test
    public void testEqualsObject() {
        MongoDataStore dataStore2 = new MongoDataStore();
        dataStore2.setClientJndiName("jndiName");
        dataStore2.setCollectionName("collectionName");
        dataStore2.setDatabaseName("databaseName");

        Assert.assertEquals(dataStore2, dataStore);
    }

}
