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

import com.redhat.lightblue.util.Error;

public final class BinaryType implements Type, Serializable {

    private static final long serialVersionUID=1l;

    public static final Type TYPE=new BinaryType();
    public static final String NAME="binary";

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
        public JsonNode toJson(JsonNodeFactory factory,Object obj) {
        if(obj.getClass().isArray()) {
            Class<?> component=obj.getClass().getComponentType();
            if(component.equals(byte.class)||
               component.equals(Byte.class)) {
                return factory.binaryNode((byte[])obj);
            }
        }
        throw Error.get(NAME,ERR_INCOMPATIBLE_VALUE,obj.toString());
    }

    @Override
    public Object fromJson(JsonNode node) {
        if(node.isValueNode())
            try {
                return node.binaryValue();
            } catch (Exception e) {}
        throw Error.get(NAME,ERR_INCOMPATIBLE_VALUE,node.toString());
    }

    @Override
    public boolean equals(Object obj) {
        return obj!=null&&obj instanceof BinaryType;
    }

    @Override
    public int hashCode() {
        return 8;
    }

    @Override
    public String toString() {
        return NAME;
    }

    private BinaryType() {}
}
