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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.BinaryComparisonOperator;
import com.redhat.lightblue.query.ValueComparisonExpression;
import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.assoc.qrew.Rewriter;

import com.redhat.lightblue.util.Path;

/**
 * If 
 * <pre>
 *   q={$or:{...,{$in:{field:x,values:[v]},..,{field:x,op:=,rvalue:w}...}}
 * </pre>
 * this rewrites q as
 * <pre>
 *   q={$or:{...,{$in:{field:x,values:[v, w]}},...}}
 * </pre>
 */
class ExtendRelationalInLogical extends Rewriter {

    private final NaryLogicalOperator logicalOp;
    private final BinaryComparisonOperator binaryOp;
    private final NaryRelationalOperator relationalOp;

    protected ExtendRelationalInLogical(NaryLogicalOperator logicalOp,
                                        BinaryComparisonOperator binaryOp,
                                        NaryRelationalOperator relationalOp) {
        this.logicalOp=logicalOp;
        this.binaryOp=binaryOp;
        this.relationalOp=relationalOp;
    }

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        NaryLogicalExpression le=dyncast(NaryLogicalExpression.class,q);
        if(le!=null) {
            if(le.getOp()==logicalOp) {
                // Get all in expressions in a list, keep a modified flag
                boolean modified=false;
                List<NaryRelationalExpression> inList=new ArrayList<>();
                List<QueryExpression> deletedValues=new ArrayList<>();
                for(QueryExpression iq:le.getQueries()) {
                    NaryRelationalExpression inexp=dyncast(NaryRelationalExpression.class,iq);
                    if(inexp!=null&&inexp.getOp()==relationalOp) {
                        boolean inexpModified=false;
                        // See if there are any value expressions with the same field and operator
                        for(QueryExpression vq:le.getQueries()) {
                            ValueComparisonExpression vce=dyncast(ValueComparisonExpression.class,vq);
                            if(vce!=null) {
                                if(vce.getField().equals(inexp.getField())&&vce.getOp()==binaryOp) {
                                    // Add this value to the in expression
                                    if(!deletedValues.contains(vce)) {
                                        if(!inexpModified) {
                                            inexpModified=true;
                                            inexp=new NaryRelationalExpression(inexp.getField(),
                                                                               inexp.getOp(),
                                                                               new ArrayList<Value>(inexp.getValues()));
                                        }
                                        boolean found=false;
                                        for(Value x:inexp.getValues())
                                            if(x.equals(vce.getRvalue())) {
                                                found=true;
                                                break;
                                            }
                                        if(!found)
                                            inexp.getValues().add(vce.getRvalue());
                                        inList.add(inexp);
                                        deletedValues.add(vce);
                                        deletedValues.add(inexp);
                                    }
                                }
                            }
                        }
                        if(inexpModified)
                            modified=true;
                    }
                }
                if(modified) {
                    List<QueryExpression> newList=new ArrayList<QueryExpression>();
                    for(QueryExpression ip:le.getQueries())
                        if(!deletedValues.contains(ip))
                            newList.add(ip);
                    newList.addAll(inList);
                    return new NaryLogicalExpression(logicalOp,newList);
                }
            }
        }
        return q;
    }
}
