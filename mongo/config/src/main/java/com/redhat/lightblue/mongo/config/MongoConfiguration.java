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

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.util.JsonInitializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.MongoClientOptions;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.config.common.DataSourceConfiguration;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import org.bson.BSONObject;

/**
 * Mongo client makes a distinction between contructing using a list of
 * ServerAddress objects, and a single ServerAddress object. If you contruct
 * with a List, it wants access to all the nodes in the replica set. If you
 * construct with a single ServerAddress, it only talks to that server. So, we
 * make a distinction between array of server addresses and a single server
 * address.
 * 
 * 
 * @author nmalik
 */
public class MongoConfiguration implements DataSourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConfiguration.class);

    private final List<ServerAddress> servers = new ArrayList<>();
    private ServerAddress theServer = null;

    private Integer connectionsPerHost;
    private String database;
    private Boolean ssl = Boolean.FALSE;
    private Class metadataDataStoreParser = MongoDataStoreParser.class;

    public void addServerAddress(String hostname, int port) throws UnknownHostException {
        this.servers.add(new ServerAddress(hostname, port));
    }

    public void addServerAddress(String hostname) throws UnknownHostException {
        this.servers.add(new ServerAddress(hostname));
    }

    public void setServer(String hostname, int port) throws UnknownHostException {
        theServer = new ServerAddress(hostname, port);
    }

    public void setServer(String hostname) throws UnknownHostException {
        theServer = new ServerAddress(hostname);
    }

    /**
     * @return the servers
     */
    public Iterator<ServerAddress> getServerAddresses() {
        return servers.iterator();
    }

    public void clearServerAddresses() {
        this.servers.clear();
    }

    public ServerAddress getServer() {
        return theServer;
    }

    @Override
    public Class<DataStoreParser> getMetadataDataStoreParser() {
        return metadataDataStoreParser;
    }

    public void setMetadataDataStoreParser(Class<DataStoreParser> clazz) {
        metadataDataStoreParser = clazz;
    }

    /**
     * @return the connectionsPerHost
     */
    public Integer getConnectionsPerHost() {
        return connectionsPerHost;
    }

    /**
     * @param connectionsPerHost
     *            the connectionsPerHost to set
     */
    public void setConnectionsPerHost(Integer connectionsPerHost) {
        this.connectionsPerHost = connectionsPerHost;
    }

    /**
     * @return the ssl
     */
    public Boolean getSsl() {
        return ssl;
    }

    /**
     * @param ssl the ssl to set
     */
    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * The database name
     */
    public String getDatabase() {
        return database;
    }

    /**
     * The database name
     */
    public void setDatabase(String s) {
        database = s;
    }

    /**
     * Returns an options object with defaults overriden where there is a valid
     * override.
     * 
     * @return
     */
    public MongoClientOptions getMongoClientOptions() {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        if (connectionsPerHost != null) {
            builder.connectionsPerHost(connectionsPerHost);
        }

        if (ssl != null && ssl) {
            // taken from MongoClientURI, written this way so we don't have to
            // construct a URI to connect
            builder.socketFactory(SSLSocketFactory.getDefault());
        }

        return builder.build();
    }

    public MongoClient getMongoClient() throws UnknownHostException {
        MongoClientOptions options = getMongoClientOptions();
        LOGGER.debug("getMongoClient with servers:{} and options:{}", servers, options);
        if (theServer != null){
            return new MongoClient(theServer, options);
        }
        else {
            return new MongoClient(servers, options);
        }
    }

    public DB getDB() throws UnknownHostException {
        return getMongoClient().getDB(database);
    }

    public String toString() {
        StringBuilder bld = new StringBuilder();
        if (theServer != null) {
            bld.append("server").append(theServer).append('\n');
        } else {
            bld.append("servers:").append(servers).append('\n');
        }
        bld.append("connectionsPerHost:").append(connectionsPerHost).append('\n').append("database:").append(database).append('\n').append("ssl:").append(ssl);
        return bld.toString();
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        if (node != null) {
            JsonNode x = node.get("connectionsPerHost");
            if (x != null){
                connectionsPerHost = x.asInt();
            }
            x = node.get("ssl");
            if (x != null) {
                ssl = x.asBoolean();
            }
            x = node.get("metadataDataStoreParser");
            try {
                if (x != null){
                    metadataDataStoreParser = Class.forName(x.asText());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(node.toString() + ":" + e);
            }
            x = node.get("database");
            if (x != null) {
                database = x.asText();
            }
            JsonNode jsonNodeServers = node.get("servers");
            if (jsonNodeServers != null && jsonNodeServers.isArray()) {
                Iterator<JsonNode> elements = jsonNodeServers.elements();
                while (elements.hasNext()) {
                    JsonNode next = elements.next();
                    try {
                        String host;
                        x = next.get("host");
                        if (x != null) {
                            host = x.asText();
                        } else {
                            host = null;
                        }

                        x = next.get("port");
                        if (x != null) {
                            addServerAddress(host, x.asInt());
                        } else {
                            addServerAddress(host);
                        }
                    } catch (UnknownHostException e) {
                        throw new IllegalStateException(e);
                    }
                }

            } else {
                JsonNode server = node.get("server");
                if (server != null) {
                    try {
                        x = server.get("host");
                        if (x != null) {
                            String host = x.asText();
                            x = server.get("port");
                            if (x != null) {
                                setServer(host, x.asInt());
                            } else {
                                setServer(host);
                            }
                        } else {
                            throw new IllegalStateException("host is required in server");
                        }
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
}
