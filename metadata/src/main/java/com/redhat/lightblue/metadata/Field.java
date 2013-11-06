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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import com.redhat.lightblue.util.TreeNode;
import com.redhat.lightblue.util.Path;

public abstract class Field implements TreeNode, Serializable {

    private static final long serialVersionUID = 1l;

    private final String name;
    private String type;
    private final FieldAccess access=new FieldAccess();
    private final List<FieldConstraint> constraints=new ArrayList<FieldConstraint>();

    public Field(String name) {
        this.name = name;
    }

    public Field(String name,String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FieldAccess getAccess() {
        return this.access;
    }

    public List<FieldConstraint> getConstraints() {
        return new ArrayList<FieldConstraint>(constraints);
    }

    public void setConstraints(Collection<FieldConstraint> l) {
        constraints.clear();
        if(l!=null)
            constraints.addAll(l);
    }

    protected abstract TreeNode resolve(Path p,int level);
}
