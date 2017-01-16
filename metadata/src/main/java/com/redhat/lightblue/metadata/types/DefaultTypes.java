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

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.TypeResolver;

import java.util.HashMap;
import java.util.Map;

public final class DefaultTypes implements TypeResolver {

    private final Map<String, Type> typeMap;

    @Override
    public Type getType(String name) {
        return typeMap.get(name);
    }

    public DefaultTypes() {
        typeMap = new HashMap<>();
        typeMap.put(BooleanType.NAME, BooleanType.TYPE);
        typeMap.put(IntegerType.NAME, IntegerType.TYPE);
        typeMap.put(BigIntegerType.NAME, BigIntegerType.TYPE);
        typeMap.put(DoubleType.NAME, DoubleType.TYPE);
        typeMap.put(BigDecimalType.NAME, BigDecimalType.TYPE);
        typeMap.put(StringType.NAME, StringType.TYPE);
        typeMap.put(DateType.NAME, DateType.TYPE);
        typeMap.put(BinaryType.NAME, BinaryType.TYPE);
        typeMap.put(ArrayType.NAME, ArrayType.TYPE);
        typeMap.put(ObjectType.NAME, ObjectType.TYPE);
        typeMap.put(ReferenceType.NAME, ReferenceType.TYPE);
        typeMap.put(AnyType.NAME, AnyType.TYPE);
        typeMap.put(UIDType.NAME, UIDType.TYPE);
    }
}
