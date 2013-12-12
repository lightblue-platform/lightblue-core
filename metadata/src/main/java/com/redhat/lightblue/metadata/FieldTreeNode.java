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

import com.redhat.lightblue.util.EmptyIterator;
import com.redhat.lightblue.util.Path;

public interface FieldTreeNode {

    public static final Iterator<FieldTreeNode> EMPTY=new EmptyIterator<FieldTreeNode>();

    /**
     * Return field name
     */
    public String getName();

    /**
     * Return field type
     */
    public Type getType();

    /**
     * Returns true if field has children
     */
    public boolean hasChildren();

    /**
     * Returns an iterator over the children of the field
     */
    public Iterator<? extends FieldTreeNode> getChildren();

    public FieldTreeNode resolve(Path p);
}
