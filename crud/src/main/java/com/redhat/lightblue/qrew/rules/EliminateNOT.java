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
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.FieldComparisonExpression;
import com.redhat.lightblue.query.NaryRelationalExpression;

import com.redhat.lightblue.qrew.Rewriter;

/**
 * If 
 * <pre>
 *   q={$not:{w}}
 * </pre>
 * and w can be negatable, then this rewrites q as
 * <pre>
 *   q=negation of w
 * </pre>
 */
public class EliminateNOT extends Rewriter {

    public static final Rewriter INSTANCE=new EliminateNOT();

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        UnaryLogicalExpression le=dyncast(UnaryLogicalExpression.class,q);
        if(le!=null&&le.getOp()==UnaryLogicalOperator._not) {
	    ValueComparisonExpression vce=dyncast(ValueComparisonExpression.class,le.getQuery());
	    if(vce!=null) {
		return new ValueComparisonExpression(vce.getField(),vce.getOp().negate(),vce.getRvalue());
	    } else {
		FieldComparisonExpression fce=dyncast(FieldComparisonExpression.class,le.getQuery());
		if(fce!=null) {
		    return new FieldComparisonExpression(fce.getField(),fce.getOp().negate(),fce.getRfield());
		} else {
		    NaryRelationalExpression nre=dyncast(NaryRelationalExpression.class,le.getQuery());
		    if(nre!=null) {
			return new NaryRelationalExpression(nre.getField(),nre.getOp().negate(),nre.getValues());
		    }
		}
	    }
        }
        return q;
    }
}
