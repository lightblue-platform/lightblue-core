/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.redhat.lightblue.config.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.redhat.lightblue.config.DataSourceConfiguration;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
public class RDBMSDataSourceConfiguration implements DataSourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSDataSourceConfiguration.class);

    private final List<ServerAddress> servers = new ArrayList<>();
    private ServerAddress theServer = null;

    private Integer connectionsPerHost;
    private String database;
    private List<MongoCredential> credentials;
    private boolean ssl = Boolean.FALSE;
    private boolean noCertValidation = Boolean.FALSE;
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

    public List<MongoCredential> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<MongoCredential> l) {
        credentials = l;
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
    public boolean isSsl() {
        return ssl;
    }

    /**
     * @param ssl the ssl to set
     */
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * If true, ssl certs are not validated
     */
    public boolean isNoCertValidation() {
        return noCertValidation;
    }

    /**
     * If true, ssl certs are not validated
     */
    public void setNoCertValidation(boolean b) {
        noCertValidation = b;
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

    private static TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
            }
        }
    };

    private SocketFactory getSocketFactory() {
        try {
            if (noCertValidation) {
                LOGGER.warn("Certificate validation is off, don't use this in production");
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                return sc.getSocketFactory();
            } else {
                return SSLSocketFactory.getDefault();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

        if (ssl) {
            // taken from MongoClientURI, written this way so we don't have to
            // construct a URI to connect
            builder.socketFactory(getSocketFactory());
        }

        return builder.build();
    }

    public MongoClient getMongoClient() throws UnknownHostException {
        MongoClientOptions options = getMongoClientOptions();
        LOGGER.debug("getMongoClient with server: {}, servers:{} and options:{}", theServer, servers, options);
        if (theServer != null) {
            return new MongoClient(theServer, credentials, options);
        } else {
            return new MongoClient(servers, credentials, options);
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
        bld.append("connectionsPerHost:").append(connectionsPerHost).append('\n').
                append("database:").append(database).append('\n').
                append("ssl:").append(ssl).append('\n').
                append("noCertValidation:").append(noCertValidation);
        bld.append("credentials:");
        boolean first = true;
        for (MongoCredential c : credentials) {
            if (first) {
                first = false;
            } else {
                bld.append(',');
            }
            bld.append(toString(c));
        }
        return bld.toString();
    }

    public static MongoCredential credentialFromJson(ObjectNode node) {
        String userName = null;
        String password = null;
        String source = null;

        JsonNode xnode = node.get("mechanism");
        if (xnode == null) {
            throw new IllegalArgumentException("mechanism is required in credentials");
        }
        String mech = xnode.asText();
        xnode = node.get("userName");
        if (xnode != null) {
            userName = xnode.asText();
        }
        xnode = node.get("password");
        if (xnode != null) {
            password = xnode.asText();
        }
        xnode = node.get("source");
        if (xnode != null) {
            source = xnode.asText();
        }

        MongoCredential cr;
        if ("GSSAPI_MECHANISM".equals(mech)) {
            cr = MongoCredential.createGSSAPICredential(userName);
        } else if ("MONGODB_CR_MECHANISM".equals(mech)) {
            cr = MongoCredential.createMongoCRCredential(userName, source,
                    password == null ? null : password.toCharArray());
        } else if ("MONGODB_X509_MECHANISM".equals(mech)) {
            cr = MongoCredential.createMongoX509Credential(userName);
        } else if ("PLAIN_MECHANISM".equals(mech)) {
            cr = MongoCredential.createPlainCredential(userName, source,
                    password == null ? null : password.toCharArray());
        } else {
            throw new IllegalArgumentException("invalid mechanism:" + mech + ", must be one of "
                    + "GSSAPI_MECHANISM, MONGODB_CR_MECHANISM, "
                    + "MONGODB_X5090_MECHANISM, or PLAIN_MECHANISM");
        }
        return cr;
    }

    public static List<MongoCredential> credentialsFromJson(JsonNode node) {
        List<MongoCredential> list = new ArrayList<>();
        try {
            if (node instanceof ArrayNode) {
                for (Iterator<JsonNode> itr = node.elements(); itr.hasNext();) {
                    list.add(credentialFromJson((ObjectNode) itr.next()));
                }
            } else if (node != null) {
                list.add(credentialFromJson((ObjectNode) node));
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Invalid credentials node:" + node);
        }
        return list;
    }

    public static String toString(MongoCredential cr) {
        StringBuilder bld = new StringBuilder();
        bld.append("{mechanism:").append(cr.getMechanism());
        if (cr.getUserName() != null) {
            bld.append(" userName:").append(cr.getUserName());
        }
        if (cr.getPassword() != null) {
            bld.append(" password:").append(cr.getPassword());
        }
        if (cr.getSource() != null) {
            bld.append(" source:").append(cr.getSource());
        }
        bld.append('}');
        return bld.toString();
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        if (node != null) {
            JsonNode x = node.get("connectionsPerHost");
            if (x != null) {
                connectionsPerHost = x.asInt();
            }
            x = node.get("ssl");
            if (x != null) {
                ssl = x.asBoolean();
            }
            x = node.get("noCertValidation");
            if (x != null) {
                noCertValidation = x.asBoolean();
            }
            credentials = credentialsFromJson(node.get("credentials"));
            x = node.get("metadataDataStoreParser");
            try {
                if (x != null) {
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
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
}
