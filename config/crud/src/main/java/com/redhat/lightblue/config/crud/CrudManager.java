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
package com.redhat.lightblue.config.crud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.config.metadata.MetadataManager;
import com.redhat.lightblue.crud.validator.DefaultFieldConstraintValidators;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.CRUDController;
import com.redhat.lightblue.mediator.Mediator;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.parser.JSONMetadataParser;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.config.common.DataSourcesConfiguration;
import com.redhat.lightblue.util.JsonInitializable;
import com.redhat.lightblue.util.JsonUtils;

/**
 * Creates CRUD controllers based on configuration. There should be only one instance of this class for each
 * application.
 *
 * @author nmalik
 */
public final class CrudManager {
    private volatile Mediator mediator = null;
    private final DataSourcesConfiguration datasources;
    private final MetadataManager metadataMgr;
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.withExactBigDecimals(true);

    public CrudManager(DataSourcesConfiguration datasources,
                       MetadataManager metadataMgr) {
        this.datasources = datasources;
        this.metadataMgr = metadataMgr;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private synchronized void initializeMediator() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (mediator != null) {
            // already initalized
            return;
        }

        JsonNode root;
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(CrudConfiguration.FILENAME)) {
            root = JsonUtils.json(is);
        }

        // convert root to Configuration object
        CrudConfiguration configuration = new CrudConfiguration();
        configuration.initializeFromJson(root);

        Factory factory = new Factory();
        factory.addFieldConstraintValidators(new DefaultFieldConstraintValidators());

        // validate
        if (!configuration.isValid()) {
            throw new IllegalStateException(CrudConstants.ERR_CONFIG_NOT_VALID + " - " + CrudConfiguration.FILENAME);
        }

        for (ControllerConfiguration x : configuration.getControllers()) {
            ControllerFactory cfactory = x.getControllerFactory().newInstance();
            CRUDController controller = cfactory.createController(x, datasources);
            factory.addCRUDController(x.getDatastoreType(), controller);
        }
        mediator = new Mediator(metadataMgr.getMetadata(), factory);
    }

    public Mediator getMediator() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, IOException, NoSuchMethodException, InstantiationException {
        if (mediator == null) {
            initializeMediator();
        }

        return mediator;
    }
}
