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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;
import com.redhat.lightblue.query.NaryRelationalExpression;
import com.redhat.lightblue.query.NaryRelationalOperator;
import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.qrew.Rewriter;

import com.redhat.lightblue.util.Path;

/**
 * If 
 * <pre>
 *   q={$or:{...,{$in:{field:x,values:[v]},..,{$in:{field:x,values=[w]}...}}
 * </pre>
 * this rewrites q as
 * <pre>
 *   q={$or:{...,{$in:{field:x,values:[v w]}},...}}
 * </pre>
 */
abstract class CombineInsNotIns extends Rewriter {

    private static final Logger LOGGER=LoggerFactory.getLogger(CombineInsNotIns.class);

    private NaryLogicalOperator logicalOp;
    private NaryRelationalOperator relationalOp;

    protected CombineInsNotIns(NaryLogicalOperator logicalOp,
                               NaryRelationalOperator relationalOp) {
        this.logicalOp=logicalOp;
        this.relationalOp=relationalOp;
    }

    @Override
    public QueryExpression rewrite(QueryExpression q) {
        NaryLogicalExpression le=dyncast(NaryLogicalExpression.class,q);
        if(le!=null) {
            if(le.getOp()==logicalOp) {
                LOGGER.debug("Processing q={}",le);
                // group in and not_in expressions
                boolean needCombine=false;
                Map<Path,List<NaryRelationalExpression>> map=new HashMap<>();
                for(QueryExpression x:le.getQueries()) {
                    NaryRelationalExpression nre=dyncast(NaryRelationalExpression.class,x);
                    if(nre!=null&&nre.getOp()==relationalOp) {
                        List<NaryRelationalExpression> values=map.get(nre.getField());
                        if(values==null)
                            map.put(nre.getField(),values=new ArrayList<>());
                        else 
                            needCombine=true; // There exists more than one N-ary expression=, so combine
                        values.add(nre);
                    }
                }
                LOGGER.debug("Grouped expressions={}",map);
                if(needCombine) {
                    LOGGER.debug("Query expressions can be combined");
                    List<QueryExpression> newList=new ArrayList<>(le.getQueries().size());
                    for(Map.Entry<Path,List<NaryRelationalExpression>> entry:map.entrySet()) {
                        if(entry.getValue().size()>1) {
                            // Combine expressions
                            Set<Value> valueList=new HashSet<Value>();
                            for(NaryRelationalExpression x:entry.getValue())
                                valueList.addAll(x.getValues());
                            newList.add(new NaryRelationalExpression(entry.getKey(),
                                                                     relationalOp,
                                                                     new ArrayList<Value>(valueList)));
                        } else {
                            newList.addAll(entry.getValue());
                        }
                    }
                    // Add all the expressions that are not n-ary relational expressions
                    for(QueryExpression x:le.getQueries())
                        if(x instanceof NaryRelationalExpression) {
                            if( ((NaryRelationalExpression)x).getOp()!=relationalOp)
                                newList.add(x);
                        } else
                            newList.add(x);
                    LOGGER.debug("Combined expression list={}",newList);
                    return new NaryLogicalExpression(logicalOp,newList);
                }
            }
        }
        return q;
    }
}
