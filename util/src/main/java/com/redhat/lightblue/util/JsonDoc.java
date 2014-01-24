/*
 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.util;

import java.io.Serializable;

import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDoc implements Serializable {

    private static final long serialVersionUID = 1l;

    transient private final JsonNode docRoot;

    private static final Resolver DEFAULT_RESOLVER = new Resolver();
    private static final Resolver CREATING_RESOLVER = new CreatingResolver();

    private static final class Iteration {
        private Iterator<JsonNode> iterator;
        private JsonNode currentNode;
        private int index;

        boolean next() {
            if (iterator.hasNext()) {
                currentNode = iterator.next();
                index++;
                return true;
            } else {
                return false;
            }
        }

        /**
         * @return the currentNode
         */
        public JsonNode getCurrentNode() {
            return currentNode;
        }

        /**
         * @return the index
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * Internal class that overrides the behavior for '*' processing during path resolution
     */
    private static final class CursorResolver extends Resolver {
        private Iteration[] iterators;

        protected JsonNode handleAny(Path p, JsonNode node, int level) {
            JsonNode output = null;
            if (iterators == null) {
                int n = p.numSegments();
                iterators = new Iteration[n];
            }
            if (node instanceof ArrayNode) {
                Iteration itr = iterators[level];
                if (itr == null) {
                    itr = new Iteration();
                    iterators[level] = itr;
                }
                itr.index = -1;
                itr.iterator = ((ArrayNode) node).elements();
                if (itr.next()) {
                    output = itr.getCurrentNode();
                }
            }
            return output;
        }
    }

    /**
     * Internal class containing the algorithm for path resolution starting from a node and path level. Handling of '*'
     * is overridable, by default, throws an exception
     */
    private static class Resolver {
        public JsonNode resolve(Path p, final JsonNode node, int level) {
            JsonNode output = node;
            int n = p.numSegments();
            for (int l = level; l < n; l++) {
                String name = p.head(l);
                JsonNode newOutput;
                if (name.equals(Path.ANY)) {
                    newOutput = handleAny(p, output, l);
                } else if (output instanceof ArrayNode) {
                    int index = Integer.valueOf(name);
                    newOutput = ((ArrayNode) output).get(index);
                } else if (output instanceof ObjectNode) {
                    newOutput = output.get(name);
                } else {
                    newOutput = null;
                }
                if (newOutput == null) {
                    newOutput = handleNullChild(output, p, l);
                }
                output = newOutput;
                if (output == null) {
                    break;
                }
            }
            return output;
        }

        protected JsonNode handleNullChild(JsonNode parent,
                                           Path p,
                                           int level) {
            return null;
        }

        protected JsonNode handleAny(Path p, JsonNode node, int level) {
            throw new IllegalArgumentException(p.toString());
        }
    }

    /**
     * Given a path p=x_1.x_2...x_n, it creates all the intermetiate nodes x_1...x_(n-1), but not the node x_n. However,
     * the node x_(n-1) is created correctly depending on the x_n: if x_n is an index, x_(n-1) is an ArrayNode,
     * otherwise x_(n-1) is an object node.
     */
    private static class CreatingResolver extends Resolver {
        @Override
        protected JsonNode handleNullChild(JsonNode parent,
                                           Path p,
                                           int level) {
            // This function is called because 'parent' does not have
            // a child with name p[level]. So, we will add that
            // child. If p[level+1] is an index, then p[level] must be
            // an array, otherwise, p[level] must be an object node. 

            // First check if p is long enough. There must be one more
            // after level
            if (p.numSegments() <= level + 1) {
                return null;
            }
            // Now determine the child type
            boolean childIsArray = p.isIndex(level + 1);
            if (parent instanceof ArrayNode) {
                ArrayNode arr = (ArrayNode) parent;
                int index = p.getIndex(level);
                // Extend the array to include this index
                int size = arr.size();
                while (size < index) {
                    arr.addNull();
                    size++;
                }
                // Now add the new node. 
                if (childIsArray) {
                    return arr.addArray();
                } else {
                    return arr.addObject();
                }
            } else {
                if (childIsArray) {
                    return ((ObjectNode) parent).putArray(p.head(level));
                } else {
                    return ((ObjectNode) parent).putObject(p.head(level));
                }
            }
        }
    }

    /**
     * A cursor that iterates through all elements of a document that matches the path. If the path has no '*', the
     * initialization code finds the node if any, and the iteration runs only once. If the path contains '*', iterators
     * for all arrays corresponding to '*' are kept in CursorResolver.
     *
     * The algorithms is somewhat complicated because not all elements of the array are guaranteed to have the same
     * structure. For instance, a path of the form x.*.y, when evaluated on a document of the form:
     *
     * <pre>
     *   x : [
     *        { a:1 },
     *        { y:2 },
     *        { y:3 }
     *    ]
     * </pre>
     *
     * the iterator starts iterating from the second element of the array x, because x.0.y does not exist.
     */
    private class PathCursor implements KeyValueCursor<Path, JsonNode> {

        private final Path path;
        private final MutablePath mpath;
        private final CursorResolver resolver = new CursorResolver();
        ;
        private JsonNode nextNode;
        private boolean ended = false;
        private boolean nextFound = false;
        private JsonNode currentNode;
        private Path currentPath;

        public PathCursor(Path p) {
            path = p;
            nextNode = resolver.resolve(path, docRoot, 0);
            if (nextNode != null) {
                nextFound = true;
            }
            if (resolver.iterators == null) {
                ended = true;
                mpath = null;
            } else {
                mpath = new MutablePath(path);
            }
        }

        public Path getCurrentKey() {
            return currentPath;
        }

        public JsonNode getCurrentValue() {
            return currentNode;
        }

        public boolean hasNext() {
            if (!nextFound && !ended) {
                nextNode = seekNext();
            }
            return nextFound;
        }

        public void next() {
            if (!nextFound && !ended) {
                nextNode = seekNext();
            }
            if (nextFound) {
                if (resolver.iterators != null) {
                    int i = 0;
                    for (Iteration x : resolver.iterators) {
                        if (x != null) {
                            mpath.set(i, x.getIndex());
                        }
                        i++;
                    }
                    currentPath = mpath.immutableCopy();
                } else {
                    currentPath = path;
                }
                currentNode = nextNode;
            } else {
                currentPath = null;
                currentNode = null;
            }
            nextFound = false;
            nextNode = null;
        }

        private JsonNode seekNext() {
            nextFound = false;
            JsonNode node = null;
            if (resolver.iterators != null) {
                int n = resolver.iterators.length;
                int level = n - 1;
                boolean done = false;
                do {
                    Iteration itr = resolver.iterators[level];
                    if (itr != null && itr.next()) {
                        node = resolver.resolve(path, itr.getCurrentNode(), level + 1);
                        if (node != null) {
                            nextFound = true;
                            done = true;
                        } else {
                            continue;
                        }
                    } else {
                        level--;
                        if (level < 0) {
                            done = true;
                            ended = true;
                        }
                    }
                } while (!done);
            }
            return node;
        }
    }

    public JsonDoc(JsonNode doc) {
        this.docRoot = doc;
    }

    public JsonNode getRoot() {
        return docRoot;
    }

    public JsonNodeCursor cursor() {
        return cursor(Path.EMPTY);
    }

    public JsonNodeCursor cursor(Path p) {
        return new JsonNodeCursor(p, docRoot);
    }

    /**
     * Returns all nodes matching the path. The path can contain *
     *
     * @param p The path
     *
     * Returns a cursor iterating through all nodes of arrays, if any
     */
    public KeyValueCursor<Path, JsonNode> getAllNodes(Path p) {
        return new PathCursor(p);
    }

    /**
     * Returns a node matching a path
     *
     * @param p The path
     *
     * The path cannot contain *.
     *
     * @returns The node, or null if the node cannot be found
     */
    public JsonNode get(Path p) {
        return get(docRoot, p);
    }

    /**
     * Static utility to resolve a path relative to a node
     */
    public static JsonNode get(JsonNode root, Path p) {
        return DEFAULT_RESOLVER.resolve(p, root, 0);
    }

    /**
     * Modifies an existing node value
     *
     * @param p Path to modify
     * @param newValue new value to set. If null, path is removed from the doc.
     * @param createPath If true, creates all intermediate nodes if they don't exist
     *
     * @return Old value
     */
    public JsonNode modify(Path p, JsonNode newValue, boolean createPath) {
        int n = p.numSegments();
        if (n == 0) {
            throw new IllegalArgumentException("Cannot set empty path value");
        }
        Path parent = p.prefix(-1);
        // Parent must be a container node
        JsonNode parentNode = DEFAULT_RESOLVER.resolve(parent, docRoot, 0);
        if (parentNode == null && createPath) {
            CREATING_RESOLVER.resolve(p, docRoot, 0);
            parentNode = DEFAULT_RESOLVER.resolve(parent, docRoot, 0);
        }
        if (parentNode != null) {
            if (!parentNode.isContainerNode()) {
                throw new IllegalArgumentException(parent.toString() + " is not a container, while setting " + p);
            }
        } else {
            throw new IllegalArgumentException("Parent of " + p + " does not exist");
        }
        JsonNode oldValue;
        String last = p.getLast();
        if (parentNode instanceof ObjectNode) {
            if(Util.isNumber(last)) {
                throw new IllegalArgumentException("Invalid indexed access:"+p);
            }
            ObjectNode obj = (ObjectNode) parentNode;
            if (newValue == null) {
                oldValue = obj.get(last);
                obj.remove(last);
            } else {
                oldValue = obj.replace(last, newValue);
            }
        } else {
            ArrayNode arr = (ArrayNode) parentNode;
            int index;
            try {
                index = Integer.valueOf(last);
            } catch (Exception e) {
                throw new IllegalArgumentException("Array index expected:"+p);
            }
            int size = arr.size();
            while (size < index) {
                arr.addNull();
                size++;
            }
            if (index < size && newValue != null) {
                oldValue = arr.get(index);
                arr.set(index, newValue);
            } else if (newValue == null) {
                oldValue = arr.get(index);
                arr.remove(index);
            } else {
                oldValue = null;
                arr.add(newValue);
            }
        }
        return oldValue;
    }

    @Override
    public String toString() {
        return docRoot.toString();
    }
}
