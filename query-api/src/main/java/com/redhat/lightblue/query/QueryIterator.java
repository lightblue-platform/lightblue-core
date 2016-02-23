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

import com.redhat.lightblue.util.CopyOnWriteIterator;
import com.redhat.lightblue.util.Path;

/**
 * Traverses the query nodes in a recursive descend manner, optionally replacing
 * query nodes as it goes through them. If a query node is replaced, all its
 * ancestors are also replaced. The implementations of this iterator are
 * expected to override the base functionality defined in the method bodies. For
 * value comparison, field comparison, regex match, n-nary relational, and array
 * contains expressions the default behavior is to simply return the original
 * query. For unary, n-ary logical operations, and array match the default
 * behavior recursively descends into the child nodes and creates new instances
 * of the query clauses if child nodes are different than the originals.
 */
public abstract class QueryIterator extends QueryIteratorSkeleton<QueryExpression> {

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrAllMatchExpression(AllMatchExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to return <code>q</code>. Override the method if more
     * processing is needed. Return a new QueryExpression object if this clause
     * is to be modified.
     */
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        return q;
    }

    /**
     * Default behavior is to recursively iterate the nested query. If nested
     * processing returns an object different from the original nested query,
     * this method creates a new unary logical expression using the new query
     * expression, and returns that.
     */
    protected QueryExpression itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        QueryExpression newq = iterate(q.getQuery(), context);
        if (newq != q.getQuery()) {
            return new UnaryLogicalExpression(q.getOp(), newq);
        } else {
            return q;
        }
    }

    /**
     * Default behavior is to recursively iterate the nested quereies. If nested
     * processing returns objects different from the original nested queries,
     * this method creates a new n-ary logical expression using the new query
     * expressions and returns that.
     */
    protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        CopyOnWriteIterator<QueryExpression> itr = new CopyOnWriteIterator<>(q.getQueries());
        while (itr.hasNext()) {
            QueryExpression nestedq = itr.next();
            QueryExpression newq = iterate(nestedq, context);
            if (newq != nestedq) {
                itr.set(newq);
            }
        }
        if (itr.isCopied()) {
            return new NaryLogicalExpression(q.getOp(), itr.getCopiedList());
        } else {
            return q;
        }
    }

    /**
     * Default behavior is to recursively iterate the nested query. If nested
     * processing returns an object different from the original nested query,
     * this method creates a new array match expression using the new query
     * expression, and returns that.
     */
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        QueryExpression newq = iterate(q.getElemMatch(), new Path(new Path(context, q.getArray()), Path.ANYPATH));
        if (newq != q.getElemMatch()) {
            return new ArrayMatchExpression(q.getArray(), newq);
        } else {
            return q;
        }
    }
}
