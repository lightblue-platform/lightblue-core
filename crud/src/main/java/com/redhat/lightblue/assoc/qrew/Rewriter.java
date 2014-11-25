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
package com.redhat.lightblue.assoc.qrew;

import com.redhat.lightblue.query.QueryExpression;

/**
 * This is the basic interface all query rewriters have to
 * implement. The implementation should inspect the query expression,
 * and return a rewritten version of the query if possible. The
 * implementation should treat the query passed into it as a read-only
 * object, not modifying it directly or indirectly. If there are any
 * modification to the query, a new query object must be returned.
 */
public abstract class Rewriter {
    
    public abstract QueryExpression rewrite(QueryExpression q);

    public static <T> T dyncast(Class<T> t, QueryExpression q) {
        if(t.isAssignableFrom(q.getClass()))
            return (T)q;
        else
            return null;
    }
}

