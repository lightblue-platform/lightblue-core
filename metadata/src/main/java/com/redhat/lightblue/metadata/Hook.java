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

import com.redhat.lightblue.query.Projection;

public class Hook extends MetadataObject {

    private final String name;
    private Projection projection;
    private HookConfiguration configuration;
    private boolean insert;
    private boolean update;
    private boolean delete;
    private boolean find;

    /**
     * Constructs a hook with the given name
     */
    public Hook(String name) {
        this.name = name;
    }

    /**
     * Hook name
     */
    public String getName() {
        return name;
    }

    /**
     * Optional projection applied to data sent to the hook. If null, data is
     * sent without projecting
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * Optional projection applied to data sent to the hook. If null, data is
     * sent without projecting
     */
    public void setProjection(Projection projection) {
        this.projection = projection;
    }

    /**
     * Hook specific configuration
     */
    public HookConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Hook specific configuration
     */
    public void setConfiguration(HookConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Determines if this is an insertion hook
     */
    public boolean isInsert() {
        return insert;
    }

    /**
     * Determines if this is an insertion hook
     */
    public void setInsert(boolean b) {
        insert = b;
    }

    /**
     * Determines if this is an update hook
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * Determines if this is an update hook
     */
    public void setUpdate(boolean b) {
        update = b;
    }

    /**
     * Determines if this is a deletion hook
     */
    public boolean isDelete() {
        return delete;
    }

    /**
     * Determines if this is a deletion hook
     */
    public void setDelete(boolean b) {
        delete = b;
    }

    /**
     * Determines if this is a find hook
     */
    public boolean isFind() {
        return find;
    }

    /**
     * Determines if this is a find hook
     */
    public void setFind(boolean b) {
        find = b;
    }
}
