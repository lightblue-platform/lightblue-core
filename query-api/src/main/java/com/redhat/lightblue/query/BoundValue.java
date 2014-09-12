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

/**
 * A modifiable primitive value. When one of the fields in a
 * FieldComparisonExpression is bound to a value, that expression is
 * rewritten as a ValueComparisonExpression with a BoundValue, so the
 * caller can change the underlying value for every execution.
 */
public class BoundValue extends Value {

    /**
     * Creates a BoundValue with value=null
     */
    public BoundValue() {
        this(null);
    }

    /**
     * Creates a BoundValue with value=o
     */
    public BoundValue(Object o) {
        super(o);
    }

    /**
     * Sets the value
     */
    public void setValue(Object o) {
        value=o;
    }
}
