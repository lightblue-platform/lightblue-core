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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Specifies a named enumeration with values in metadata (entity info).
 */
public class Enum implements Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private final Set<EnumValue> values = new HashSet<>();

    /**
     * Default ctor
     */
    public Enum(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public void setValues(Set<EnumValue> v) {
        values.clear();
        if (v != null) {
            values.addAll(v);
        }
    }

    public void setValues(Collection<String> values){
        Set<EnumValue> evSet = new HashSet<EnumValue>();
        for(String string : values){
            evSet.add(new EnumValue(string, null));
        }
        setValues(evSet);
    }

    /**
     * The {@link EnumValue}s allowed in this enumeration.
     */
    public Set<EnumValue> getEnumValues() {
        return new HashSet<EnumValue>(values);
    }

    /**
     * The values allowed in this enumeration.
     */
    public Set<String> getValues(){
        Set<String> strings = new HashSet<String>();
        for(EnumValue v : values){
            strings.add(v.getName());
        }
        return strings;
    }
}
