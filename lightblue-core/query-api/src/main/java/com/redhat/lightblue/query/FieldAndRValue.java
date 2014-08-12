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
package com.redhat.lightblue.query;

import java.io.Serializable;

import com.redhat.lightblue.util.Path;
import java.util.Objects;

/**
 * Represents a field and rvalue
 */
public class FieldAndRValue implements Serializable {

    private static final long serialVersionUID = 1l;

    private Path field;
    private RValueExpression rvalue;

    /**
     * Default ctor
     */
    public FieldAndRValue() {
    }

    /**
     * Constructs a field-value pair using the given values
     */
    public FieldAndRValue(Path field, RValueExpression rvalue) {
        this.field = field;
        this.rvalue = rvalue;
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
    public void setField(Path p) {
        field = p;
    }

    /**
     * The rvalue
     */
    public RValueExpression getRValue() {
        return rvalue;
    }

    /**
     * The rvalue
     */
    public void setRValue(RValueExpression e) {
        rvalue = e;
    }

    @Override
    public String toString() {
        return field + ":" + rvalue;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.field);
        hash = 67 * hash + Objects.hashCode(this.rvalue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FieldAndRValue other = (FieldAndRValue) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        if (!Objects.equals(this.rvalue, other.rvalue)) {
            return false;
        }
        return true;
    }

}
