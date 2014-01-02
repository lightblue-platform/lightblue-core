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

import java.util.Iterator;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.metadata.types.ObjectType;

public class ObjectField extends Field {

    private final Fields fields = new Fields();

    public ObjectField(String name) {
        super(name, ObjectType.TYPE);
    }

    public Fields getFields() {
        return fields;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    public Iterator<? extends FieldTreeNode> getChildren() {
        return fields.getFields();
    }

    protected FieldTreeNode resolve(Path p, int level) {
        int l = p.numSegments() - level;
        if (l == 0) {
            return this;
        } else {
            String name = p.head(level);
            Error.push(name);
            try {
                if (p.isIndex(level)
                        || name.equals(Path.ANY)) {
                    throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
                }
                Field f = fields.getField(name);
                if (f == null) {
                    throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
                }
                return f.resolve(p, level + 1);
            } finally {
                Error.pop();
            }
        }
    }
}
