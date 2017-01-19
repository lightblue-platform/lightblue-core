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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.MetadataConstants;

import com.redhat.lightblue.util.JsonUtils;

public final class AnyType implements Type, Serializable {
    
    private static final long serialVersionUID = 1l;

    public static final Type TYPE = new AnyType();
    public static final String NAME = "any";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean supportsEq() {
        return false;
    }

    @Override
    public boolean supportsOrdering() {
        return false;
    }

    @Override
    public JsonNode toJson(JsonNodeFactory factory, Object obj) {
        return JsonUtils.toJson(obj);
    }
    
    @Override
    public Object fromJson(JsonNode node) {
        return JsonUtils.fromJson(node);
    }

    @Override
    public Object cast(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof JsonNode) {
            return fromJson((JsonNode)obj);
        } else {
            return obj;
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
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
            throw new UnsupportedOperationException(MetadataConstants.ERR_COMPARE_NOT_SUPPORTED);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AnyType;
    }

    @Override
    public int hashCode() {
        return NAME.hashCode();
    }

    @Override
    public String toString() {
        return NAME;
    }

    private AnyType() {
    }
}
