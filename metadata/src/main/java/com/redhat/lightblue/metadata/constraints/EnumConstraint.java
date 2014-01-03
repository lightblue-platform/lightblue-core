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
package com.redhat.lightblue.metadata.constraints;

import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import com.redhat.lightblue.metadata.FieldConstraint;

import com.redhat.lightblue.metadata.types.Type;
import com.redhat.lightblue.metadata.types.StringType;

/**
 * Enumerated field constraint
 */
public class EnumConstraint implements FieldConstraint, Serializable {

    private static final long serialVersionUID = 1l;

    public static final String TYPE = "enum";

    private final HashSet<String> values = new HashSet<String>();

    public EnumConstraint() {
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean isValidForFieldType(Type fieldType) {
        return StringType.TYPE.equals(fieldType);
    }

    public Set<String> getValues() {
        return (Set<String>) values.clone();
    }

    public void setValues(Collection<String> values) {
        this.values.clear();
        if (values != null) {
            this.values.addAll(values);
        }
    }

}
