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
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

/**
 * Interface for items of the field tree. This interface allows resolution of Paths based on an arbitrary location in
 * the field tree.
 */
public interface FieldTreeNode {

    Iterator<FieldTreeNode> EMPTY = new EmptyIterator<>();

    /**
     * Return field name
     */
    String getName();

    /**
     * Return field type
     */
    Type getType();

    /**
     * Returns true if field has children
     */
    boolean hasChildren();

    /**
     * Returns an a FieldTreeNode that is the parent of this one
     */
    FieldTreeNode getParent();

    /**
     * Returns an iterator over the children of the field
     */
    Iterator<? extends FieldTreeNode> getChildren();

    /**
     * Returns the field tree node for the given Path relative to this
     */
    FieldTreeNode resolve(Path p);

    /**
     * Returns the field tree node for the given Path relative to this, starting at a specific level
     */
    FieldTreeNode resolve(Path p, int level);

    /**
     * Returns the full non-relative path to this based on parent hierarchy
     */
    Path getFullPath();

    /**
     * Builds the full path by calling parent.getFullPath() first, and then adding the current segment. Returns mp.
     */
    MutablePath getFullPath(MutablePath mp);
}
