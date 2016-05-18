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

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

import java.util.Iterator;

/**
 * Represents a simple field, e.g. string or number. Objects and arrays are not
 * simple fields.
 *
 */
public class SimpleField extends Field {

    private static final long serialVersionUID = 1L;

    private ValueGenerator valueGenerator;

    public SimpleField(String name) {
        super(name);
    }

    public SimpleField(String name, Type type) {
        super(name, type);
    }

    public SimpleField(String name, Type type, ValueGenerator valueGenerator) {
        this(name, type);
        this.valueGenerator = valueGenerator;
    }

    @Override
    public void shallowCopyFrom(Field source) {
        super.shallowCopyFrom(source);
        this.valueGenerator = ((SimpleField) source).valueGenerator;
    }

    @Override
    public Iterator<FieldTreeNode> getChildren() {
        return FieldTreeNode.EMPTY;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public FieldTreeNode resolve(Path p, int level) {
        if (p.numSegments() == level) {
            return this;
        } else if (Path.PARENT.equals(p.head(level))) {
            return this.getParent().resolve(p, level + 1);
        } else {
            throw Error.get(MetadataConstants.ERR_INVALID_FIELD_REFERENCE, p.head(level) + " in " + p.toString());
        }
    }

    public ValueGenerator getValueGenerator() {
        return valueGenerator;
    }

    public void setValueGenerator(ValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

}
