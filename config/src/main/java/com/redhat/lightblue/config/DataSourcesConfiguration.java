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
package com.redhat.lightblue.config;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonInitializable;

/**
 * Keeps all datasource configurations
 */
public class DataSourcesConfiguration implements JsonInitializable, Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourcesConfiguration.class);

    private final HashMap<String, DataSourceConfiguration> datasources = new HashMap<>();

    /**
     * Initalize an empty datasource configuration
     */
    public DataSourcesConfiguration() {
    }

    /**
     * initialize datasource configuration from the given json document
     *
     * @param node Json document
     *
     * The document has the following format:      <pre>
     * {
     *    "datasourceName" : {
     *         "type" : datasourceType,
     *         <Type specific configuration>
     *     }
     *    ...
     * }
     * </pre>
     *
     * Each datasource is defined by a field in a Json object. For each
     * datasource definition, the 'type' field gives the class name of a a
     * JsonInitializable object. When Json object is processed, an instance of
     * 'type' is instantiated for each datasource, and processing of the actual
     * datasource configuration is delegated to the implementation,
     */
    public DataSourcesConfiguration(JsonNode node) {
        this.initializeFromJson(node);
    }

    /**
     * Returns a datasource configuration by name
     */
    public DataSourceConfiguration getDataSourceConfiguration(String name) {
        return datasources.get(name);
    }

    /**
     * Adds a new datasource
     */
    public void add(String name, DataSourceConfiguration datasource) {
        datasources.put(name, datasource);
    }

    /**
     * Returns all datasources that extend the given class
     */
    public Map<String, DataSourceConfiguration> getDataSourcesByType(Class<?> clazz) {
        Map<String, DataSourceConfiguration> map = new HashMap<>();
        for (Map.Entry<String, DataSourceConfiguration> entry : datasources.entrySet()) {
            Class<? extends DataSourceConfiguration> aClass = entry.getValue().getClass();
            if (aClass.isAssignableFrom(clazz)) { // it was inverted
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }

    /**
     * Returns all datasources
     */
    public Map<String, DataSourceConfiguration> getDataSources() {
        Map<String, DataSourceConfiguration> map = new HashMap<>();
        map.putAll(datasources);
        return map;
    }

    @Override
    public final void initializeFromJson(JsonNode node) {
        // Node must be an object node
        if (node instanceof ObjectNode) {
            for (Iterator<Map.Entry<String, JsonNode>> fieldItr = node.fields(); fieldItr.hasNext();) {
                Map.Entry<String, JsonNode> field = fieldItr.next();
                String name = field.getKey();
                JsonNode dsNode = field.getValue();
                LOGGER.debug("Parsing {}", name);
                JsonNode typeNode = dsNode.get("type");
                if (typeNode == null) {
                    throw new IllegalArgumentException("type expected in " + name);
                }
                String type = typeNode.asText();
                LOGGER.debug("{} is a {}", name, type);
                try {
                    Class clazz = Class.forName(type);
                    DataSourceConfiguration ds = (DataSourceConfiguration) clazz.newInstance();
                    ds.initializeFromJson(dsNode);
                    datasources.put(name, ds);
                } catch (Exception e) {
                    throw new IllegalArgumentException(dsNode + ":" + e);
                }
            }
        } else {
            throw new IllegalArgumentException("node must be instanceof ObjectNode: " + node.toString());
        }
    }
}
