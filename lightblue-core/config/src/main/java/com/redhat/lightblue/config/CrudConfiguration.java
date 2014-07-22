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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.JsonInitializable;

/**
 * JSON based configuration file.
 *
 * @author nmalik
 */
public class CrudConfiguration implements JsonInitializable, Serializable {

    private static final long serialVersionUID = 1l;

    /**
     * The file on classpath that this configuration is loaded from.
     */
    public static final transient String FILENAME = "lightblue-crud.json";

    private ControllerConfiguration controllers[];

    /**
     * @return the controllers
     */
    public ControllerConfiguration[] getControllers() {
        return copyControllerArray(controllers);
    }

    /**
     * @param controllers the controllers to set
     */
    public void setControllers(ControllerConfiguration[] controllers) {
        this.controllers = copyControllerArray(controllers);
    }

    private ControllerConfiguration[] copyControllerArray(ControllerConfiguration[] source) {
        ControllerConfiguration[] theCopy = new ControllerConfiguration[source.length];
        for (int i = 0; i < source.length; i++) {
            theCopy[i] = new ControllerConfiguration(source[i]);
        }
        return theCopy;
    }

    /**
     * Validate that the configuration has all data needed.
     */
    public boolean isValid() {
        return (controllers != null && controllers.length > 0);
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        if (node != null) {
            JsonNode x = node.get("controllers");
            if (x instanceof ArrayNode) {
                List<ControllerConfiguration> list = new ArrayList<>(x.size());
                for (Iterator<JsonNode> itr = ((ArrayNode) x).elements(); itr.hasNext();) {
                    JsonNode controllerNode = itr.next();
                    ControllerConfiguration controller = new ControllerConfiguration();
                    controller.initializeFromJson(controllerNode);
                    list.add(controller);
                }
                controllers = list.toArray(new ControllerConfiguration[list.size()]);
            } else {
                throw new IllegalArgumentException("'controllers' must be instanceof ArrayNode: " + node.toString());
            }
        }
    }
}
