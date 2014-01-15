/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.metadata;

import com.google.gson.internal.LinkedTreeMap;

/**
 * JSON based configuration file.
 *
 * @author nmalik
 */
public class MetadataConfiguration {
    /**
     * The file on classpath that this configuration is loaded from.
     */
    public static final transient String FILENAME = "lightblue-metadata.json";

    private String metadataClass;
    private String metadataFactoryMethod;
    private String databaseConfigurationClass;
    private Object databaseConfiguration;
    private LinkedTreeMap<String, Object> properties;

    /**
     * Validate that the configuration has all data needed.
     */
    public boolean isValid() {
        return (getMetadataClass() != null && !metadataClass.isEmpty() && getDatabaseConfiguration() != null);
    }

    /**
     * @return the metadataClass
     */
    public String getMetadataClass() {
        return metadataClass;
    }

    /**
     * @param metadataClass the metadataClass to set
     */
    public void setMetadataClass(String metadataClass) {
        this.metadataClass = metadataClass;
    }

    /**
     * @return the metadataFactoryMethod
     */
    public String getMetadataFactoryMethod() {
        return metadataFactoryMethod;
    }

    /**
     * @param metadataFactoryMethod the metadataFactoryMethod to set
     */
    public void setMetadataFactoryMethod(String metadataFactoryMethod) {
        this.metadataFactoryMethod = metadataFactoryMethod;
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
}
