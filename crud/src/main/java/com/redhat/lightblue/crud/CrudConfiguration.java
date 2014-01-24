/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        for(int i=0;i<toCopy.length;i++) {
            Controller copy = new Controller();
            copy.setClassName(new String(toCopy[i].getClassName()));
            copy.setDatastoreType(new String(toCopy[i].getDatastoreType()));
            copy.setFactoryMethod(new String(toCopy[i].getFactoryMethod()));
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
