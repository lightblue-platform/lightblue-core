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

import com.redhat.lightblue.assoc.qrew.Rewriter;

/**
 * If 
 * <pre>
 *   q={$or:{x}} or q={$and:{x}}
 * </pre>
 * this rewrites q as
 * <pre>
 *   q={x} 
 * </pre>
 */
public class EliminateSingleANDOR extends Rewriter {

    public static final Rewriter INSTANCE=new EliminateSingleANDOR();

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        NaryLogicalExpression le=dyncast(NaryLogicalExpression.class,q);
        if(le!=null) 
            if(le.getQueries().size()==1)
                return le.getQueries().get(0);
        return q;
    }
}
