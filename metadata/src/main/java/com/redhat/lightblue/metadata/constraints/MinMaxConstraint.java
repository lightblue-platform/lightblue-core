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

import com.redhat.lightblue.metadata.FieldConstraint;

import com.redhat.lightblue.metadata.Type;
import com.redhat.lightblue.metadata.types.IntegerType;
import com.redhat.lightblue.metadata.types.DoubleType;
import com.redhat.lightblue.metadata.types.BigIntegerType;
import com.redhat.lightblue.metadata.types.BigDecimalType;

/**
 * Minimum/maximum number constraint
 */
public class MinMaxConstraint implements FieldConstraint, Serializable {

    private static final long serialVersionUID=1l;
    
    public static final String MIN="minimum";
    public static final String MAX="maximum";

    private final String type;
    private Number value;

    public MinMaxConstraint(String type) {
        this.type=type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isValidForFieldType(Type fieldType) {
        return IntegerType.TYPE.equals(fieldType)||
            DoubleType.TYPE.equals(fieldType)||
            BigDecimalType.TYPE.equals(fieldType)||
            BigIntegerType.TYPE.equals(fieldType);
    }

    public Number getValue() {
        return value;
    }

    public void setValue(Number value) {
        this.value=value;
    }
}
