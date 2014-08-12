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
package com.redhat.lightblue.config.rdbms;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.common.rdbms.RDBMSUtils;
import com.redhat.lightblue.config.DataSourceConfiguration;
import com.redhat.lightblue.metadata.rdbms.impl.RDBMSDataStoreParser;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lcestari
 */
public class RDBMSDataSourceConfiguration implements DataSourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSDataSourceConfiguration.class);

    private final Map<String, String> dataSourceJDNIMap = new HashMap<>();
    private String databaseName;
    private Class metadataDataStoreParser = RDBMSDataStoreParser.class;

    @Override
    public void initializeFromJson(JsonNode node) {
        if (node != null) {
            JsonNode x = node.get("metadataDataStoreParser");
            try {
                if (x != null) {
                    metadataDataStoreParser = Class.forName(x.asText());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(node.toString() + ":" + e);
            }
            x = node.get("databaseName");
            if (x != null) {
                databaseName = x.asText();
            }
            JsonNode jsonNodeServers = node.get("connections");
            if (jsonNodeServers != null && jsonNodeServers.isArray()) {
                Iterator<JsonNode> elements = jsonNodeServers.elements();
                while (elements.hasNext()) {
                    JsonNode next = elements.next();
                    String datasourceName;
                    String JNDI;
                    x = next.get("datasourceName");
                    if (x != null) {
                        datasourceName = x.asText();
                    } else {
                        throw new IllegalStateException("No datasourceName was found: " + node.toString());
                    }

                    x = next.get("JNDI");
                    if (x != null) {
                        JNDI = x.asText();
                    } else {
                        throw new IllegalStateException("No datasourceName was found: " + node.toString());
                    }
                    dataSourceJDNIMap.put(datasourceName, JNDI);
                }
            } else {
                throw new IllegalStateException("No connection was found: " + node.toString());
            }
        }
    }

    public DataSource getDataSource(String name) {
        DataSource ds = null;
        String datasource = null;
        if (databaseName.equals(name)) {
            datasource = dataSourceJDNIMap.entrySet().iterator().next().getValue();
        } else {
            datasource = name;
        }
        ds = RDBMSUtils.getDataSource(datasource);
        return ds;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public Class getMetadataDataStoreParser() {
        return metadataDataStoreParser;
    }

    public void setMetadataDataStoreParser(Class metadataDataStoreParser) {
        this.metadataDataStoreParser = metadataDataStoreParser;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.dataSourceJDNIMap);
        hash = 37 * hash + Objects.hashCode(this.databaseName);
        hash = 37 * hash + Objects.hashCode(this.metadataDataStoreParser);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RDBMSDataSourceConfiguration other = (RDBMSDataSourceConfiguration) obj;
        if (!Objects.equals(this.dataSourceJDNIMap, other.dataSourceJDNIMap)) {
            return false;
        }
        if (!Objects.equals(this.databaseName, other.databaseName)) {
            return false;
        }
        if (!Objects.equals(this.metadataDataStoreParser, other.metadataDataStoreParser)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RDBMSDataSourceConfiguration{" + "dataSourceJDNIMap=" + dataSourceJDNIMap + ", databaseName=" + databaseName + ", metadataDataStoreParser=" + metadataDataStoreParser + '}';
    }
}
