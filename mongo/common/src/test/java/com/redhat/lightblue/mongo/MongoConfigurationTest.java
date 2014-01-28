package com.redhat.lightblue.mongo;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.mongo.MongoConfiguration.Server;

public class MongoConfigurationTest {

    MongoConfiguration config;
    List<Server> servers;
    Server server;
    
    @Before
    public void setUp() throws Exception {
        config = new MongoConfiguration();
        config.setName("name");
        config.setCollection("collection");
        config.setConnectionsPerHost(10);
        server = new MongoConfiguration.Server();
        servers = new ArrayList<Server>();
        servers.add(server);
        config.setServers(servers);

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
        Assert.assertEquals("name", config.getName());
    }

}
