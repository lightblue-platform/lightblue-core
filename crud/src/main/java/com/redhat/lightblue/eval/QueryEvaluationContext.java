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
package com.redhat.lightblue.eval;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;

/**
 * An instance of the query evaluation context is passed to query
 * evaluation logic, mainly too keep track of the context for nested
 * queries, and remember what elements of arrays matched the query, if
 * any, once the evaluation is complete.
 */
public class QueryEvaluationContext {
    private final JsonNode root;
    private final Map<Path,List<Integer>> matchingArrayElements=new HashMap<Path,List<Integer>>();
    private boolean result;
    private final ArrayList<StackItem> context=new ArrayList<StackItem>(16);
    private final MutablePath currentPath=new MutablePath();

    private static final class StackItem {
        JsonNode node;
        final int pathLength;

        public StackItem(JsonNode node,int length) {
            this.node=node;
            this.pathLength=length;
        }
    }

    public QueryEvaluationContext(JsonNode root) {
        this.root=root;
        context.add(new StackItem(root,0));
    }

    public JsonNode getRoot() {
        return root;
    }

    public QueryEvaluationContext getNestedContext() {
        return new QueryEvaluationContext(getCurrentContextNode());
    }

    public Map<Path,List<Integer>> getMatchingArrayElements() {
        return matchingArrayElements;
    }

    public List<Integer> getMatchingArrayElements(Path p) {
        return matchingArrayElements.get(p);
    }

    public boolean isMatchingElement(Path p) {
        List<Integer> l=getMatchingArrayElements(p.prefix(-1));
        if(l!=null)
            return l.contains(p.getIndex(p.numSegments()-1));
        return false;
    }

    /**
     * Adds a matching array element index for the given field.
     */
    public void addMatchingArrayElement(Path p,int index) {
        List<Integer> l=matchingArrayElements.get(p);
        if(l==null)
            matchingArrayElements.put(p.immutableCopy(),l=new ArrayList<Integer>());
        l.add(index);
    }

    /**
     * query evaluation result 
     */
    public boolean getResult() {
        return result;
    }

    public void setResult(boolean b) {
        result=b;
    }

    public JsonNode getCurrentContextNode() {
        return context.get(context.size()-1).node;
    }

    public Path getCurrentContextPath() {
        return currentPath.immutableCopy();
    }

    /**
     * Pushes a relative path, and the node corresponding to it
     */
    public void push(JsonNode node,Path p) {
        currentPath.push(p);
        context.add(new StackItem(node,currentPath.numSegments()));
    }

    /**
     * Pushed an array index and the node corresponding to that element
     */
    public void push(JsonNode node,int index) {
        currentPath.push(index);
        context.add(new StackItem(node,currentPath.numSegments()));
    }

    /**
     * Rewrites the last index and node
     */
    public void setLast(JsonNode node,int index) {
        currentPath.setLast(index);
        context.get(context.size()-1).node=node;
    }

    /**
     * Pops the context
     */
    public void pop() {
        int n=context.size()-1;
        context.remove(n);
        currentPath.cut(context.get(n-1).pathLength);
    }
}
