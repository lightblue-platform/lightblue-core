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
package com.redhat.lightblue.mongo.config.metadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSocketFactory;
import org.bson.BSONObject;

/**
 * TODO revisit having spun this out into a common module. May only be needed by metadata.
 *
 * @author nmalik
 */
public class MongoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConfiguration.class);

    private String name;
    private transient final List<ServerAddress> servers = new ArrayList<>();
    private final List<String> serversConfigurations = new ArrayList<>();
    private String collection;
    private Integer connectionsPerHost;
    private Boolean ssl = Boolean.TRUE;

    public static MongoMetadata create(MongoConfiguration configuration) throws UnknownHostException {
        DB db = configuration.getDB();
        Extensions<BSONObject> parserExtensions = new Extensions<>();
        parserExtensions.addDefaultExtensions();
        parserExtensions.registerDataStoreParser("mongo", new MongoDataStoreParser<BSONObject>());
        DefaultTypes typeResolver = new DefaultTypes();
        return new MongoMetadata(db, parserExtensions, typeResolver);
    }
    
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

    public void addServerAddress(String hostname, Integer port) throws UnknownHostException {
        this.servers.add(new ServerAddress(hostname, port));
        this.serversConfigurations.add(hostname+":"+port)       ;
    }

    /**
     * @return the servers
     */
    public Iterator<ServerAddress> getServerAddresses() {
        return servers.iterator();
    }

    public void clearServerAddresses() {
        this.servers.clear();
        this.serversConfigurations.clear();
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
     * Returns an options object with defaults overriden where there is a valid override.
     *
     * @return
     */
    public MongoClientOptions getMongoClientOptions() {
        if(this.servers.size() == 0) {
            for (String configuration : serversConfigurations) {
                String[] confSplit = configuration.split(":");
                if(confSplit == null || confSplit.length != 2){
                    throw new IllegalStateException("Illegal serversConfigurations->"+configuration);
                }
                try {
                    this.servers.add(new ServerAddress(confSplit[0], Integer.parseInt(confSplit[1])));
                } catch (UnknownHostException | NumberFormatException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        MongoClientOptions.Builder builder = MongoClientOptions.builder();

        if (connectionsPerHost != null) {
            builder.connectionsPerHost(connectionsPerHost);
        }

        if (ssl != null && ssl.booleanValue()) {
            // taken from MongoClientURI, written this way so we don't have to construct a URI to connect
            builder.socketFactory(SSLSocketFactory.getDefault());
        }

        return builder.build();
    }

    public DB getDB() throws UnknownHostException {
        MongoClientOptions options=getMongoClientOptions();
        LOGGER.debug("getDB with servers:{} and options:{}",servers,options);
        MongoClient client = new MongoClient(servers, options);
        return client.getDB(getName());
    }

    public String toString() {
        StringBuilder bld=new StringBuilder();
        bld.append("name:").append(name).append('\n').
            append("servers:").append(servers).append('\n').
            append("serversConfigurations:").append(serversConfigurations).append('\n').
            append("collection:").append(collection).append('\n').
            append("connectionsPerHost:").append(connectionsPerHost).append('\n').
            append("ssl:").append(ssl);
        return bld.toString();
    }
            
}
