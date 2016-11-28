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
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class SimpleArrayElement extends ArrayElement {

    private static final long serialVersionUID = 1L;

    private final List<FieldConstraint> constraints = new ArrayList<>();

    public SimpleArrayElement() {
    }

    public SimpleArrayElement(Type type) {
        super(type);
    }

    /**
     * @return a deep copy of the constraints
     */
    public List<FieldConstraint> getConstraints() {
        return new ArrayList<>(constraints);
    }

    public void setConstraints(Collection<FieldConstraint> l) {
        constraints.clear();
        if (l != null) {
            constraints.addAll(l);
        }
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public Iterator<FieldTreeNode> getChildren() {
        return FieldTreeNode.EMPTY;
    }

    @Override
    public FieldTreeNode resolve(Path p, int level) {
        if (p.numSegments() == level) {
            return this;
        } else if (p.head(level).equals(Path.PARENT)) {
            return this.getParent().getParent().resolve(p, level + 1);
        } else if (p.head(level).equals(Path.THIS)) {
            return this.resolve(p,level+1);
        } else {
            throw Error.get(MetadataConstants.ERR_INVALID_ARRAY_REFERENCE,p.toString());
        }
    }

}
