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
import java.util.Map;
import java.util.HashMap;

/**
 * Non-version specific bits of metadata.
 *
 * @author nmalik
 */
public class EntityInfo implements Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private String defaultVersion;
    private final Hooks hooks = new Hooks();
    private final Indexes indexes = new Indexes();
    private final Enums enums = new Enums();
    private Backend backend;
    private final Map<String, Object> properties = new HashMap<>();

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
     * Gets the value of backend
     *
     * @return the value of backend
     */
    public Backend getBackend() {
        return this.backend;
    }

    /**
     * Sets the value of backed
     *
     * @param argBackend Value to assign to this.backend
     */
    public void setBackend(Backend argBackend) {
        this.backend = argBackend;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
