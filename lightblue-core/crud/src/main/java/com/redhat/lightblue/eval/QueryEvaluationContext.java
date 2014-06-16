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

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.KeyValueCursor;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Path;

/**
 * An instance of the query evaluation context is passed to query evaluation logic, mainly too keep track of the context
 * for nested queries, and remember what elements of arrays matched the query, if any, once the evaluation is complete.
 */
public class QueryEvaluationContext {

    private JsonNode contextRoot;
    private final MutablePath contextPath;
    private boolean result;

    public QueryEvaluationContext(JsonNode root) {
        this(root, Path.EMPTY);
    }

    public QueryEvaluationContext(JsonNode root, Path p) {
        this.contextRoot = root;
        this.contextPath = p.mutableCopy();
    }

    private QueryEvaluationContext(QueryEvaluationContext ctx, JsonNode root, Path relativePath) {
        this.contextRoot = root;
        this.contextPath = new MutablePath(ctx.contextPath);
        this.contextPath.push(relativePath);
    }

    public JsonNode getNode() {
        return contextRoot;
    }

    public JsonNode getNode(Path relativePath) {
        return JsonDoc.get(contextRoot, relativePath);
    }

    public KeyValueCursor<Path, JsonNode> getNodes(Path relativePath) {
        return new JsonDoc(contextRoot).getAllNodes(new Path(relativePath));
    }

    public Path getPath() {
        return contextPath.immutableCopy();
    }

    public QueryEvaluationContext getNestedContext(Path relativePath) {
        return getNestedContext(JsonDoc.get(contextRoot, relativePath), relativePath);
    }

    public QueryEvaluationContext getNestedContext(JsonNode node, Path relativePath) {
        return new QueryEvaluationContext(this, node, relativePath);
    }

    public QueryEvaluationContext firstElementNestedContext(JsonNode node, Path arrayField) {
        MutablePath p = new MutablePath(arrayField);
        p.push(0);
        return new QueryEvaluationContext(this, node, p);
    }

    public void elementNestedContext(JsonNode node, int index) {
        contextRoot = node;
        contextPath.setLast(index);
    }

    public Path absolutePath(Path p) {
        if (contextPath.numSegments() == 0) {
            return p.immutableCopy();
        } else {
            return new Path(contextPath, p);
        }
    }

    /**
     * query evaluation result
     */
    public boolean getResult() {
        return result;
    }

    public void setResult(boolean b) {
        result = b;
    }
}
