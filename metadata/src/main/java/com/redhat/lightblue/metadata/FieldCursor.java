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

import com.redhat.lightblue.util.AbstractTreeCursor;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.Path;

public class FieldCursor extends AbstractTreeCursor<FieldTreeNode> {

    private static final class Adapter implements KeyValueCursor<String, FieldTreeNode> {

        private Iterator<? extends FieldTreeNode> itr;
        private FieldTreeNode node;

        public Adapter(Iterator<? extends FieldTreeNode> itr) {
            this.itr = itr;
        }

        public boolean hasNext() {
            return itr.hasNext();
        }

        public void next() {
            node = itr.next();
        }

        public String getCurrentKey() {
            return node.getName();
        }

        public FieldTreeNode getCurrentValue() {
            return node;
        }
    }

    public FieldCursor(Path p, FieldTreeNode start) {
        super(p, start);
    }

    protected KeyValueCursor<String, FieldTreeNode> getCursor(FieldTreeNode node) {
        return new Adapter(node.getChildren());
    }

    protected boolean hasChildren(FieldTreeNode node) {
        return node.hasChildren();
    }

}
