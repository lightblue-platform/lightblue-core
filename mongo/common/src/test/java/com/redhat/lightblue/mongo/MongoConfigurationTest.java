package com.redhat.lightblue.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

public class MongoConfigurationTest {

    MongoConfiguration config;
    List<ServerAddress> servers;
    ServerAddress serverAddress;

    @Before
    public void setUp() throws Exception {
        config = new MongoConfiguration();
        config.setName("name");
        config.setCollection("collection");
        config.setConnectionsPerHost(10);
        serverAddress = new ServerAddress("localhost", 27017);
        servers = new ArrayList<>();
        servers.add(serverAddress);
        config.addServerAddress("localhost", 27017);

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("name", config.getName());
    }

    @Test
    public void testSetName() {
        config.setName("newName");

        Assert.assertEquals("newName", config.getName());
    }

    @Test
    public void testGetCollection() {
        Assert.assertEquals("collection", config.getCollection());
    }

    @Test
    public void testSetCollection() {
        config.setCollection("newCollecton");

        Assert.assertEquals("newCollecton", config.getCollection());
    }

    @Test
    public void testGetConnectionsPerHost() {
        Assert.assertEquals(new Integer(10), config.getConnectionsPerHost());
    }

    @Test
    public void testSetConnectionsPerHost() {
        config.setConnectionsPerHost(100);

        Assert.assertEquals(new Integer(100), config.getConnectionsPerHost());
    }

    @Test
    public void testGetMongoClientOptions() {
        config.setSsl(null);
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        builder.connectionsPerHost(10);

        Assert.assertEquals(builder.build(), config.getMongoClientOptions());
    }

    @Test
    public void testGetMongoClientOptionsConnectionsPerHostNull() {
        config.setConnectionsPerHost(null);
        config.setSsl(null);
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        Assert.assertEquals(builder.build(), config.getMongoClientOptions());
    }

    @Test
    public void testGetServerAddresses() {
        Iterator<ServerAddress> serversFromConfig = config.getServerAddresses();
        int i = 0;
        while (serversFromConfig.hasNext()) {
            Assert.assertEquals(serversFromConfig.next(), servers.get(i));
            i++;
        }
    }

    @Test
    public void testGetServersNull() throws UnknownHostException {
        config.clearServerAddresses();
        Iterator<ServerAddress> itr = config.getServerAddresses();

        Assert.assertFalse(itr.hasNext());
    }

    @Test
    public void testGetDb() throws UnknownHostException {
        List<ServerAddress> addresses = new ArrayList<>();
        Iterator<ServerAddress> itr = config.getServerAddresses();

        while (itr.hasNext()) {
            addresses.add(itr.next());
        }

        MongoClient client = new MongoClient(addresses, config.getMongoClientOptions());

        Assert.assertEquals(config.getDB().toString(), client.getDB("name").toString());
    }
}
