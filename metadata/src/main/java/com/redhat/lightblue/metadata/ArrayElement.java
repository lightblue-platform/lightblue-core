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
import java.util.HashMap;
import java.util.Map;

public abstract class ArrayElement implements FieldTreeNode, Serializable {

    private static final long serialVersionUID = 1l;

    private Type type;

    private FieldTreeNode parent = null;
    private final Map<String, Object> properties = new HashMap<String, Object>();

    public ArrayElement() {
    }

    public ArrayElement(Type type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return Path.ANY;
    }

    /**
     * Gets the value of type
     *
     * @return the value of type
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Sets the value of type
     *
     * @param argType Value to assign to this.type
     */
    public void setType(Type argType) {
        this.type = argType;
    }

    @Override
    public FieldTreeNode resolve(Path p) {
        return resolve(p, 0);
    }

    public abstract FieldTreeNode resolve(Path p, int level);

    @Override
    public FieldTreeNode getParent() {
        return parent;
    }

    protected void setParent(FieldTreeNode node) {
        parent = node;
    }

    public MutablePath getFullPath(MutablePath mp) {
        parent.getFullPath(mp);
        mp.push(Path.ANY);
        return mp;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public Path getFullPath() {
        return getFullPath(new MutablePath()).immutableCopy();
    }

}
