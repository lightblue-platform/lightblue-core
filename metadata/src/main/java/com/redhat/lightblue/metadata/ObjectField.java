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

import com.redhat.lightblue.util.TreeNode;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class ObjectField extends Field {

    private final Fields fields = new Fields();

    public ObjectField(String name) {
        super(name);
        setType(Constants.TYPE_OBJECT);
    }

    public Fields getFields() {
        return fields;
    }

    public int getNumChildren() {
        return fields.getNumChildren();
    }

    public TreeNode getChild(int index) {
        return fields.getChild(index);
    }

    public TreeNode getChild(String name) {
        return fields.getChild(name);
    }

    public Iterator<? extends TreeNode> getChildren() {
        return fields.getChildren();
    }

    protected TreeNode resolve(Path p,int level) {
        int l=p.numSegments()-level;
        if(l==0)
            return this;
        else {
            String name=p.head(level);
            Error.push(name);
            try {
                if(p.isIndex(level)||
                   name.equals(Path.ANY))
                    throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
                Field f=(Field)getChild(name);
                if(f==null)
                    throw Error.get(Constants.ERR_INVALID_FIELD_REFERENCE);
                return f.resolve(p,level+1);
            } finally {
                Error.pop();
            }
        } 
    }
}
