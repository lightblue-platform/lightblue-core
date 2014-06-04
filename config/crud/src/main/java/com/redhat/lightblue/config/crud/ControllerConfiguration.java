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

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.JsonInitializable;

public class ControllerConfiguration implements JsonInitializable {

    private String datastoreType;
    private Class<? extends ControllerFactory> controllerFactory;

    public ControllerConfiguration() {
    }

    public ControllerConfiguration(ControllerConfiguration c) {
        datastoreType = c.datastoreType;
        controllerFactory = c.controllerFactory;
    }

    /**
     * @return the datastoreType
     */
    public String getDatastoreType() {
        return datastoreType;
    }

    /**
     * @param datastoreType the datastoreType to set
     */
    public void setDatastoreType(String datastoreType) {
        this.datastoreType = datastoreType;
    }

    /**
     * @return the controller factory class
     */
    public Class<? extends ControllerFactory> getControllerFactory() {
        return controllerFactory;
    }

    /**
     * @param clazz the class to set
     */
    public void setControllerFactory(Class<? extends ControllerFactory> clazz) {
        this.controllerFactory = clazz;
    }

    @Override
    public void initializeFromJson(JsonNode node) {
        try {
            if(node!=null) {
                JsonNode x=node.get("datastoreType");
                if(x!=null) {
                    datastoreType=x.asText();
                }
                x=node.get("controllerFactory");
                if(x!=null) {
                    controllerFactory=(Class<ControllerFactory>)Class.forName(x.asText());
                }
            }
        } catch (Exception e ){
            throw new RuntimeException(e);
        }
    }
}
