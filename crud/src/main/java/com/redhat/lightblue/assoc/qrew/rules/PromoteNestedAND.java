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
package com.redhat.lightblue.assoc.qrew.rules;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.assoc.qrew.Rewriter;

import com.redhat.lightblue.util.CopyOnWriteIterator;

/**
 * If
 * <pre>
 *   q={$and:{q1,q2,...,{$and:{x1,x2...}},...,qn}}
 * </pre> this rewrites q as
 * <pre>
 *   q={$and:{q1,q2,...,qn,x1,x2,...}}
 * </pre>
 */
public class PromoteNestedAND extends Rewriter {

    public static final Rewriter INSTANCE = new PromoteNestedAND();

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        NaryLogicalExpression le = dyncast(NaryLogicalExpression.class, q);
        if (le != null) {
            if (le.getOp() == NaryLogicalOperator._and) {
                CopyOnWriteIterator<QueryExpression> itr = new CopyOnWriteIterator<>(le.getQueries());
                while (itr.hasNext()) {
                    QueryExpression x = itr.next();
                    NaryLogicalExpression nested = dyncast(NaryLogicalExpression.class, x);
                    if (nested != null && nested.getOp() == NaryLogicalOperator._and) {
                        // Remove this element, and add all its queries to the parent query
                        itr.remove();
                        itr.getCopiedList().addAll(nested.getQueries());
                    }
                }
                if (itr.isCopied()) {
                    return new NaryLogicalExpression(NaryLogicalOperator._and, itr.getCopiedList());
                }
            }
        }
        return q;
    }
}
