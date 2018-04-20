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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.BulkRequest;
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
import com.redhat.lightblue.crud.valuegenerators.GeneratedFieldInterceptor;
import com.redhat.lightblue.extensions.ExtensionSupport;
import com.redhat.lightblue.extensions.synch.Locking;
import com.redhat.lightblue.extensions.synch.LockingSupport;
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

    private transient volatile MetadataConfiguration mdConfiguration;
    private transient volatile CrudConfiguration crudConfiguration=null;

    private volatile Metadata metadata = null;
    private transient volatile JSONMetadataParser parser = null;
    private transient volatile Mediator mediator = null;
    private transient volatile Factory factory;
    private transient volatile JsonTranslator jsonTranslator = null;
    private transient volatile Map<String, LockingSupport> lockingMap = null;

    public LightblueFactory(DataSourcesConfiguration datasources) {
        this(datasources, (JsonNode) null, null);
    }

    public LightblueFactory(DataSourcesConfiguration datasources, JsonNode crudNode, JsonNode metadataNode) {
        if (datasources == null) {
            throw new IllegalArgumentException("datasources cannot be null");
        }
        this.datasources = datasources;
        this.crudNode = crudNode;
        this.metadataNode = metadataNode;
    }

    public LightblueFactory(DataSourcesConfiguration datasources, MetadataConfiguration mdConfiguration, CrudConfiguration crudConfiguration) {
        this.crudNode = null;
        this.metadataNode = null;

        this.datasources = Objects.requireNonNull(datasources, "datasources");
        this.mdConfiguration = Objects.requireNonNull(mdConfiguration, "mdConfiguration");
        this.crudConfiguration = Objects.requireNonNull(crudConfiguration, "crudConfiguration");
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
                injectDependencies(backendParser);
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

    private synchronized void initializeCrudConfiguration()
        throws IOException {
        if(crudConfiguration==null) {
            JsonNode root = crudNode;
            if (root == null) {
                try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CrudConfiguration.FILENAME)) {
                    if (null == is) {
                        throw new FileNotFoundException(CrudConfiguration.FILENAME);
                    }
                    root = JsonUtils.json(is, true);
                }
            } else {
                LOGGER.debug("Using passed in node to initialize crud configuration");
            }
            LOGGER.debug("crud configuration: {}", root);
            // convert root to Configuration object
            crudConfiguration = new CrudConfiguration();
            crudConfiguration.initializeFromJson(root);
            // validate
            if (!crudConfiguration.isValid()) {
                throw new IllegalStateException(CrudConstants.ERR_CONFIG_NOT_VALID + " - " + CrudConfiguration.FILENAME);
            }
        }
    }

    private synchronized void initializeFactory()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (factory == null) {
            LOGGER.debug("Initializing factory");
            if(crudConfiguration==null)
                initializeCrudConfiguration();

            // Set validation flag for all crud requests
            getJsonTranslator().setValidation(Request.class, crudConfiguration.isValidateRequests());

            Factory f = new Factory();
            f.setBulkParallelExecutions(crudConfiguration.getBulkParallelExecutions());
            f.setMemoryIndexThreshold(crudConfiguration.getMemoryIndexThreshold());
            f.addFieldConstraintValidators(new DefaultFieldConstraintValidators());
            f.setMaxResultSetSizeForReadsB(crudConfiguration.getMaxResultSetSizeForReadsB());
            f.setWarnResultSetSizeB(crudConfiguration.getWarnResultSetSizeB());
            f.setMaxResultSetSizeForWritesB(crudConfiguration.getMaxResultSetSizeForWritesB());

            // Add default interceptors
            new UIDInterceptor().register(f.getInterceptors());
            new GeneratedFieldInterceptor().register(f.getInterceptors());

            for (ControllerConfiguration x : crudConfiguration.getControllers()) {
                ControllerFactory cfactory = x.getControllerFactoryInitializer().newInstance();
                CRUDController controller = cfactory.createController(x, datasources);
                injectDependencies(controller);
                f.addCRUDController(x.getBackend(), controller);
            }
            // Make sure we assign factory after it is initialized. (factory is volatile, there's a memory barrier here)
            factory = f;

            LOGGER.info("Initialized factory: {}", factory);
        }
    }

    private synchronized void initializeMetadata(Factory factory) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (metadata == null) {
            LOGGER.debug("Initializing metadata");

            MetadataConfiguration cfg = getMetadataConfiguration();

            // Set validation flag for all metadata requests
            getJsonTranslator().setValidation(EntityMetadata.class, cfg.isValidateRequests());
            getJsonTranslator().setValidation(EntitySchema.class, cfg.isValidateRequests());
            getJsonTranslator().setValidation(EntityInfo.class, cfg.isValidateRequests());

            metadata = cfg.createMetadata(datasources, getJSONParser(), this);

            factory.setHookResolver(new SimpleHookResolver(cfg.getHookConfigurationParsers(), this));
        }
    }

    private MetadataConfiguration getMetadataConfiguration() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (mdConfiguration != null) {
            return mdConfiguration;
        }

        JsonNode root = metadataNode;
        if (root == null) {
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(MetadataConfiguration.FILENAME)) {
                if (null == is) {
                    throw new FileNotFoundException(MetadataConfiguration.FILENAME);
                }
                root = JsonUtils.json(is, true);
            }
        }
        LOGGER.debug("Config root:{}", root);

        JsonNode cfgClass = root.get("type");
        if (cfgClass == null) {
            throw new IllegalStateException(MetadataConstants.ERR_CONFIG_NOT_FOUND + " - type");
        }

        MetadataConfiguration cfg = (MetadataConfiguration) Thread.currentThread().getContextClassLoader().loadClass(
                cfgClass.asText()).newInstance();
        injectDependencies(cfg);

        cfg.initializeFromJson(root);

        return mdConfiguration = cfg;
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
                tx.registerTranslation(BulkRequest.class,
                        new JsonTranslator.StaticFactoryMethod(BulkRequest.class, "fromJson", ObjectNode.class),
                        "json-schema/bulkRequest.json");
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            jsonTranslator = tx;
        }
    }

    private synchronized void initializeLockingMap()
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (lockingMap == null) {
            LOGGER.debug("Initializing locking");
            Map<String, LockingSupport> map = new HashMap<>();
            CRUDController[] controllers = getFactory().getCRUDControllers();
            LOGGER.debug("Got {} controllers", controllers.length);
            for (CRUDController controller : controllers) {
                LOGGER.debug("Inspecting {}", controller.getClass());
                if (controller instanceof ExtensionSupport) {
                    LOGGER.debug("{} supports extensions", controller.getClass());
                    LockingSupport lockingSupport = (LockingSupport) ((ExtensionSupport) controller).getExtensionInstance(LockingSupport.class);
                    if (lockingSupport != null) {
                        LOGGER.debug("{} supports locking", controller.getClass());
                        for (String domain : lockingSupport.getLockingDomains()) {
                            map.put(domain, lockingSupport);
                        }
                    }
                }
            }
            LOGGER.debug("Locking map:{}", map);
            lockingMap = map;
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

    public CrudConfiguration getCrudConfiguration() throws IOException {
        if(crudConfiguration==null)
            initializeCrudConfiguration();
        return crudConfiguration;
    }
    
    public Locking getLocking(String domain)
            throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (lockingMap == null) {
            initializeLockingMap();
        }
        LockingSupport ls = lockingMap.get(domain);
        if (ls != null) {
            return ls.getLockingInstance(domain);
        } else {
            throw new RuntimeException("Unrecognized locking domain");
        }
    }

    public JsonTranslator getJsonTranslator() {
        if (jsonTranslator == null) {
            initializeJsonTranslator();
        }
        return jsonTranslator;
    }

    void injectDependencies(Object o) {

        if (o instanceof LightblueFactoryAware) {
            ((LightblueFactoryAware) o).setLightblueFactory(this);
        }

    }

}
