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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

public abstract class Field implements FieldTreeNode, Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private Type type;
    private final FieldAccess access = new FieldAccess();
    private final List<FieldConstraint> constraints = new ArrayList<>();

    private FieldTreeNode parent;

    public Field(String name) {
        this.name = name;
    }

    public Field(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public FieldAccess getAccess() {
        return this.access;
    }

    public FieldTreeNode getParent() {
        return parent;
    }

    public void setParent(FieldTreeNode field) {
        this.parent = field;
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

    public FieldTreeNode resolve(Path p) {
        return resolve(p, 0);
    }

    public MutablePath getFullPath(MutablePath mp) {
        if (parent != null) {
            parent.getFullPath(mp);
        }
        mp.push(name);
        return mp;
    }

    public Path getFullPath() {
        return getFullPath(new MutablePath()).immutableCopy();
    }

    public abstract FieldTreeNode resolve(Path p, int level);
}
