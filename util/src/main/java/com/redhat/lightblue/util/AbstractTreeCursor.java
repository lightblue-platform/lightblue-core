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

import java.util.LinkedList;

/**
 * Abstract cursor for tree structures of name-value pairs. The type parameter N denotes the type of the value object.
 * Implementations should provide the hasChildren() and getCursor() methods.
 */
public abstract class AbstractTreeCursor<N> {

    private final LinkedList<LevelState<N>> stack = new LinkedList<>();
    private final MutablePath currentPath;
    private N currentNode;

    private static final class LevelState<T> {
        private final T node;
        private final KeyValueCursor<String, T> cursor;

        public LevelState(T node, KeyValueCursor<String, T> cursor) {
            this.node = node;
            this.cursor = cursor;
        }

        public boolean hasNext() {
            return cursor.hasNext();
        }

        public T next(MutablePath path, boolean newLevel) {
            cursor.next();
            T value = cursor.getCurrentValue();
            if (newLevel) {
                path.push(cursor.getCurrentKey());
            } else {
                path.setLast(cursor.getCurrentKey());
            }
            return value;
        }
    }

    public AbstractTreeCursor(Path p, N start) {
        currentPath = new MutablePath(p);
        if (pushNode(start) == null) {
            throw new IllegalArgumentException(start.getClass().getName());
        }
    }

    public N getCurrentNode() {
        return currentNode;
    }

    public Path getCurrentPath() {
        return currentPath.immutableCopy();
    }

    public boolean firstChild() {
        // If currentNode==null, get the first child of TOS
        // If not null, push current state to stack, and get the first child of TOS
        if (currentNode != null) {
            if (hasChildren(currentNode)) {
                pushNode(currentNode);
            } else {
                return false;
            }
        }
        LevelState<N> tos = stack.peekLast();
        if (tos.hasNext()) {
            currentNode = tos.next(currentPath, true);
        } else {
            return false;
        }
        return true;
    }

    public boolean nextSibling() {
        // Getting the next sibling is done using the iterator of
        // the parent node
        if (currentNode != null) {
            // If currentNode!=null, TOS exists
            LevelState<N> tos = stack.peekLast();
            if (tos.hasNext()) {
                currentNode = tos.next(currentPath, false);
                return true;
            }
        }
        return false;
    }

    public boolean parent() {
        if (stack.size() > 1) {
            stack.removeLast();
            currentPath.pop();
            currentNode = stack.peekLast().node;
        }
        return false;
    }

    public boolean next() {
        boolean done = false;
        do {
            if (firstChild() || nextSibling()) {
                return true;
            } else if (!parent()) {
                done = true;
            }
        } while (!done);
        return false;
    }

    protected abstract KeyValueCursor<String, N> getCursor(N node);

    protected abstract boolean hasChildren(N node);

    private LevelState<N> pushNode(N node) {
        LevelState<N> ret = new LevelState<N>(node, getCursor(node));
        stack.addLast(ret);
        return ret;
    }

}
