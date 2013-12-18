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
 * Unary logical operators (not)
 */
public enum UnaryLogicalOperator { 
    _not;
    
    /**
     * Applies the operator to a value
     *
     * @param value The value to which the operator is to be applied
     *
     * @return Returns op(value). For _not, returns !value.
     */
    public boolean apply(boolean value) {
        return !value;
    }

    public String toString() {
        return "$not";
    }

    public static UnaryLogicalOperator fromString(String s) {
        return "$not".equals(s)?_not:null;
    }
}
