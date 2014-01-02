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

/**
 * Wrapper for primitive values in queries. Provides the basics to
 * convert a primitive value to/from json. during query evaluation,
 * metadata is used to interpret the actual value.
 */
public class Value extends JsonObject {
    private Object value;

    public static final String INVALID_VALUE="INVALID_VALUE";

    /**
     * Default ctor with value=null
     */
    public Value() {}

    /**
     * Creates a Value with value=o
     */
    public Value(Object o) {
        this.value=o;
    }

    /**
     * Returns the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value
     */
    public void setValue(Object o) {
        this.value=o;
    }

    /**
     * Creates the appropriate json node based on the type of the value
     */
    @Override
    public JsonNode toJson() {
        if(value instanceof Number) {
            if(value instanceof BigDecimal) {
                return getFactory().numberNode((BigDecimal)value);
            } else if(value instanceof BigInteger) {
                return getFactory().numberNode((BigInteger)value);
            } else if(value instanceof Double) {
                return getFactory().numberNode((Double)value);
            } else if(value instanceof Float) {
                return getFactory().numberNode((Float)value);
            } else if(value instanceof Long) {
                return getFactory().numberNode((Long)value);
            } else  {
                return getFactory().numberNode(((Number)value).intValue());
            }
        } else if(value instanceof Boolean) {
            return getFactory().booleanNode((Boolean)value);
        } else {
            return getFactory().textNode(value.toString());
        }
    }

    /**
     * Creates a value from a json node
     *
     * If the node is decimal, double, or float, create s a BigDecimal
     * value. If the node is BigInteger, creates a BigIngeter
     * value. If the node is a long or int, creates a long or int
     * value. If the node is a boolean, creates a boolean
     * value. Otherwise, creates a string value.
     */
    public static Value fromJson(JsonNode node) {
        if(node.isValueNode()) {
            Value ret=new Value();
            if(node.isNumber()) {
                if(node.isBigDecimal()||node.isDouble()||node.isFloat()) {
                    ret.value=node.decimalValue();
                } else if(node.isBigInteger()) {
                    ret.value=node.bigIntegerValue();
                } else if(node.isLong()) {
                    ret.value=node.longValue();
                } else {
                    ret.value=node.intValue();
                }
            } else if(node.isBoolean()) {
                ret.value=node.booleanValue();
            } else {
                ret.value=node.textValue();
            }
            return ret;
        } else {
            throw Error.get(INVALID_VALUE,node.toString());
        }
    }
}
