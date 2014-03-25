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
package com.redhat.lightblue.crud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.gson.Gson;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.crud.CrudConfiguration.Controller;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonInitializable;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Because rest resources are instantiated for every request this manager exists to keep the number of Metadata
 * instances created down to a reasonable level.
 *
 * @author nmalik
 */
public final class CrudManager {
    private static Mediator mediator = null;
    private static JSONMetadataParser parser = null;
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private CrudManager() {

    }

    private static synchronized void initializeParser() {
        if (parser != null) {
            return;
        }
        Extensions<JsonNode> extensions = new Extensions<>();
        extensions.addDefaultExtensions();

        parser = new JSONMetadataParser(extensions, new DefaultTypes(), NODE_FACTORY);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static synchronized void initializeMediator() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (mediator != null) {
            // already initalized
            return;
        }

        StringBuilder buff = new StringBuilder();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CrudConfiguration.FILENAME);
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
        // TODO swap out something other than Gson
        Gson g = new Gson();
        CrudConfiguration configuration = g.fromJson(buff.toString(), CrudConfiguration.class);

        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());

        // instantiate the database specific configuration object
        Class databaseConfigurationClass = Class.forName(configuration.getDatabaseConfigurationClass());
        JsonNode dbNode = root.findValue("databaseConfiguration");
        JsonInitializable databaseConfiguration = (JsonInitializable) databaseConfigurationClass.newInstance();
        databaseConfiguration.initializeFromJson(dbNode);
        configuration.setDatabaseConfiguration(databaseConfiguration);

        // validate
        if (!configuration.isValid()) {
            throw new IllegalStateException(CrudConstants.ERR_CONFIG_NOT_VALID + " - " + CrudConfiguration.FILENAME);
        }

        for (Controller x : configuration.getControllers()) {
            Class clazz = Class.forName(x.getClassName());

            Method m = clazz.getDeclaredMethod(x.getFactoryMethod(), databaseConfigurationClass);

            CRUDController controller = (CRUDController) m.invoke(null, configuration.getDatabaseConfiguration());
            factory.addCRUDController(x.getDatastoreType(), controller);
        }
        mediator = new Mediator(MetadataManager.getMetadata(), factory);
    }

    public static Mediator getMediator() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (mediator == null) {
            initializeMediator();
        }

        return mediator;
    }

    public static JSONMetadataParser getJSONParser() {
        if (parser == null) {
            initializeParser();
        }

        return parser;
    }
}
