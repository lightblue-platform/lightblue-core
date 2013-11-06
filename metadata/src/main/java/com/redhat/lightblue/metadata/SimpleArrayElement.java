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

public class SimpleArrayElement extends ArrayElement {
    public int getNumChildren() {
        return 0;
    }

    public TreeNode getChild(int index) {
        return null;
    }

    public TreeNode getChild(String name) {
        return null;
    }

    public Iterator<? extends TreeNode> getChildren() {
        return TreeNode.EMPTY;
    }

    protected TreeNode resolve(Path p,int level) {
        if(p.numSegments()==level)
            return this;
        else
            throw Error.get(Constants.ERR_INVALID_ARRAY_REFERENCE);
    }
}
