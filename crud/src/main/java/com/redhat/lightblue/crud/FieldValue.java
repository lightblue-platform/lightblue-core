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
package com.redhat.lightblue.crud;

import com.redhat.lightblue.query.Value;
import java.io.Serializable;

import com.redhat.lightblue.util.Path;

/**
 * A field and value pair, used in update expressions
 */
public class FieldValue implements Serializable {

    private static final long serialVersionUID = 1l;

    private Path field;
    private Value value;

    /**
     * Default ctor
     */
    public FieldValue() {
    }

    /**
     * Constructs a field and value pair with the given values
     */
    public FieldValue(Path field, Value value) {
        this.field = field;
        this.value = value;
    }

    /**
     * The field
     */
    public Path getField() {
        return field;
    }

    /**
     * The field
     */
    public void setPath(Path p) {
        this.field = p;
    }

    /**
     * The value
     */
    public Value getValue() {
        return value;
    }

    /**
     * The value
     */
    public void setValue(Value v) {
        value = v;
    }

    @Override
    public String toString() {
        return field+":"+value;
    }

}
