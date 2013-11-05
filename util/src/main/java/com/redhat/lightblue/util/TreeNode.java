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

package com.redhat.lightblue.util;

import java.util.Iterator;

/**
 * A generic tree node.
 */
public interface TreeNode {

    public static final Iterator<TreeNode> EMPTY=new EmptyIterator<TreeNode>();

    /**
     * Returns the number children of this node
     */
    public int getNumChildren();

    /**
     * Returns a child node by index. Index starts from 0. Returns null if index
     * is out of bounds.
     */
    public TreeNode getChild(int index);

    /**
     * Returns a child node by name. Returns null if the named child does not
     * exist.
     */
    public TreeNode getChild(String name);

    /**
     * Returns an iterator of children
     */
    public Iterator<? extends TreeNode> getChildren();

}
