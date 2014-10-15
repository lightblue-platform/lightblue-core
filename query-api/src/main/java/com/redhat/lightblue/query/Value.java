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

import java.math.BigInteger;
import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;
import java.util.Objects;

/**
 * Wrapper for primitive values in queries. Provides the basics to convert a
 * primitive value to/from json. during query evaluation, metadata is used to
 * interpret the actual value.
 */
public class Value extends JsonObject {

    private static final long serialVersionUID = 1L;

    protected Object value;

    /**
     * Creates a Value with value=o
     */
    public Value(Object o) {
        this.value = o;
    }

    /**
     * Returns the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Creates the appropriate json node based on the type of the value
     */
    @Override
    public JsonNode toJson() {
        if (value instanceof Number) {
            if (value instanceof BigDecimal) {
                return getFactory().numberNode((BigDecimal) value);
            } else if (value instanceof BigInteger) {
                return getFactory().numberNode((BigInteger) value);
            } else if (value instanceof Double) {
                return getFactory().numberNode((Double) value);
            } else if (value instanceof Float) {
                return getFactory().numberNode((Float) value);
            } else if (value instanceof Long) {
                return getFactory().numberNode((Long) value);
            } else {
                return getFactory().numberNode(((Number) value).intValue());
            }
        } else if (value instanceof Boolean) {
            return getFactory().booleanNode((Boolean) value);
        } else if(value == null ) {
            return getFactory().nullNode();
        } else {
            return getFactory().textNode(value.toString());
        }
    }

    /**
     * Creates a value from a json node
     *
     * If the node is decimal, double, or float, create s a BigDecimal value. If
     * the node is BigInteger, creates a BigIngeter value. If the node is a long
     * or int, creates a long or int value. If the node is a boolean, creates a
     * boolean value. Otherwise, creates a string value.
     */
    public static Value fromJson(JsonNode node) {
        if (node.isValueNode()) {
            Object v = null;
            if (node.isNumber()) {
                if (node.isBigDecimal() || node.isDouble() || node.isFloat()) {
                    v = node.decimalValue();
                } else if (node.isBigInteger()) {
                    v = node.bigIntegerValue();
                } else if (node.isLong()) {
                    v = node.longValue();
                } else {
                    v = node.intValue();
                }
            } else if (node.isBoolean()) {
                v = node.booleanValue();
            } else {
                v = node.textValue();
            }
            return new Value(v);
        } else {
            throw Error.get(QueryConstants.ERR_INVALID_VALUE, node.toString());
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.value);
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
        final Value other = (Value) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}
