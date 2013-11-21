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
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

public final class JsonNodeCursor extends AbstractTreeCursor<JsonNode> {

    private static final class ArrayElementCursor implements KeyValueCursor<String,JsonNode> {
        int index=-1;
        final Iterator<JsonNode> itr;
        JsonNode node;
        
        public ArrayElementCursor(Iterator<JsonNode> itr) {this.itr=itr; }
        public boolean hasNext() {return itr.hasNext();}
        public void next() {node=itr.next();index++; }
        public String getCurrentKey() { return Integer.toString(index); }
        public JsonNode getCurrentValue() { return node; }
    }

    public JsonNodeCursor(Path p,JsonNode start) {
        super(p,start);
    }

    protected KeyValueCursor<String,JsonNode> getCursor(JsonNode node) {
        if(node instanceof ArrayNode)
            return new ArrayElementCursor(((ArrayNode)node).elements());
        else if(node instanceof ObjectNode)
            return new KeyValueCursorIteratorAdapter<String,JsonNode>(((ObjectNode)node).fields());
        else
            throw new IllegalArgumentException(node.getClass().getName());
    }

    protected boolean hasChildren(JsonNode node) {
        return node.isContainerNode()&&node.size()>0;
    }
    
}


