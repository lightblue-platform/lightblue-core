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

import com.redhat.lightblue.query.Value;

/**
 * An extension of value with fixed null value, but containing field query field
 * info for the field. When a query is rewritten, all fields referenced in the
 * query that belong to another entity are replaced with a BoundValue instance.
 * When the actual value of the field is determined, the query is rewritten to
 * replace the BoundValue with the actual value.
 */
public class BoundValue extends Value implements BoundObject {
    private static final long serialVersionUID = 1L;

    protected final QueryFieldInfo fieldInfo;

    public BoundValue(QueryFieldInfo fieldInfo) {
        super(null);
        this.fieldInfo = fieldInfo;
    }

    @Override
    public QueryFieldInfo getFieldInfo() {
        return fieldInfo;
    }
}
