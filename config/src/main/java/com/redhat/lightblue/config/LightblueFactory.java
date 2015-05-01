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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DeleteRequest;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.InsertionRequest;
import com.redhat.lightblue.crud.SaveRequest;
import com.redhat.lightblue.crud.UpdateRequest;
import com.redhat.lightblue.crud.interceptors.UIDInterceptor;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.hooks.CRUDHook;
import com.redhat.lightblue.hooks.HookPostParseListener;
import com.redhat.lightblue.hooks.SimpleHookResolver;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.parser.DataStoreParser;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Manager class that creates instances of Mediator, Factory, Metadata, etc.
 * based on configuration.
 */
public final class LightblueFactory implements Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(LightblueFactory.class);

    private final DataSourcesConfiguration datasources;
    private final JsonNode crudNode;
    private final JsonNode metadataNode;

    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    private volatile Metadata metadata = null;
    private transient volatile JSONMetadataParser parser = null;
    private transient volatile Mediator mediator = null;
    private volatile Factory factory;
    private transient volatile JsonTranslator jsonTranslator = null;

    public LightblueFactory(DataSourcesConfiguration datasources) {
        this(datasources, null, null);
    }

    public LightblueFactory(DataSourcesConfiguration datasources, JsonNode crudNode, JsonNode metadataNode) {
        this.datasources = datasources;
        this.crudNode = crudNode;
        this.metadataNode = metadataNode;
    }

    private LightblueFactory getThis() {
        return this;
    }

    private synchronized void initializeParser()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException {
        if (parser == null) {
            Extensions<JsonNode> extensions = new Extensions<>();
            extensions.addDefaultExtensions();

            Map<String, DataSourceConfiguration> ds = datasources.getDataSources();
            for (Map.Entry<String, DataSourceConfiguration> entry : ds.entrySet()) {
                Class<? extends DataStoreParser> tempParser = entry.getValue().getMetadataDataStoreParser();
                DataStoreParser backendParser = tempParser.newInstance();
                if (backendParser instanceof LightblueFactoryAware) {
                    ((LightblueFactoryAware) backendParser).setLightblueFactory(this);
                }
                extensions.registerDataStoreParser(backendParser.getDefaultName(), backendParser);
            }

            parser = new JSONMetadataParser(extensions, new DefaultTypes(), NODE_FACTORY);
        }
    }

    private synchronized void initializeMediator()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (mediator == null) {
            mediator = new Mediator(getMetadata(), getFactory());
        }
    }

    private synchronized void initializeFactory()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (factory == null) {
            JsonNode root = crudNode;
            if (root == null) {
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CrudConfiguration.FILENAME)) {
                    root = JsonUtils.json(is);
                }
            }

            // convert root to Configuration object
            CrudConfiguration configuration = new CrudConfiguration();
            configuration.initializeFromJson(root);

            // Set validation flag for all crud requests
            getJsonTranslator().setValidation(Request.class, configuration.isValidateRequests());

            Factory f = new Factory();
            f.addFieldConstraintValidators(new DefaultFieldConstraintValidators());

            // Add default interceptors
            new UIDInterceptor().register(f.getInterceptors());

            // validate
            if (!configuration.isValid()) {
                throw new IllegalStateException(CrudConstants.ERR_CONFIG_NOT_VALID + " - " + CrudConfiguration.FILENAME);
            }

            for (ControllerConfiguration x : configuration.getControllers()) {
                ControllerFactory cfactory = x.getControllerFactory().newInstance();
                CRUDController controller = cfactory.createController(x, datasources);
                if (controller instanceof LightblueFactoryAware) {
                    ((LightblueFactoryAware) controller).setLightblueFactory(this);
                }
                f.addCRUDController(x.getBackend(), controller);
            }
            // Make sure we assign factory after it is initialized. (factory is volatile, there's a memory barrier here)
            factory = f;
        }
    }

    private synchronized void initializeMetadata(Factory factory) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (metadata == null) {
            LOGGER.debug("Initializing metadata");

            JsonNode root = metadataNode;
            if (root == null) {
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MetadataConfiguration.FILENAME)) {
                    root = JsonUtils.json(is);
                }
            }
            LOGGER.debug("Config root:{}", root);

            JsonNode cfgClass = root.get("type");
            if (cfgClass == null) {
                throw new IllegalStateException(MetadataConstants.ERR_CONFIG_NOT_FOUND + " - type");
            }

            MetadataConfiguration cfg = (MetadataConfiguration) Class.forName(cfgClass.asText()).newInstance();
            if (cfg instanceof LightblueFactoryAware) {
                ((LightblueFactoryAware) cfg).setLightblueFactory(this);
            }
            cfg.initializeFromJson(root);

            // Set validation flag for all metadata requests
            getJsonTranslator().setValidation(EntityMetadata.class, cfg.isValidateRequests());
            getJsonTranslator().setValidation(EntitySchema.class, cfg.isValidateRequests());
            getJsonTranslator().setValidation(EntityInfo.class, cfg.isValidateRequests());

            metadata = cfg.createMetadata(datasources, getJSONParser(), this);

            factory.setHookResolver(new SimpleHookResolver(
                    cfg.getHookConfigurationParsers(),
                    new ArrayList<HookPostParseListener>(Arrays.asList(
                            new HookPostParseListener() {

                                @Override
                                public void fire(CRUDHook hook) {
                                    ((LightblueFactoryAware) hook).setLightblueFactory(getThis());
                                }

                            }))));
        }
    }

    private synchronized void initializeJsonTranslator() {
        if (jsonTranslator == null) {
            LOGGER.debug("Initializing JsonTranslator");

            JsonTranslator tx = new JsonTranslator();

            tx.registerTranslation(EntityMetadata.class, new JsonTranslator.FromJson() {
                @Override
                public Object fromJson(JsonNode node) {
                    try {
                        return getJSONParser().parseEntityMetadata(node);
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "json-schema/metadata/metadata.json");
            tx.registerTranslation(EntityInfo.class, new JsonTranslator.FromJson() {
                @Override
                public Object fromJson(JsonNode node) {
                    try {
                        return getJSONParser().parseEntityInfo(node);
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "json-schema/metadata/entityInfo.json");
            tx.registerTranslation(EntitySchema.class, new JsonTranslator.FromJson() {
                @Override
                public Object fromJson(JsonNode node) {
                    try {
                        return getJSONParser().parseEntitySchema(node);
                    } catch (RuntimeException re) {
                        throw re;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }, "json-schema/metadata/schema.json");

            try {
                tx.registerTranslation(FindRequest.class,
                        new JsonTranslator.StaticFactoryMethod(FindRequest.class, "fromJson", ObjectNode.class),
                        "json-schema/findRequest.json");
                tx.registerTranslation(InsertionRequest.class,
                        new JsonTranslator.StaticFactoryMethod(InsertionRequest.class, "fromJson", ObjectNode.class),
                        "json-schema/insertRequest.json");
                tx.registerTranslation(DeleteRequest.class,
                        new JsonTranslator.StaticFactoryMethod(DeleteRequest.class, "fromJson", ObjectNode.class),
                        "json-schema/deleteRequest.json");
                tx.registerTranslation(SaveRequest.class,
                        new JsonTranslator.StaticFactoryMethod(SaveRequest.class, "fromJson", ObjectNode.class),
                        "json-schema/saveRequest.json");
                tx.registerTranslation(UpdateRequest.class,
                        new JsonTranslator.StaticFactoryMethod(UpdateRequest.class, "fromJson", ObjectNode.class),
                        "json-schema/updateRequest.json");
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            jsonTranslator = tx;
        }
    }

    public Metadata getMetadata()
            throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (metadata == null) {
            initializeMetadata(getFactory());
        }

        return metadata;
    }

    public JSONMetadataParser getJSONParser()
            throws ClassNotFoundException, NoSuchMethodException, IOException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (parser == null) {
            initializeParser();

            // Metadata is loaded next because the hooks need to be registered on the Factory.
            getMetadata();
        }

        return parser;
    }

    public Factory getFactory()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (factory == null) {
            initializeFactory();
        }
        return factory;
    }

    public Mediator getMediator()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (mediator == null) {
            initializeMediator();
        }

        return mediator;
    }

    public JsonTranslator getJsonTranslator() {
        if (jsonTranslator == null) {
            initializeJsonTranslator();
        }
        return jsonTranslator;
    }
}
