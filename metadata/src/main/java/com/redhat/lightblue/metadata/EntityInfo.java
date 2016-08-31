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
package com.redhat.lightblue.metadata;

/**
 * Non-version specific bits of metadata.
 *
 * @author nmalik
 */
public class EntityInfo extends MetadataObject {

    private final String name;
    private String defaultVersion;
    private final Hooks hooks = new Hooks();
    private final Indexes indexes = new Indexes();
    private final Enums enums = new Enums();
    private DataStore backend;

    public EntityInfo(String name) {
        this.name = name;
    }

    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return this.name;
    }

    public String getDefaultVersion() {
        return this.defaultVersion;
    }

    public void setDefaultVersion(String version) {
        this.defaultVersion = version;
    }

    public Indexes getIndexes() {
        return indexes;
    }

    public Hooks getHooks() {
        return hooks;
    }

    public Enums getEnums() {
        return this.enums;
    }

    /**
     * Gets the value of datastore
     *
     * @return the value of datastore
     */
    public DataStore getDataStore() {
        return this.backend;
    }

    /**
     * Sets the value of datastore
     *
     * @param argDataStore Value to assign to this.datastore
     */
    public void setDataStore(DataStore argDataStore) {
        this.backend = argDataStore;
    }
}
