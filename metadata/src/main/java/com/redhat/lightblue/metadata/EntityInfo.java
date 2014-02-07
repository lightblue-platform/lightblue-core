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

import java.io.Serializable;

/**
 * Non-version specific bits of metadata.
 *
 * @author nmalik
 */
public class EntityInfo implements Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    //hooks
    private Indexes indexes;
    private Enums enums;
    private DataStore dataStore;

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

    public Indexes getIndexes() {
        return indexes;
    }

    public void setIndexes(Indexes indexes) {
        this.indexes = indexes;
    }

    public Enums getEnums() {
        return this.enums;
    }

    public void setEnums(Enums enums) {
        this.enums = enums;
    }

    /**
     * Gets the value of dataStore
     *
     * @return the value of dataStore
     */
    public DataStore getDataStore() {
        return this.dataStore;
    }

    /**
     * Sets the value of dataStore
     *
     * @param argDataStore Value to assign to this.dataStore
     */
    public void setDataStore(DataStore argDataStore) {
        this.dataStore = argDataStore;
    }
}
