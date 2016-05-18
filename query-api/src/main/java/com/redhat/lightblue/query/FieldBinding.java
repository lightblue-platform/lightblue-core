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
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.Path;

import java.io.Serializable;

/**
 * Base class for a bound value. The bound value cam be a primitive (BoundValue)
 * or a list (BoundValueList). When a field is bound to a value, that query
 * clause is replaced with another one containing a BoundValue or
 * BoundValueList. The FieldBinding for this instance keeps the field path, the
 * value it is bound to, the original query expression (originalQuery), and the
 * new query expression containing the bound value (boundQuery). For instance,
 * consider the following expression:
 *
 * <pre>
 *  { "field":"field1","op":"=","rfield":"field2" }
 * </pre>
 *
 * When <code>field1</code> is bound to a value, the FieldBinding is:
 * <pre> field: field1 originalQuery: {
 * "field":"field1","op":"=","rfield":"field2" } boundQuery:
 * {"field":"field2","op":"=","rvalue":<value> } value: <value> </pre>
 */
public abstract class FieldBinding implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Path field;
    private final QueryExpression originalQuery;
    private final QueryExpression boundQuery;

    public FieldBinding(Path field,
                        QueryExpression originalQ,
                        QueryExpression boundQ) {
        this.field = field;
        this.originalQuery = originalQ;
        this.boundQuery = boundQ;
    }

    /**
     * Returns the field bound to a value.
     */
    public Path getField() {
        return field;
    }

    /**
     * Returns the original query object
     */
    public QueryExpression getOriginalQuery() {
        return originalQuery;
    }

    /**
     * Returns the rewritten query object containing the bound value
     */
    public QueryExpression getBoundQuery() {
        return boundQuery;
    }
}
