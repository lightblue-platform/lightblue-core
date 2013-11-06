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

import java.util.ArrayList;
import java.util.Iterator;

import com.redhat.lightblue.util.TreeNode;
import com.redhat.lightblue.util.Util;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class ArrayField extends Field {

    private ArrayElement element;

    public ArrayField(String name) {
        super(name,Constants.TYPE_ARRAY);
    }

    public ArrayField(String name,ArrayElement el) {
        super(name,Constants.TYPE_ARRAY);
        element=el;
    }

    public ArrayElement getElement() {
        return element;
    }

    public void setElement(ArrayElement el) {
        element = el;
    }

    public int getNumChildren() {
        return element == null ? 0 : 1;
    }

    public TreeNode getChild(int index) {
        if (index == 0 && element != null)
            return element;
        return null;
    }

    public TreeNode getChild(String name) {
        if (Constants.ARRAY_ANY_ELEM.equals(name) || Util.isNumber(name))
            if (element != null)
                return element;
        return null;
    }

    public Iterator<? extends TreeNode> getChildren() {
        if (element != null) {
            ArrayList<TreeNode> l = new ArrayList<TreeNode>(1);
            l.add(element);
            return l.iterator();
        }
        return null;
    }

    protected TreeNode resolve(Path p,int level) {
        int l=p.numSegments()-level;
        if(l==0)
            return this;
        else {
            Error.push(p.head(level));
            try {
                if(p.isIndex(level)||
                   p.head(level).equals(Path.ANY))
                    return element.resolve(p,level+1);
                else
                    throw Error.get(Constants.ERR_INVALID_ARRAY_REFERENCE);
            } finally {
                Error.pop();
            }
        } 
    }

}
