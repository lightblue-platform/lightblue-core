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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ContainerNode;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wrapper class around JSOn documents
 */
public class JsonDoc implements Serializable {

    private static final long serialVersionUID = 1l;

    private final transient JsonNode docRoot;

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
     * Internal class that overrides the behavior for '*' processing during path
     * resolution
     */
    private static final class CursorResolver extends Resolver {
        private Iteration[] iterators;

        @Override
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

    private static class NodeAndLevel {
        final JsonNode node;
        final int level;

        public NodeAndLevel(JsonNode node, int level) {
            this.node = node;
            this.level = level;
        }
    }

    /**
     * Internal class containing the algorithm for path resolution starting from
     * a node and path level. Handling of '*' is overridable, by default, throws
     * an exception
     */
    private static class Resolver {
        public NodeAndLevel resolve(Path p, final JsonNode root, final JsonNode node, int level) {
            JsonNode output = node;

            int n = p.numSegments();
            int newLevel = level;
            for (int l = level; l < n; l++) {
                newLevel = l;
                String name = p.head(l);
                JsonNode newOutput;
                if (name.equals(Path.ANY)) {
                    newOutput = handleAny(p, output, l);
                } else if (name.equals(Path.THIS)) {
                    continue;
                } else if (name.equals(Path.PARENT)) {
                    output = findParent(root, output);
                    if (output instanceof ArrayNode) {
                        output = findParent(root, output);
                    }
                    if (output == null) {
                        throw new IllegalArgumentException(node.toString());
                    }

                    continue;
                } else if (output instanceof ArrayNode) {
                    int index = Integer.valueOf(name);
                    if (index < 0) {
                        newOutput = ((ArrayNode) output).get(((ArrayNode) output).size() + index);
                    } else {
                        newOutput = ((ArrayNode) output).get(index);
                    }
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
            return new NodeAndLevel(output, newLevel);
        }

        protected JsonNode handleNullChild(JsonNode parent, Path p, int level) {
            return null;
        }

        protected JsonNode handleAny(Path p, JsonNode node, int level) {
            throw new IllegalArgumentException(p.toString());
        }

    }

    /**
     * This method here expands our horizons in writing code that sucks.
     * JsonNodes have no parent pointer, so finding a parent involves iterating
     * all nodes with the hope of finding the node, and returning the container
     * that contains it.
     *
     * The whole JsonNode thing should be reengineered at some point.
     */
    public static JsonNode findParent(final JsonNode root, final JsonNode node) {
        if (root instanceof ContainerNode) {
            for (Iterator<JsonNode> itr = root.elements(); itr.hasNext();) {
                JsonNode child = itr.next();
                if (child == node) {
                    return root;
                } else {
                    JsonNode found = findParent(child, node);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Given a path p=x_1.x_2...x_n, it creates all the intermediate nodes
     * x_1...x_(n-1), but not the node x_n. However, the node x_(n-1) is created
     * correctly depending on the x_n: if x_n is an index, x_(n-1) is an
     * ArrayNode, otherwise x_(n-1) is an object node.
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
            } else if (childIsArray) {
                return ((ObjectNode) parent).putArray(p.head(level));
            } else {
                return ((ObjectNode) parent).putObject(p.head(level));
            }
        }
    }

    /**
     * A cursor that iterates through all elements of a document that matches
     * the path. If the path has no '*', the initialization code finds the node
     * if any, and the iteration runs only once. If the path contains '*',
     * iterators for all arrays corresponding to '*' are kept in CursorResolver.
     *
     * The algorithms is somewhat complicated because not all elements of the
     * array are guaranteed to have the same structure. For instance, a path of
     * the form x.*.y, when evaluated on a document of the form:
     *
     * <pre>
     *   x : [
     *        { a:1 },
     *        { y:2 },
     *        { y:3 }
     *    ]
     * </pre>
     *
     * the iterator starts iterating from the second element of the array x,
     * because x.0.y does not exist.
     */
    private class PathCursor implements KeyValueCursor<Path, JsonNode> {

        private final Path path;
        private final MutablePath mpath;
        private final CursorResolver resolver = new CursorResolver();
        private final boolean returnMissingNodes;

        private JsonNode nextNode;
        private boolean ended = false;
        private boolean nextFound = false;
        private JsonNode currentNode;
        private Path currentPath;

        public PathCursor(Path p, boolean returnMissingNodes) {
            this.returnMissingNodes = returnMissingNodes;
            path = p;
            NodeAndLevel nl = resolver.resolve(path, docRoot, docRoot, 0);
            nextNode = nl.node;
            if (nextNode != null || (returnMissingNodes && nl.level == path.numSegments() - 1)) {
                nextFound = true;
            }
            if (resolver.iterators == null) {
                ended = true;
                mpath = null;
            } else {
                mpath = new MutablePath(path);
            }
        }

        @Override
        public Path getCurrentKey() {
            return currentPath;
        }

        @Override
        public JsonNode getCurrentValue() {
            return currentNode;
        }

        @Override
        public boolean hasNext() {
            if (!nextFound && !ended) {
                nextNode = seekNext();
            }
            return nextFound;
        }

        @Override
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
                        NodeAndLevel nl = resolver.resolve(path, docRoot, itr.getCurrentNode(), level + 1);
                        node = nl.node;
                        level = nl.level;
                        if (node != null) {
                            nextFound = true;
                            done = true;
                        } else if (returnMissingNodes && level == path.numSegments() - 1) {
                            nextFound = true;
                            node = null;
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

    /**
     * Creates a JsonDoc with the given root
     */
    public JsonDoc(JsonNode doc) {
        this.docRoot = doc;
    }

    /**
     * Returns the root node
     */
    public JsonNode getRoot() {
        return docRoot;
    }

    /**
     * Returns a cursor that iterates all nodes of the document in a depth-first
     * manner
     */
    public JsonNodeCursor cursor() {
        return cursor(Path.EMPTY);
    }

    /**
     * Returns a cursor that iterates all nodes of the document in a depth first
     * manner, but uses <code>p</code> as a prefix to all the paths during
     * iteration. This method is meant to be used for a JsonDoc rooted at an
     * intermediate node in a Json node tree.
     */
    public JsonNodeCursor cursor(Path p) {
        return cursor(docRoot, p);
    }

    /**
     * Returns a cursor that iterates all the nodes under the given root, where
     * the root is an intermediate node in a Json document accessed by path 'p'.
     * Path can be empty, meaning the 'root' is the real document root.
     */
    public static JsonNodeCursor cursor(JsonNode root, Path p) {
        return new JsonNodeCursor(p, root);
    }

    /**
     * Returns all nodes matching the path. The path can contain *
     *
     * @param p The path
     *
     * Returns a cursor iterating through all nodes of arrays, if any
     */
    public KeyValueCursor<Path, JsonNode> getAllNodes(Path p) {
        return getAllNodes(p, false);
    }

    public KeyValueCursor<Path, JsonNode> getAllNodes(Path p, boolean returnMissingNodes) {
        return new PathCursor(p, returnMissingNodes);
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
        return DEFAULT_RESOLVER.resolve(p, root, root, 0).node;
    }

    /**
     * Modifies an existing node value
     *
     * @param p Path to modify
     * @param newValue new value to set. If null, path is removed from the doc.
     * @param createPath If true, creates all intermediate nodes if they don't
     * exist
     *
     * @return Old value
     */
    public JsonNode modify(Path p, JsonNode newValue, boolean createPath) {
        return modify(docRoot, p, newValue, createPath);
    }

    /**
     * Recursively remove all null nodes in the given json subtree
     *
     * This method operates on the given root node, and returns the same root
     * instance. It does not create a new copy.
     */
    public static JsonNode filterNulls(JsonNode root) {
        if (root instanceof ArrayNode) {
            for (JsonNode element : root) {
                filterNulls(element);
            }
        } else if (root instanceof ObjectNode) {
            ObjectNode o = (ObjectNode) root;
            for (Iterator<Map.Entry<String, JsonNode>> itr = o.fields(); itr.hasNext();) {
                Map.Entry<String, JsonNode> entry = itr.next();
                JsonNode value = entry.getValue();
                if (value == null || value instanceof NullNode) {
                    itr.remove();
                } else {
                    filterNulls(value);
                }
            }
        }
        return root;
    }

    /**
     * Modifies an existing node value
     *
     * @param root The root node
     * @param p Path to modify
     * @param newValue new value to set. If null, path is removed from the doc.
     * @param createPath If true, creates all intermediate nodes if they don't
     * exist
     *
     * @return Old value
     */
    public static JsonNode modify(JsonNode root, Path p, JsonNode newValue, boolean createPath) {
        int n = p.numSegments();
        if (n == 0) {
            throw new IllegalArgumentException(UtilConstants.ERR_CANT_SET_EMPTY_PATH_VALUE);
        }
        Path parent = p.prefix(-1);
        // Parent must be a container node
        JsonNode parentNode = getParentNode(root, parent, createPath, p);
        JsonNode oldValue;
        String last = p.getLast();
        if (parentNode instanceof ObjectNode) {
            oldValue = modifyObjectNode(parentNode, newValue, last, parent);
        } else {
            oldValue = modifyArrayNode((ArrayNode) parentNode, newValue, last, p);
        }
        return oldValue;
    }

    /**
     * Return a list of JsonDoc objects from the given Json node
     *
     * @psram data Json document containing one or more documents
     *
     * The Json document is either an ArrayNode containing Json documents at
     * each element, or an ObjectNode containing only one document.
     */
    public static List<JsonDoc> docList(JsonNode data) {
        ArrayList<JsonDoc> docs = null;
        if (data != null) {
            if (data instanceof ArrayNode) {
                docs = new ArrayList<>(((ArrayNode) data).size());
                for (Iterator<JsonNode> itr = ((ArrayNode) data).elements();
                        itr.hasNext();) {
                    docs.add(new JsonDoc(itr.next()));
                }
            } else if (data instanceof ObjectNode) {
                docs = new ArrayList<>(1);
                docs.add(new JsonDoc(data));
            }
        }
        return docs;
    }

    /**
     * Combines all Json documents in a list into a single Json document
     *
     * @param docs List of JsonDoc objects
     * @param nodeFactory Json node factory
     *
     * @return If the list has only one document, returns an ObjectNode,
     * otherwise returns an array node containing each document in array
     * elements
     */
    public static JsonNode listToDoc(List<JsonDoc> docs, JsonNodeFactory nodeFactory) {
        if (docs == null) {
            return null;
        } else if (docs.isEmpty()) {
            return nodeFactory.arrayNode();
        } else if (docs.size() == 1) {
            return docs.get(0).getRoot();
        } else {
            ArrayNode node = nodeFactory.arrayNode();
            for (JsonDoc doc : docs) {
                node.add(doc.getRoot());
            }
            return node;
        }
    }

    /**
     * Returns a deep copy of the current document
     */
    public JsonDoc copy() {
        return new JsonDoc(docRoot.deepCopy());
    }

    private static JsonNode getParentNode(JsonNode docRoot, Path parent, boolean createPath, Path p) {
        JsonNode parentNode = DEFAULT_RESOLVER.resolve(parent, docRoot, docRoot, 0).node;
        if (parentNode == null && createPath) {
            CREATING_RESOLVER.resolve(p, docRoot, docRoot, 0);
            parentNode = DEFAULT_RESOLVER.resolve(parent, docRoot, docRoot, 0).node;
        }
        if (parentNode != null) {
            if (!parentNode.isContainerNode()) {
                throw new IllegalArgumentException(parent.toString() + UtilConstants.ERR_IS_NOT_A_CONTAINER + p);
            }
        } else {
            throw new IllegalArgumentException(UtilConstants.ERR_PARENT_DOESNT_EXIST + p);
        }
        return parentNode;
    }

    private static JsonNode modifyObjectNode(JsonNode parentNode, JsonNode newValue, String last, Path p) {
        JsonNode oldValue;
        if (Util.isNumber(last)) {
            throw new IllegalArgumentException(UtilConstants.ERR_INVALID_INDEXED_ACCESS + p);
        }
        ObjectNode obj = (ObjectNode) parentNode;
        if (newValue == null) {
            oldValue = obj.get(last);
            obj.remove(last);
        } else {
            oldValue = obj.replace(last, newValue);
        }
        return oldValue;
    }

    private static JsonNode modifyArrayNode(ArrayNode parentNode, JsonNode newValue, String last, Path p) {
        JsonNode oldValue;
        ArrayNode arr = (ArrayNode) parentNode;
        int index;
        try {
            index = Integer.valueOf(last);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(UtilConstants.ERR_EXPECTED_ARRAY_INDEX + p);
        }
        int size = arr.size();
        while (size < index) {
            arr.addNull();
            size++;
        }
        if (index < 0) {
            index = size + index;
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
        return oldValue;
    }

    @Override
    public String toString() {
        return docRoot.toString();
    }
}
