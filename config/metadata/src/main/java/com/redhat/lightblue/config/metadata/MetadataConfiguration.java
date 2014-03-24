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
package com.redhat.lightblue.config.metadata;

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
    private LinkedTreeMap<String, String> dataStoreParserNames;

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

    public LinkedTreeMap<String, String> getDataStoreParserNames() {
        return dataStoreParserNames;
    }

    public void setDataStoreParserNames(LinkedTreeMap<String, String>  dataStoreParserNames) {
        this.dataStoreParserNames = dataStoreParserNames;
    }

    public String toString() {
        StringBuilder bld=new StringBuilder();
        bld.append("metadataClass:").append(metadataClass).append('\n').
            append("metadataFactoryMethod:").append(metadataFactoryMethod).append('\n').
            append("databaseConfigurationClass:").append(databaseConfigurationClass).append('\n').
            append("databaseConfiguration:").append(databaseConfiguration).append('\n').
            append("properties:").append(properties).append('\n').
            append("dataStoreParserNames:").append(dataStoreParserNames);
        return bld.toString();
    }
}
