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

public final class NodeIterator {
    private final LinkedList<LevelState> stack=new LinkedList<LevelState>();
    private MutablePath currentPath;
    private JsonNode currentNode;

    private static final class Pair implements Map.Entry<String,JsonNode> {
        String key;
        JsonNode value;
        
        public String getKey() {return key;}
        public JsonNode getValue() {return value;}
        public JsonNode setValue(JsonNode v) {value=v;return value;}
    }

    private static final class ArrayIteratorAdapter 
        implements Iterator<Map.Entry<String,JsonNode>> {
        private final Iterator<JsonNode> nodeIterator;
        private int index=0;
        private Pair node=new Pair();
        
        public ArrayIteratorAdapter(Iterator<JsonNode> itr) {
            this.nodeIterator=itr;
        }
        
        public boolean hasNext() {
            return nodeIterator.hasNext();
        }
        
        public Map.Entry<String,JsonNode> next() {
            node.key=Integer.toString(index++);
            node.value=nodeIterator.next();
            return node;
        }
        
        public void remove() {
            nodeIterator.remove();
        }
    }

    private static final class LevelState {
        final JsonNode node;
        final Iterator<Map.Entry<String,JsonNode>> iterator;
        
        public LevelState(ObjectNode node) {
            this.node=node;
            this.iterator=node.fields();
        }
        
        public LevelState(ArrayNode node) {
            this.node=node;
            this.iterator=new ArrayIteratorAdapter(node.elements());
        }
        
        public boolean hasNext() {
            return iterator.hasNext();
        }
        
        public JsonNode next(MutablePath path,boolean newLevel) {
            Map.Entry<String,JsonNode> value=iterator.next();
            if(newLevel)
                path.push(value.getKey());
            else
                path.setLast(value.getKey());
            return value.getValue();
        }
    }
        
    public NodeIterator(Path p,JsonNode start) {
        currentPath=new MutablePath(p);
        if(pushNode(start)==null)
            throw new IllegalArgumentException(start.getClass().getName());
    }

    public JsonNode getCurrentNode() {
        return currentNode;                
    }
    
    public Path getCurrentPath() {
        return currentPath.immutableCopy();
    }
    
    public boolean firstChild() {
        // If currentNode==null, get the first child of TOS
        // If not null, push current state to stack, and get the first child of TOS
        if(currentNode!=null) {
            if(currentNode.isContainerNode()&&currentNode.size()>0) {
                pushNode(currentNode);
            } else
                return false;
        }
        LevelState tos=stack.peekLast();
        if(tos.hasNext()) {
            currentNode=tos.next(currentPath,true);
        } else {
            return false;
        }
        return true;
    }
    
    public boolean nextSibling() {
        // Getting the next sibling is done using the iterator of
        // the parent node
        if(currentNode!=null) {
            // If currentNode!=null, TOS exists
            LevelState tos=stack.peekLast();
            if(tos.hasNext()) {
                currentNode=tos.next(currentPath,false);
                return true;
            } 
        } 
        return false;
    }
    
    public boolean parent() {
        if(stack.size()>1) {
            stack.removeLast();
            currentPath.pop();
            currentNode=stack.peekLast().node;
        }
        return false;
    }

    public boolean next() {
        boolean done=false;
        do {
            if(firstChild() || nextSibling()) {
                return true;
            } else if(!parent())
                done=true;
        } while(!done);
        return false;
    }

    private LevelState pushNode(JsonNode node) {
        LevelState ret;
        if(node instanceof ObjectNode)
            ret=new LevelState((ObjectNode)node);
        else if(node instanceof ArrayNode)
            ret=new LevelState((ArrayNode)node);
        else
            ret=null;
        if(ret!=null)
            stack.addLast(ret);
        return ret;
    }
    
}


