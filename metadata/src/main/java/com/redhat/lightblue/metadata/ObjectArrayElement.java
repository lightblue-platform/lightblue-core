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

import com.redhat.lightblue.metadata.types.ObjectType;
import com.redhat.lightblue.util.Path;

import java.util.Iterator;

public class ObjectArrayElement extends ArrayElement {

    private static final long serialVersionUID = 1L;
    private final Fields fields;

    public ObjectArrayElement() {
        super(ObjectType.TYPE);
        fields = new Fields(this);
    }

    /**
     * Internal ctor to set fields to another object. Used in
     * ResolvedReferenceField
     */
    private ObjectArrayElement(Fields fields) {
        super(ObjectType.TYPE);
        this.fields = fields;
        fields.setParent(this);
        for (Iterator<Field> itr = fields.getFields(); itr.hasNext();) {
            itr.next().setParent(this);
        }
    }

    /**
     * Creates a new object array element with the given fields. A reference to
     * the given fields object is stored in the instance, so caller can create
     * an object array element, and then add the fields.
     */
    public static ObjectArrayElement withFields(Fields fields) {
        return new ObjectArrayElement(fields);
    }

    public Fields getFields() {
        return fields;
    }

    @Override
    public boolean hasChildren() {
        return true;
    }

    @Override
    public Iterator<? extends FieldTreeNode> getChildren() {
        return fields.getFields();
    }

    @Override
    public FieldTreeNode resolve(Path p, int level) {
        if (p.numSegments() == level) {
            return this;
        } else if (Path.PARENT.equals(p.head(level))) {
            return this.getParent().getParent().resolve(p, level + 1);
        } else {
            return fields.resolve(p, level);
        }
    }
}
