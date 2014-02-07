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
import java.util.ArrayList;
import java.util.List;

import com.redhat.lightblue.util.Path;

/**
 * Specifies that the combined value of one or more fields must be unique
 */
public class Index implements Serializable {

    private static final long serialVersionUID = 1l;

    private String name;
    private boolean unique = false;
    private final ArrayList<Path> fields = new ArrayList<>();

    /**
     * Default ctor
     */
    public Index() {
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the unique
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * @param unique the unique to set
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * The fields such that the ordered combination of their values must be unique
     */
    public List<Path> getFields() {
        return (ArrayList<Path>) fields.clone();
    }

    /**
     * The fields such that the ordered combination of their values must be unique
     */
    public void setFields(List<Path> f) {
        fields.clear();
        if (f != null) {
            fields.addAll(f);
        }
    }
}
