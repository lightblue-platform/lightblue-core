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
package com.redhat.lightblue.metadata.types;

import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.metadata.MetadataConstants;
import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.util.Error;

public final class BigIntegerType implements Type, Serializable {

    private static final long serialVersionUID = 1l;

    public static final Type TYPE = new BigIntegerType();
    public static final String NAME = "biginteger";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean supportsEq() {
        return true;
    }

    @Override
    public boolean supportsOrdering() {
        return false;
    }

    @Override
    public JsonNode toJson(JsonNodeFactory factory, Object obj) {
        return factory.numberNode((BigInteger) cast(obj));
    }

    @Override
    public Object fromJson(JsonNode node) {
        if (node.isValueNode()) {
            return node.bigIntegerValue();
        } else {
            throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, node.toString());
        }
    }

    @Override
    public Object cast(Object obj) {
        BigInteger value = null;
        if (obj != null) {
            if (obj instanceof BigInteger) {
                value = (BigInteger) obj;
            } else if (obj instanceof Number) {
                value = new BigInteger(Long.toString(((Number) obj).longValue()));
            } else if (obj instanceof Boolean) {
                value = new BigInteger(((Boolean) obj) ? "1" : "0");
            } else if (obj instanceof String) {
                try {
                    value = new BigInteger((String) obj);
                } catch (NumberFormatException e) {
                    throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, obj.toString());
                }
            } else {
                throw Error.get(NAME, MetadataConstants.ERR_INCOMPATIBLE_VALUE, obj.toString());
            }
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public int compare(Object v1, Object v2) {
        if (v1 == null) {
            if (v2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (v2 == null) {
            return 1;
        } else {
            return ((Comparable) cast(v1)).compareTo(cast(v2));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BigIntegerType;
    }

    @Override
    public int hashCode() {
        return 5;
    }

    @Override
    public String toString() {
        return NAME;
    }

    private BigIntegerType() {
    }
}
