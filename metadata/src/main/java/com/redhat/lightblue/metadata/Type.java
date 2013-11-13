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

public interface Type {

    public static final String ERR_INCOMPATIBLE_VALUE="INCOMPATIBLE_VALUE";

    /**
     * Returns the type name
     */
    public String getName();

    /**
     * Returns if values of this type can be compared for equality
     */
    public boolean supportsEq();

    /**
     * Returns if values of this type can be ordered (i.e. <, <=, >, >=)
     */
    public boolean supportsOrdering();

    /**
     * Convert the non-null value to Json node
     */
    public JsonNode toJson(JsonNodeFactory factory,Object value);

    /**
     * Convert the non-null json node value to Java native value
     */
    public Object fromJson(JsonNode value);

    public boolean equals(Object obj);

    public int hashCode();

    public String toString();
}

