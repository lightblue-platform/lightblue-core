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

public class Value extends JsonObject {
    private Object value;

    public static final String INVALID_VALUE="INVALID_VALUE";

    public Value() {}

    public Value(Object o) {
        this.value=o;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object o) {
        this.value=o;
    }

    public JsonNode toJson() {
        if(value instanceof Number) {
            if(value instanceof BigDecimal)
                return factory.numberNode((BigDecimal)value);
            else if(value instanceof BigInteger)
                return factory.numberNode((BigInteger)value);
            else if(value instanceof Double)
                return factory.numberNode((Double)value);
            else if(value instanceof Float)
                return factory.numberNode((Float)value);
            else if(value instanceof Long)
                return factory.numberNode((Long)value);
            else 
                return factory.numberNode(((Number)value).intValue());
        } else if(value instanceof Boolean)
            return factory.booleanNode((Boolean)value);
        else
            return factory.textNode(value.toString());
    }

    public static Value fromJson(JsonNode node) {
        if(node.isValueNode()) {
            Value ret=new Value();
            if(node.isNumber()) {
                if(node.isBigDecimal()||node.isDouble()||node.isFloat())
                    ret.value=node.decimalValue();
                else if(node.isBigInteger())
                    ret.value=node.bigIntegerValue();
                else if(node.isLong())
                    ret.value=node.longValue();
                else
                    ret.value=node.intValue();
            } else if(node.isBoolean())
                ret.value=node.booleanValue();
            else
                ret.value=node.textValue();
            return ret;
        } else
            throw Error.get(INVALID_VALUE,node.toString());
    }
}
