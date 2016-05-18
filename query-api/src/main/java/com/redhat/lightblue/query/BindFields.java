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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public class BindFields<T extends FieldBinding> extends QueryIterator {
    protected final List<T> bindingResult;
    protected final Set<Path> bindRequest;

    public BindFields(List<T> result,
                      Set<Path> request) {
        this.bindingResult = result;
        this.bindRequest = request;
    }

    /**
     * Override this method to create a subclass of valueBinding if necessary
     */
    protected T newValueBinding(Path field, Path ctx, BoundValue value, QueryExpression originalQuery, QueryExpression newQuery) {
        return (T) new ValueBinding(field, value, originalQuery, newQuery);
    }

    /**
     * Override this method to create a subclass of ListBinding if necessary
     */
    protected T newListBinding(Path field, Path ctx, BoundValueList value, QueryExpression originalQuery, QueryExpression newQuery) {
        return (T) new ListBinding(field, value, originalQuery, newQuery);
    }

    protected QueryExpression checkError(QueryExpression q, Path field, Path ctx) {
        if (bindRequest.contains(new Path(ctx, field))) {
            throw Error.get(QueryConstants.ERR_INVALID_VALUE_BINDING, q.toString());
        }
        return q;
    }

    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path ctx) {
        return checkError(q, q.getArray(), ctx);
    }

    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path ctx) {
        return checkError(q, q.getField(), ctx);
    }

    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path ctx) {
        return checkError(q, q.getField(), ctx);
    }

    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path ctx) {
        return checkError(q, q.getField(), ctx);
    }

    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path ctx) {
        QueryExpression newq = q;
        Path l = new Path(ctx, q.getField());
        Path r = new Path(ctx, q.getRfield());
        boolean bindl = bindRequest.contains(l);
        boolean bindr = bindRequest.contains(r);
        if (bindl && bindr) {
            throw Error.get(QueryConstants.ERR_INVALID_VALUE_BINDING, q.toString());
        }
        if (bindl || bindr) {
            // If we're here, only one of the fields is bound
            if (bindr) {
                BoundValueList newValue = new BoundValueList();
                newq = new NaryValueRelationalExpression(q.getField(), q.getOp(), newValue);
                bindingResult.add(newListBinding(r, ctx, newValue, q, newq));
            } else {
                BoundValue newValue = new BoundValue();
                List<Value> list = new ArrayList<>(1);
                list.add(newValue);
                newq = new ArrayContainsExpression(q.getRfield(), q.getOp() == NaryRelationalOperator._in
                        ? ContainsOperator._all : ContainsOperator._none,
                        list);
                bindingResult.add(newValueBinding(l, ctx, newValue, q, newq));
            }
        }
        return newq;
    }

    @Override
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path ctx) {
        checkError(q, q.getArray(), ctx);
        return super.itrArrayMatchExpression(q, ctx);
    }

    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path ctx) {
        QueryExpression newq = q;
        Path l = new Path(ctx, q.getField());
        Path r = new Path(ctx, q.getRfield());
        boolean bindl = bindRequest.contains(l);
        boolean bindr = bindRequest.contains(r);
        if (bindl && bindr) {
            throw Error.get(QueryConstants.ERR_INVALID_VALUE_BINDING, q.toString());
        }
        if (bindl || bindr) {
            // If we're here, only one of the fields is bound
            BoundValue newValue = new BoundValue();
            if (bindr) {
                newq = new ValueComparisonExpression(q.getField(), q.getOp(), newValue);
                bindingResult.add(newValueBinding(r, ctx, newValue, q, newq));
            } else {
                newq = new ValueComparisonExpression(q.getRfield(), q.getOp().invert(), newValue);
                bindingResult.add(newValueBinding(l, ctx, newValue, q, newq));
            }
        }
        return newq;
    }

    public static <T extends FieldBinding> QueryExpression bind(QueryExpression q,
                                                                List<T> bindingResult,
                                                                Set<Path> bindRequest) {
        return bind(q, Path.EMPTY, bindingResult, bindRequest);
    }

    public static <T extends FieldBinding> QueryExpression bind(QueryExpression q,
                                                                Path ctx,
                                                                List<T> bindingResult,
                                                                Set<Path> bindRequest) {
        return new BindFields<>(bindingResult, bindRequest).iterate(q, ctx);
    }
}
