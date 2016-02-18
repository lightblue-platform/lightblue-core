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
package com.redhat.lightblue.assoc;

import java.util.List;

import com.redhat.lightblue.query.Value;

/**
 * Represents a field bound to a list of values. This contains the
 * list within a query clause that is bound to an array field in
 * another document.
 */
public class ListBinding extends FieldBinding {
    private final List<Value> value;

    public ListBinding(QueryFieldInfo fieldInfo,
                       List<Value> value) {
        super(fieldInfo);
        this.value=value;
    }
    
    public List<Value> getValue() {
        return value;
    }
}
