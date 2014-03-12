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

import com.google.gson.internal.LinkedTreeMap;

/**
 * JSON based configuration file.
 *
 * @author nmalik
 */
public class CrudConfiguration {
    /**
     * The file on classpath that this configuration is loaded from.
     */
    public static final transient String FILENAME = "lightblue-crud.json";

    /**
     * @return the controllers
     */
    public Controller[] getControllers() {
        return copyControllerArray(controllers);
    }

    /**
     * @param controllers the controllers to set
     */
    public void setControllers(Controller[] controllers) {
        this.controllers = copyControllerArray(controllers);
    }

    private Controller[] copyControllerArray(Controller[] toCopy) {
        Controller[] theCopy = new Controller[toCopy.length];
        for (int i = 0; i < toCopy.length; i++) {
            Controller copy = new Controller();
            copy.setClassName(toCopy[i].getClassName());
            copy.setDatastoreType(toCopy[i].getDatastoreType());
            copy.setFactoryMethod(toCopy[i].getFactoryMethod());
            theCopy[i] = copy;
        }
        return theCopy;
    }

    /**
     * @return the databaseConfigurationClass
     */
    public String getDatabaseConfigurationClass() {
        return databaseConfigurationClass;
    }

    /**
     * @param databaseConfigurationClass the databaseConfigurationClass to set
     */
    public void setDatabaseConfigurationClass(String databaseConfigurationClass) {
        this.databaseConfigurationClass = databaseConfigurationClass;
    }

    /**
     * @return the databaseConfiguration
     */
    public Object getDatabaseConfiguration() {
        return databaseConfiguration;
    }

    /**
     * @param databaseConfiguration the databaseConfiguration to set
     */
    public void setDatabaseConfiguration(Object databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    /**
     * @return the properties
     */
    public LinkedTreeMap<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(LinkedTreeMap<String, Object> properties) {
        this.properties = properties;
    }

    public static class Controller {
        private String datastoreType;
        private String className;
        private String factoryMethod;

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
         * @return the className
         */
        public String getClassName() {
            return className;
        }

        /**
         * @param className the className to set
         */
        public void setClassName(String className) {
            this.className = className;
        }

        /**
         * @return the factoryMethod
         */
        public String getFactoryMethod() {
            return factoryMethod;
        }

        /**
         * @param factoryMethod the factoryMethod to set
         */
        public void setFactoryMethod(String factoryMethod) {
            this.factoryMethod = factoryMethod;
        }
    }

    private Controller controllers[];
    private String databaseConfigurationClass;
    private Object databaseConfiguration;
    private LinkedTreeMap<String, Object> properties;

    /**
     * Validate that the configuration has all data needed.
     */
    public boolean isValid() {
        return (getDatabaseConfiguration() != null);
    }

}
