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
package com.redhat.lightblue.metadata.constraints;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.metadata.EntityConstraint;

/**
 * Specifies that the combined value of one or more fields must be unique
 */
public class UniqueConstraint implements EntityConstraint, Serializable {

    private static final long serialVersionUID = 1l;

    public static final String UNIQUE = "unique";

    private final ArrayList<Path> fields = new ArrayList<Path>();

    /**
     * Default ctor
     */
    public UniqueConstraint() {}

    /**
     * Constructs a unique constraint using the given fields.
     */
    public UniqueConstraint(List<Path> fields) {
        this.fields.addAll(fields);
    }

    /**
     * Constructs a unique constraint using the given fields.
     */
    public UniqueConstraint(Path... fields) {
        this(Arrays.asList(fields));
    }

    /**
     * Returns UNIQUE
     */
    public String getType() {
        return UNIQUE;
    }

    /**
     * The fields such that the ordered combination of their values must be unique
     */
    @SuppressWarnings("unchecked")
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
