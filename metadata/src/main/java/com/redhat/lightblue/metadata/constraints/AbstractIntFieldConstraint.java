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

import com.redhat.lightblue.metadata.FieldConstraint;

import java.io.Serializable;

/**
 * Basic field constaint of the form
 *
 * <pre>
 *  { "constraint" : <value> }
 * </pre>
 */
public abstract class AbstractIntFieldConstraint
        implements FieldConstraint, Serializable {

    private static final long serialVersionUID = 1l;

    private final String type;
    private int value;

    /**
     * Constructs a constraint with the given type
     */
    public AbstractIntFieldConstraint(String type) {
        this.type = type;
    }

    /**
     * Constructs a constraint with the given type and value
     */
    public AbstractIntFieldConstraint(String type, int value) {
        this(type);
        this.value = value;
    }

    @Override
    public String getType() {
        return type;
    }

    /**
     * Constraint value
     */
    public int getValue() {
        return value;
    }

    /**
     * Constraint value
     */
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String getDescription() {
        return Integer.toString(value);
    }

}
