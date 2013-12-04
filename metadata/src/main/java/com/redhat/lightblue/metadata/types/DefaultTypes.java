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

import java.util.HashMap;

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.TypeResolver;

public final class DefaultTypes implements TypeResolver {

    private final HashMap<String,Type> TYPE_MAP;
    
    public Type getType(String name) {
        return TYPE_MAP.get(name);
    }
    
    public DefaultTypes() {
        TYPE_MAP=new HashMap<String,Type>();
        TYPE_MAP.put(BooleanType.NAME,BooleanType.TYPE);
        TYPE_MAP.put(IntegerType.NAME,IntegerType.TYPE);
        TYPE_MAP.put(BigIntegerType.NAME,BigIntegerType.TYPE);
        TYPE_MAP.put(DoubleType.NAME,DoubleType.TYPE);
        TYPE_MAP.put(BigDecimalType.NAME,BigDecimalType.TYPE);
        TYPE_MAP.put(StringType.NAME,StringType.TYPE);
        TYPE_MAP.put(DateType.NAME,DateType.TYPE);
        TYPE_MAP.put(BinaryType.NAME,BinaryType.TYPE);
        TYPE_MAP.put(ArrayType.NAME,ArrayType.TYPE);
        TYPE_MAP.put(ObjectType.NAME,ObjectType.TYPE);
        TYPE_MAP.put(ReferenceType.NAME,ReferenceType.TYPE);
    }
}


