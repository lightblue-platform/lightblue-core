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

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.assoc.qrew.Rewriter;

import com.redhat.lightblue.util.Path;

/**
 * If 
 * <pre>
 *   q={array: X, elemMatch: { x op value } or { x op y } }
 * </pre>
 * this rewrites q as
 * <pre>
 *   q={ field: X.*.x op value | y }
 * </pre>
 */
public class SimpleElemMatchIsComparison extends Rewriter {

    public static final SimpleElemMatchIsComparison INSTANCE=new SimpleElemMatchIsComparison();
    
    @Override
    public QueryExpression rewrite(QueryExpression q) {
        ArrayMatchExpression ae=dyncast(ArrayMatchExpression.class, q);
        QueryExpression newq=q;
        if(ae!=null) {
            QueryExpression nestedq=ae.getElemMatch();
            ValueComparisonExpression vce;
            FieldComparisonExpression fce;
            NaryValueRelationalExpression nvre;
            NaryFieldRelationalExpression nfre;
            if( (vce=dyncast(ValueComparisonExpression.class,nestedq))!=null) {
                newq=new ValueComparisonExpression(normalize(ae,vce.getField()),vce.getOp(),vce.getRvalue());
            } else if( (fce=dyncast(FieldComparisonExpression.class,nestedq))!=null) {
                newq=new FieldComparisonExpression(normalize(ae,fce.getField()),fce.getOp(),normalize(ae,fce.getRfield()));
            } else if( (nvre=dyncast(NaryValueRelationalExpression.class,nestedq))!=null) {
                newq=new NaryValueRelationalExpression(normalize(ae,nvre.getField()),nvre.getOp(),nvre.getValues());
            } else if( (nfre=dyncast(NaryFieldRelationalExpression.class,nestedq))!=null) {
                newq=new NaryFieldRelationalExpression(normalize(ae,nfre.getField()),nfre.getOp(),nfre.getRfield());
            }
        }
        return newq;
    }

    private Path normalize(ArrayMatchExpression ae,Path field) {
        Path p=new Path(new Path(ae.getArray(),Path.ANYPATH),field);
        return p.normalize();
    }
}
