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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.lightblue.util.JsonInitializable;

/**
 * Represents a controller configuration.It specifies the name of the backend,
 * and the class for the controller factory. The controller instances of that
 * backend are created by an instance of the controller factory.
 */
public class ControllerConfiguration implements JsonInitializable, Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerConfiguration.class);

    private String backend;
    private ControllerFactoryInitializer controllerFactoryInitializer;
    private ObjectNode extensions;
    private ObjectNode options;

    /**
     * A delegate used to lazily initialize ControllerFactory.
     *
     */
    public static interface ControllerFactoryInitializer {
        public ControllerFactory newInstance();
    }

    public ControllerConfiguration() {}

    public ControllerConfiguration(ControllerConfiguration c) {
        backend = c.backend;
        controllerFactoryInitializer = c.controllerFactoryInitializer;
        extensions = c.extensions;
        options=c.options;
    }

    /**
     * @return the backend
     */
    public String getBackend() {
        return backend;
    }

    /**
     * @param backend the backend to set
     */
    public void setBackend(String backend) {
        this.backend = backend;
    }

    /**
     * @return the controller factory initializer
     */
    public ControllerFactoryInitializer getControllerFactoryInitializer() {
        return controllerFactoryInitializer;
    }


    public void setControllerFactoryInitializer(ControllerFactoryInitializer controllerFactoryInitializer) {
        this.controllerFactoryInitializer = controllerFactoryInitializer;
    }

    /**
     * The configuration for extensions
     */
    public ObjectNode getExtensions() {
        return extensions;
    }

    /**
     * The configuration for extensions
     */
    public void setExtensions(ObjectNode node) {
        extensions = node;
    }

    /**
     * The options for the controller
     */
    public ObjectNode getOptions() {
        return options;
    }

    /**
     * The  options for the controller
     */
    public void setOptions(ObjectNode node) {
        options = node;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initializeFromJson(JsonNode node) {
        try {
            if (node != null) {
                JsonNode x = node.get("backend");
                if (x != null) {
                    backend = x.asText();
                }
                x = node.get("controllerFactory");
                if (x != null) {
                    final String clazz = x.asText();
                    controllerFactoryInitializer = new ControllerFactoryInitializer() {
                        @Override
                        public ControllerFactory newInstance() {
                            try {
                                return (ControllerFactory) Thread.currentThread().getContextClassLoader()
                                        .loadClass(clazz).newInstance();
                            } catch (Exception e) {
                                throw new RuntimeException("Could not initialize ControllerFactory of "+clazz, e);
                            }
                        }

                        public String toString() {
                            return clazz;
                        }
                    };
                }
                extensions = (ObjectNode) node.get("extensions");
                options = (ObjectNode) node.get("options");
                LOGGER.debug("Initialized: source={} backend={} controllerFactory={} extensions={} options={}", node, backend, controllerFactoryInitializer, extensions,options);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
