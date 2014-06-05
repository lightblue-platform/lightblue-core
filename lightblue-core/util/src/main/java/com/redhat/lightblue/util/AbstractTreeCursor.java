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
 * An abstract cursor for traversing a tree of name-value pairs. The class parameter N denotes the node type of the
 * underlying tree. For instance, a Json document could be traversed using a concrete implementation extending
 * AbstractTreeCursor<JsonNode>.
 *
 * Every node in the tree is identified by two components: the path to the node, and the actual node object itself. The
 * cursor provides methods to seek to the first child and the next sibling of the current node, as well as the parent
 * node of the current node. The cursor cannot move to the next sibling or to the parent of the root node with which it
 * is initialized.
 *
 * For example:
 * <pre>
 *
 *  |
 *  +-A
 *  |
 *  +-B
 *  | |
 *  | +-C
 *  | |
 *  | +-D
 *  |   |
 *  |   +-E
 *  +-F
 * </pre> Assuming the cursor is initialized with the root node of the above tree:
 * <pre>
 *    cursor.getCurrentNode() => Docroot
 *    cursor.getCurrentPath() => ""
 * </pre> After <code>cursor.getFirstChild()</code>:
 * <pre>
 *    cursor.getCurrentNode() => A
 *    cursor.getCurrentPath() => "A"
 * </pre> At this point, <code>cursor.getFirstChild()</code> will return null. <code>cursor.getNextSibling()</code> will
 * seek to node B. After calling <code>cursor.getFirstChild()</code>:
 * <pre>
 *     cursor.getCurrentNode() => C
 *     cursor.getCurrentPath() => "B.C"
 * </pre>
 *
 * The concrete implementation must provide two methods to provide access to the underlying tree structure:
 * <ul>
 * <li>hasChildren: Return if the node has children</li>
 * <li>getCursor: If the node has children, return a KeyValueCursor to iterate the elements of the node. In the above
 * example, a <code>getCursor</code> call with node B as the argument should return a cursor that will return C and
 * D.</li>
 * </ul>
 */
public abstract class AbstractTreeCursor<N> {

    /**
     * Iteration state for every level is kept in a stack.
     */
    private final LinkedList<LevelState<N>> stack = new LinkedList<>();

    /**
     * Current absolute path in tree
     */
    private final MutablePath currentPath;

    /**
     * Current node in tree. Never null
     */
    private N currentNode;

    /**
     * Current cursor we're using to iterate the level. Never null.
     */
    private KeyValueCursor<String,N> currentCursor;

    private static final class SingleElemCursor<T> implements KeyValueCursor<String,T> {
        private T value;
        private final String key;
        private boolean fetched=false;

        SingleElemCursor(String key,T value) {
            this.key=key;
            this.value=value;
        }

        @Override
        public boolean hasNext() {
            return !fetched;
        }
        @Override
        public  void next() {
            fetched=true;
        }
        @Override
        public T getCurrentValue() {
            return value;
        }

        @Override
        public String getCurrentKey() {
            return key;
        }
    }

    /**
     * Keeps the node and the cursor for the level
     */
    private static final class LevelState<T> {
        private final T node;
        private final KeyValueCursor<String, T> cursor;

        public LevelState(T node, KeyValueCursor<String, T> cursor) {
            this.node = node;
            this.cursor = cursor;
        }

        public String toString() {
            return node.toString();
        }
    }

    /**
     * Construct a cursor at a given location in a tree
     *
     * @param p Path to the root node for the cursor. If docroot, empty path.
     * @param start The root node
     */
    public AbstractTreeCursor(Path p, N start) {
        currentPath = new MutablePath(p);
        currentNode = start;
        if(!hasChildren(start)) {
            throw new IllegalArgumentException("Not iterable");
        }
        currentCursor=new SingleElemCursor("",start);
        currentCursor.next();
    }

    /**
     * Get the current node the cursor is pointing to
     */
    public N getCurrentNode() {
        return currentNode;
    }

    /**
     * Get the current path the cursor is pointing to
     */
    public Path getCurrentPath() {
        return currentPath.immutableCopy();
    }

    /**
     * Attempt to seek the cursor to the first child of the current node
     *
     * @return If the current node does not have any children, returns false, and cursor still points to the node it was
     * pointing to before the call. If the current node has children, cursor points to the first child of the node, and
     * true is returned.
     */
    public boolean firstChild() {
        // push current state to stack, and get the first child of TOS
        if (hasChildren(currentNode)) {
            push();
            currentCursor=getCursor(currentNode);
            currentCursor.next();
            currentNode=currentCursor.getCurrentValue();
            currentPath.push(currentCursor.getCurrentKey());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to seek the cursor to the next sibling of the current node
     *
     * @return If the current node is the last node of its parent, returns false, and the cursor still points to the
     * same node. Otherwise, cursor points to the next sibling and true is returned.
     */
    public boolean nextSibling() {
        if(currentCursor.hasNext()) {
            currentCursor.next();
            currentNode=currentCursor.getCurrentValue();
            currentPath.setLast(currentCursor.getCurrentKey());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempt to seek the cursor to the parent of the current node
     *
     * @return If the node has a parent that is a descendent of the root node with which the cursor is initialized,
     * seeks to the parent and returns true. Otherwise, returns false and cursor still points to the same node.
     */
    public boolean parent() {
        if (!stack.isEmpty()) {
            pop();
            currentPath.pop();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Attempts to seek to the next node in a depth-first manner
     *
     * @return If there is a next node in a depth-first manner, returns true and seeks to that node. Otherwise, returns
     * false and the cursor continues to point to the same node.
     */
    public boolean next() {
        boolean done = false;
        boolean backing = false;
        do {
            if (backing) {
                if (nextSibling()) {
                    return true;
                } else if (!parent()) {
                    done = true;
                }
            } else {
                if (firstChild() || nextSibling()) {
                    return true;
                } else if (!parent()) {
                    done = true;
                } else {
                    backing = true;
                }
            }
        } while (!done);
        return false;
    }

    /**
     * Returns a cursor over the immediate children of the node
     *
     * @param node The node for which the cursor will be returned
     */
    protected abstract KeyValueCursor<String, N> getCursor(N node);

    /**
     * Returns true if the node given node has children
     */
    protected abstract boolean hasChildren(N node);

    private void push() {
        stack.addLast(new LevelState<N>(currentNode,currentCursor));
    }

    private void pop() {
        LevelState<N> last=stack.removeLast();
        currentNode=last.node;
        currentCursor=last.cursor;
    }

}
