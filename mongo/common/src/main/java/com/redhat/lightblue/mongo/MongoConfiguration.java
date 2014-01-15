/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TODO revisit having spun this out into a common module.  May only be needed by metadata.
 * @author nmalik
 */
public class MongoConfiguration {
    public class Server {
        private String hostname = "localhost";
        private String port = "27017";

        public ServerAddress toServerAddress() throws UnknownHostException {
            return new ServerAddress(hostname, Integer.valueOf(port));
        }
    }

    private String name;
    private List<Server> servers;
    private String collection;
    private Integer connectionsPerHost;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the collection
     */
    public String getCollection() {
        return collection;
    }

    /**
     * @param collection the collection to set
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * @return the servers
     */
    public Iterator<Server> getServers() {
        return servers.iterator();
    }

    /**
     * @param servers the servers to set
     */
    public void setServers(List<Server> servers) {
        this.servers = servers;
    }

    public List<ServerAddress> getServerAddresses() throws UnknownHostException {
        List<ServerAddress> serverAddresses = new ArrayList<>();

        if (this.servers != null) {
            for (Server server : servers) {
                serverAddresses.add(server.toServerAddress());
            }
        }

        return serverAddresses;
    }

    /**
     * @return the connectionsPerHost
     */
    public Integer getConnectionsPerHost() {
        return connectionsPerHost;
    }

    /**
     * @param connectionsPerHost the connectionsPerHost to set
     */
    public void setConnectionsPerHost(Integer connectionsPerHost) {
        this.connectionsPerHost = connectionsPerHost;
    }

    /**
     * Returns an options object with defaults overriden where there is a valid override.
     *
     * @return
     */
    public MongoClientOptions getMongoClientOptions() {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        if (connectionsPerHost != null) {
            builder.connectionsPerHost(connectionsPerHost);
        }

        return builder.build();
    }

    public DB getDB() throws UnknownHostException {
        MongoClient client = new MongoClient(getServerAddresses(), getMongoClientOptions());
        DB db = client.getDB(getName());
        return db;
    }
}
