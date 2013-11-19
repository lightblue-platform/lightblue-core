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

import java.io.Serializable;

import java.util.Map;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonDoc implements Serializable {

    private static final long serialVersionUID=1l;

    private final JsonNode docRoot;

    private static final Iterator<Map.Entry<Path,JsonNode>> EMPTY_ITERATOR=
        new Iterator<Map.Entry<Path,JsonNode>>() {
        public boolean hasNext() { return false;}
        public Map.Entry<Path,JsonNode> next() {throw new NoSuchElementException();}
        public void remove() {throw new UnsupportedOperationException();}
    };

    public JsonDoc(JsonNode doc) {
        this.docRoot=doc;
    }

    public NodeIterator iterator() {
        return iterator(Path.EMPTY);
    }

    public NodeIterator iterator(Path p) {
        return new NodeIterator(p,docRoot);
    }

    public JsonNode get(Path p) {
        JsonNode current = docRoot;
        int n = p.numSegments();
        for (int level = 0; level < n; level++) {
            String name = p.head(level);
            if (name.equals(Path.ANY))
                return null;
            else if(current instanceof ArrayNode) {
                int index=Integer.valueOf(name);
                current=((ArrayNode)current).get(index);
            } else if(current instanceof ObjectNode) {
                current=current.get(name);
            }
            if(current==null)
                break;
        }
        return current;
    }
}
