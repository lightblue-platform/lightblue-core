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
package com.redhat.lightblue.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.gson.Gson;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * Because rest resources are instantiated for every request this manager exists to keep the number of Metadata
 * instances created down to a reasonable level.
 *
 * @author nmalik
 */
public class MetadataManager {
    private static Metadata metadata = null;
    private static JSONMetadataParser parser = null;
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private static synchronized void initializeParser() {
        if (parser != null) {
            return;
        }
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();

        parser = new JSONMetadataParser(extensions, new DefaultTypes(), NODE_FACTORY);
    }

    private static synchronized void initializeMetadata() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (metadata != null) {
            // already initalized
            return;
        }

        StringBuilder buff = new StringBuilder();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MetadataConfiguration.FILENAME);
                InputStreamReader isr = new InputStreamReader(is, Charset.defaultCharset());
                BufferedReader reader = new BufferedReader(isr)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buff.append(line).append("\n");
            }
        }

        // get the root json node so can throw subsets of the tree at Gson later
        JsonNode root = JsonUtils.json(buff.toString());

        // convert root to Configuration object
        Gson g = new Gson();
        MetadataConfiguration configuration = g.fromJson(buff.toString(), MetadataConfiguration.class);

        if (null == configuration) {
            throw new IllegalStateException("Configuration not found: " + MetadataConfiguration.FILENAME);
        }

        // instantiate the database specific configuration object
        Class databaseConfigurationClass = Class.forName(configuration.getDatabaseConfigurationClass());
        JsonNode dbNode = root.findValue("database");
        configuration.setDatabaseConfiguration(g.fromJson(dbNode.toString(), databaseConfigurationClass));

        // validate
        if (!configuration.isValid()) {
            throw new IllegalStateException("Configuration not valid: " + MetadataConfiguration.FILENAME);
        }

        Class metadataClass = Class.forName(configuration.getMetadataClass());

        Method m = metadataClass.getDeclaredMethod(configuration.getMetadataFactoryMethod(), databaseConfigurationClass);

        metadata = (Metadata) m.invoke(null, configuration.getDatabaseConfiguration());
    }

    public static Metadata getMetadata() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (metadata == null) {
            initializeMetadata();
        }

        return metadata;
    }

    public static JSONMetadataParser getJSONParser() {
        if (parser == null) {
            initializeParser();
        }

        return parser;
    }
}
