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
package com.redhat.lightblue.config.metadata;

import com.redhat.lightblue.util.JsonInitializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.config.common.DataSourceConfiguration;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Because rest resources are instantiated for every request this manager exists
 * to keep the number of Metadata instances created down to a reasonable level.
 * 
 * This class is expected to be a singleton.
 * 
 * @author nmalik
 */
public final class MetadataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataManager.class);

    private volatile Metadata metadata = null;
    private volatile JSONMetadataParser parser = null;
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private final DataSourcesConfiguration datasources;

    public MetadataManager(DataSourcesConfiguration datasources) {
        this.datasources = datasources;
    }

    private synchronized void initializeParser() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException {
        if (parser != null) {
            return;
        }
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();

        Map<String, DataSourceConfiguration> ds = datasources.getDataSources();
        for (Map.Entry<String, DataSourceConfiguration> entry : ds.entrySet()) {
            Class<DataStoreParser> tempParser = entry.getValue().getMetadataDataStoreParser();
            extensions.registerDataStoreParser(entry.getKey(), tempParser.newInstance());
        }

        parser = new JSONMetadataParser(extensions, new DefaultTypes(), NODE_FACTORY);
    }

    private synchronized void initializeMetadata() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (metadata != null) {
            // already initalized
            return;
        }
        LOGGER.debug("Initializing metadata");

        JsonNode root;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MetadataConfiguration.FILENAME)) {
            root = JsonUtils.json(is);
        }
        LOGGER.debug("Config root:{}", root);

        JsonNode cfgClass = root.get("type");
        if (cfgClass == null) {
            throw new IllegalStateException(MetadataConstants.ERR_CONFIG_NOT_FOUND + " - type");
        }
        
        MetadataConfiguration cfg = (MetadataConfiguration) Class.forName(cfgClass.asText()).newInstance();
        cfg.initializeFromJson(root);

        metadata = cfg.createMetadata(datasources, getJSONParser());
    }

    public Metadata getMetadata() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (metadata == null) {
            initializeMetadata();
        }

        return metadata;
    }

    public JSONMetadataParser getJSONParser() throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (parser == null) {
            initializeParser();
        }

        return parser;
    }
}
