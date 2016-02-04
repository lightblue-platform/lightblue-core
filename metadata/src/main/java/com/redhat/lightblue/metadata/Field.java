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

import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

import java.io.Serializable;
import java.util.*;

/**
 * Represents arrays and objects.
 */
public abstract class Field implements FieldTreeNode, Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private String description;
    private Type type;
    private final FieldAccess access = new FieldAccess();
    private final List<FieldConstraint> constraints = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();

    private FieldTreeNode parent;

    public Field(String name) {
        this.name = name;
    }

    public Field(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Shallow copy data from source to this Field.
     *
     * @param source
     */
    public void shallowCopyFrom(Field source) {
        setType(source.getType());
        setDescription(source.getDescription());

        FieldAccess da = getAccess();
        FieldAccess sa = source.getAccess();
        da.getFind().setRoles(sa.getFind());
        da.getUpdate().setRoles(sa.getUpdate());
        da.getInsert().setRoles(sa.getInsert());
        setConstraints(source.getConstraints());
        getProperties().putAll(source.getProperties());
    }

    @Override
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public FieldAccess getAccess() {
        return this.access;
    }

    @Override
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

    @Override
    public FieldTreeNode resolve(Path p) {
        return resolve(p, 0);
    }

    @Override
    public MutablePath getFullPath(MutablePath mp) {
        if (parent != null) {
            parent.getFullPath(mp);
        }
        mp.push(name);
        return mp;
    }

    @Override
    public Path getFullPath() {
        return getFullPath(new MutablePath()).immutableCopy();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public abstract FieldTreeNode resolve(Path p, int level);
}
