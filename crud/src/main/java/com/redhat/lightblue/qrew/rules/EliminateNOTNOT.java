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
package com.redhat.lightblue.qrew.rules;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.UnaryLogicalExpression;
import com.redhat.lightblue.query.UnaryLogicalOperator;

import com.redhat.lightblue.qrew.Rewriter;

/**
 * If 
 * <pre>
 *   q={$not:{$not:{x}]}
 * </pre>
 * this rewrites q as
 * <pre>
 *   q=x
 * </pre>
 */
public class EliminateNOTNOT extends Rewriter {

    public static final Rewriter INSTANCE=new EliminateNOTNOT();

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        UnaryLogicalExpression le=dyncast(UnaryLogicalExpression.class,q);
        if(le!=null&&le.getOp()==UnaryLogicalOperator._not) {
            UnaryLogicalExpression oreq=dyncast(UnaryLogicalExpression.class,le.getQuery());
            if(oreq!=null&&oreq.getOp()==UnaryLogicalOperator._not) {
                return oreq.getQuery();
            }
        }
        return q;
    }
}
