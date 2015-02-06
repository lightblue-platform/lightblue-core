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

/**
 * Skeleton implementation of query iteration.
 */
public abstract class QueryIteratorSkeleton<T> {

    protected abstract T itrAllMatchExpression(AllMatchExpression q, Path context);
    protected abstract T itrValueComparisonExpression(ValueComparisonExpression q, Path context);
    protected abstract T itrFieldComparisonExpression(FieldComparisonExpression q, Path context);
    protected abstract T itrRegexMatchExpression(RegexMatchExpression q, Path context);
    protected abstract T itrNaryRelationalExpression(NaryRelationalExpression q, Path context);
    protected abstract T itrArrayContainsExpression(ArrayContainsExpression q, Path context);
    protected abstract T itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context);
    protected abstract T itrNaryLogicalExpression(NaryLogicalExpression q, Path context);
    protected abstract T itrArrayMatchExpression(ArrayMatchExpression q, Path context);

    /**
     * Recursively iterates the nodes of the query. The field names are
     * interpreted relative to the given context
     */
    public T iterate(QueryExpression q, Path context) {
        if (q instanceof ValueComparisonExpression) {
            return itrValueComparisonExpression((ValueComparisonExpression) q, context);
        } else if (q instanceof FieldComparisonExpression) {
            return itrFieldComparisonExpression((FieldComparisonExpression) q, context);
        } else if (q instanceof RegexMatchExpression) {
            return itrRegexMatchExpression((RegexMatchExpression) q, context);
        } else if (q instanceof NaryRelationalExpression) {
            return itrNaryRelationalExpression((NaryRelationalExpression) q, context);
        } else if (q instanceof UnaryLogicalExpression) {
            return itrUnaryLogicalExpression((UnaryLogicalExpression) q, context);
        } else if (q instanceof NaryLogicalExpression) {
            return itrNaryLogicalExpression((NaryLogicalExpression) q, context);
        } else if (q instanceof ArrayContainsExpression) {
            return itrArrayContainsExpression((ArrayContainsExpression) q, context);
        } else if (q instanceof ArrayMatchExpression) {
            return itrArrayMatchExpression((ArrayMatchExpression) q, context);
        } else {
            // q instanceof AllMatchExpression
            return itrAllMatchExpression((AllMatchExpression) q, context);
        }
    }

    /**
     * Recursively iterates the nodes of the query.
     */
    public T iterate(QueryExpression q) {
        return iterate(q, Path.EMPTY);
    }
}
