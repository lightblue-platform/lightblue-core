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
package com.redhat.lightblue.mongo.config;

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
        config.setDatabase("database");
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
    public void testGetDatabase() {
        Assert.assertEquals("database", config.getDatabase());
    }

    @Test
    public void testSetDatabase() {
        config.setDatabase("newDatabase");

        Assert.assertEquals("newDatabase", config.getDatabase());
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
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        builder.connectionsPerHost(10);

        Assert.assertEquals(builder.build(), config.getMongoClientOptions());
    }

    @Test
    public void testGetMongoClientOptionsConnectionsPerHostNull() {
        config.setConnectionsPerHost(null);
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

        Assert.assertEquals(config.getDB().toString(), client.getDB("database").toString());
    }
}
