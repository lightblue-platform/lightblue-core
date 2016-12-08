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
package com.redhat.lightblue.assoc;

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;

/**
 * Creates a copy of a bound query containing the bound query values
 *
 * The caller prepares a list of Binder objects containing the field bindings,
 * and their corresponding values. For each query value, this class finds the
 * fieldBinding.value that is used in a clause, and replaces that clause with
 * the new value.
 *
 */
public class BindQuery extends QueryIterator {

    private final List<Binder> bindings;

    public BindQuery(List<Binder> bindings) {
        this.bindings = bindings;
    }

    public static BindQuery combine(List<BindQuery> binders) {
        List<Binder> allb = new ArrayList<>();
        for (BindQuery b : binders) {
            allb.addAll(b.bindings);
        }
        return new BindQuery(allb);
    }

    private Binder getBoundValue(Object v) {
        for (Binder binding : bindings) {
            if (binding.getBinding() == v) {
                return binding;
            }
        }
        return null;
    }

    public List<Binder> getBindings() {
        return bindings;
    }

    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        Binder binding = getBoundValue(q.getRvalue());
        if (binding != null) {
            if(binding.getValue() instanceof List) {
                // If field = [v1,v2,v3], then rewrite the query as:
                //     field in [v1,v2,v3]
                // otherwise if query is field op [v1,v2,v3], then:
                //    $or:[
                //           { field op v1},
                //           { field op v2},
                //           { field op v3 } ]
                
                if(q.getOp()==BinaryComparisonOperator._eq) {                    
                    return new NaryValueRelationalExpression(q.getField(),NaryRelationalOperator._in,(List<Value>)binding.getValue());
                } else {
                    List<QueryExpression> resultList=new ArrayList<>();
                    for(Value v:(List<Value>)binding.getValue()) {
                        resultList.add(new ValueComparisonExpression(q.getField(),q.getOp(),v));
                    }
                    return new NaryLogicalExpression(NaryLogicalOperator._or,resultList);
                }
            } else {
                return new ValueComparisonExpression(q.getField(), q.getOp(), (Value) binding.getValue());
            }
        } else {
            return q;
        }
    }

    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        Binder binding = getBoundValue(q.getValues());
        if (binding != null) {
            return new NaryValueRelationalExpression(q.getField(), q.getOp(), (List<Value>) binding.getValue());
        } else {
            return q;
        }
    }

    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        Binder binding = getBoundValue(q.getValues());
        if (binding != null) {
            return new ArrayContainsExpression(q.getArray(), q.getOp(), (List<Value>) binding.getValue());
        } else {
            return q;
        }
    }

}
