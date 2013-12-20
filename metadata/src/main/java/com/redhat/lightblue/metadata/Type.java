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

import com.fasterxml.jackson.databind.JsonNode;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Interface defining a data type 
 */
public interface Type {

    public static final String ERR_INCOMPATIBLE_VALUE="INCOMPATIBLE_VALUE";

    /**
     * Returns the type name
     */
    String getName();

    /**
     * Returns if values of this type can be compared for equality
     */
    boolean supportsEq();

    /**
     * Returns if values of this type can be ordered (i.e. <, <=, >, >=)
     */
    boolean supportsOrdering();

    /**
     * Convert the non-null value to Json node
     */
    JsonNode toJson(JsonNodeFactory factory,Object value);

    /**
     * Convert the non-null json node value to Java native value
     */
    Object fromJson(JsonNode value);

    /**
     * Compares v1 and v2, and returns <0 if v1<v2, 0 if v1=v2 and >0 if v1>v2
     */
    int compare(Object v1,Object v2);

    /**
     * Try to cast java object v to this type. 
     */
    Object cast(Object v);

    /**
     * Determine if two Types are the same
     */
    boolean equals(Object obj);

    /**
     * Returns the hashcode for the type
     */
    int hashCode();

    /**
     * Returns the name of the type
     */
    String toString();
}

