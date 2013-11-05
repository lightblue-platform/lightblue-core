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
import java.util.HashMap;
import java.util.Iterator;

import com.redhat.lightblue.util.TreeNode;
import com.redhat.lightblue.util.Path;

public class Fields implements TreeNode, Serializable {

    private static final long serialVersionUID = 1l;

    private final HashMap<String, Field> fieldMap = new HashMap<String, Field>();
    private final ArrayList<Field> fields = new ArrayList<Field>();

    public String getNodeName() {
        return "";
    }

    public int getNumChildren() {
        return fields.size();
    }

    public TreeNode getChild(int index) {
        try {
            return fields.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    public Field getField(int index) {
        return (Field) getChild(index);
    }

    public TreeNode getChild(String name) {
        return fieldMap.get(name);
    }

    public Field getField(String name) {
        return (Field) getChild(name);
    }

    public Iterator<? extends TreeNode> getChildren() {
        return fields.iterator();
    }

    public Iterator<Field> getFields() {
        return fields.iterator();
    }

    public boolean has(String name) {
        return fieldMap.containsKey(name);
    }

    public void addNew(Field f) {
        String name = f.getName();
        if (has(name))
            throw new DuplicateField(name);
        fieldMap.put(name, f);
        fields.add(f);
    }

    public void put(Field f) {
        String name = f.getName();
        if (has(name)) {
            int n = fields.size();
            for (int i = 0; i < n; i++) {
                Field x = fields.get(i);
                if (x.getName().equals(name)) {
                    fields.set(i, f);
                    break;
                }
            }
        } else
            fields.add(f);
        fieldMap.put(name, f);
    }

    public TreeNode resolve(Path p) {
        return resolve(p,0);
    }

    protected TreeNode resolve(Path p,int level) {
        if(level>=p.numSegments())
            throw new InvalidRedirection(p);
        String name=p.head(level);

        if(p.isIndex(level))
            throw new InvalidArrayReference(p,name);
        if(name.equals(Path.ANY))
            throw new InvalidArrayReference(p,name);

        Field field=getField(name);
        if(field==null)
            throw new InvalidFieldReference(p,name);
        return field.resolve(p,level+1);
    }

}
