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
import java.util.Set;
import java.util.Collection;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

/**
 * Base class for all query expressions
 */
public abstract class QueryExpression extends JsonObject {
    private static final long serialVersionUID = 1L;

    private static final class BindableClausesItr extends QueryIterator {
        private List<QueryInContext> list;
        public BindableClausesItr(List<QueryInContext> l) {
            this.list=l;
        }

        @Override
        protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q,Path ctx) {
            list.add(new QueryInContext(ctx,q));
            return q;
        }
    }

    private static final class GetQueryFieldsItr extends QueryIterator {

        private List<FieldInfo> fields;

        public GetQueryFieldsItr(List<FieldInfo> fields) {
            this.fields=fields;
        }
    
        @Override
        protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q,Path ctx) {
            fields.add(new FieldInfo(new Path(ctx,q.getArray()),ctx,q));
            return q;
        }

        @Override
        protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q,Path ctx) {
            Path arrayPath=new Path(ctx,q.getArray());
            fields.add(new FieldInfo(arrayPath,ctx,q));
            return super.itrArrayMatchExpression(q,ctx);
        }

        @Override
        protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q,Path ctx) {
            fields.add(new FieldInfo(new Path(ctx,q.getField()),ctx,q));
            fields.add(new FieldInfo(new Path(ctx,q.getRfield()),ctx,q));
            return q;
        }
        
        @Override
        protected QueryExpression itrNaryRelationalExpression(NaryRelationalExpression q,Path ctx) {
            fields.add(new FieldInfo(new Path(ctx,q.getField()),ctx,q));
            return q;
        }

        @Override
        protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q,Path ctx) {
            fields.add(new FieldInfo(new Path(ctx,q.getField()),ctx,q));
            return q;
        }

        @Override
        protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q,Path ctx) {
            fields.add(new FieldInfo(new Path(ctx,q.getField()),ctx,q));
            return q;
        }
    }

    private static final class BindItr extends QueryIterator {
        private List<FieldBinding> bindingResult;
        private Set<Path> bindRequest;

        public BindItr(List<FieldBinding> bindingResult,
                       Set<Path> bindRequest) {
            this.bindingResult=bindingResult;
            this.bindRequest=bindRequest;
        }
        
        private QueryExpression checkError(QueryExpression q,Path field,Path ctx) {
            if(bindRequest.contains(new Path(ctx,field)))
                throw Error.get(QueryConstants.ERR_INVALID_VALUE_BINDING,q.toString());
            return q;
        }

        @Override
        protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q,Path ctx) {
            return checkError(q,q.getArray(),ctx);
        }
        
        @Override
        protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q,Path ctx) {
            return checkError(q,q.getField(),ctx);
        }

        @Override
        protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path ctx) {
            return checkError(q,q.getField(),ctx);
        }

        @Override
        protected QueryExpression itrNaryRelationalExpression(NaryRelationalExpression q, Path ctx) {
            return checkError(q,q.getField(),ctx);
        }

       @Override
        protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q,Path ctx) {
            checkError(q,q.getArray(),ctx);
            return super.itrArrayMatchExpression(q,ctx);
        }

        @Override
        protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q,Path ctx) {
            Path l=new Path(ctx,q.getField());
            Path r=new Path(ctx,q.getRfield());
            boolean bindl=bindRequest.contains(l);
            boolean bindr=bindRequest.contains(r);
            if(bindl&&bindr)
                throw Error.get(QueryConstants.ERR_INVALID_VALUE_BINDING,q.toString());
            if(!bindl&&!bindr)
                return q;
            // If we're here, only one of the fields is bound
            Path newf;
            Path boundf;
            BinaryComparisonOperator newop;
            BoundValue newValue=new BoundValue();
            if(bindr) {
                newf=q.getField();
                newop=q.getOp();
                boundf=r;
            } else {
                newf=q.getRfield();
                newop=q.getOp().invert();
                boundf=l;
            }
            QueryExpression newq=new ValueComparisonExpression(newf,newop,newValue);
            bindingResult.add(new FieldBinding(boundf,newValue,q,newq));
            return newq;
        }

    }


    /**
     * Returns field information about the query
     */
    public List<FieldInfo> getQueryFields() {
        List<FieldInfo> list=new ArrayList<FieldInfo>(16);
        getQueryFields(list);
        return list;
    }

    /**
     * Returns field information about the query
     *
     * @param fields The call adds the field information to this list
     */
    public void getQueryFields(List<FieldInfo> fields) {
        getQueryFields(fields,Path.EMPTY);
    }

    /**
     * The implementation should populate the list with the field information
     */
    public void getQueryFields(List<FieldInfo> fields,Path ctx) {
        new GetQueryFieldsItr(fields).iterate(this,ctx);
    }

    /**
     * Returns the query expressions that can be bound to a value
     */
    public List<QueryInContext> getBindableClauses() {
        List<QueryInContext> list=new ArrayList<>();
        getBindableClauses(list,Path.EMPTY);
        return list;
    }


    /**
     * Adds the query expressions that can be bound to a value to the given list
     */
    public void getBindableClauses(List<QueryInContext> list,Path ctx) {
        new BindableClausesItr(list).iterate(this,ctx);
    }

    public QueryExpression bind(List<FieldBinding> bindingResult,
                                Set<Path> bindRequest) {
        return bind(Path.EMPTY,bindingResult,bindRequest);
    }

    /**
     * Binds all the bindable fields in the bindRequest, populates the
     * bindingResult with binding information, and return a new
     * QueryExpression with bound values.
     *
     * @param ctx Context
     * @param bindingResult The results of the bindings will be added to this list
     * @param bindRequest Full paths to the fields to be bound. If
     * there are array elements, '*' must be used
     *
     * @return A new instance of the query object with bound
     * values. If there are no bindable values, the same query object
     * will be returned.
     */
    public QueryExpression bind(Path ctx,
                                List<FieldBinding> bindingResult,
                                Set<Path> bindRequest) {
        return new BindItr(bindingResult,bindRequest).iterate(this,ctx);
    }

    
    /**
     * Parses a query expression from the given json node
     */
    public static QueryExpression fromJson(JsonNode node) {
        if (node instanceof ObjectNode) {
            ObjectNode onode = (ObjectNode) node;
            // If there is only one field, then that field must be a
            // logical operator
            String firstField = onode.fieldNames().next();
            if (UnaryLogicalOperator.fromString(firstField) != null) {
                return UnaryLogicalExpression.fromJson(onode);
            } else if (NaryLogicalOperator.fromString(firstField) != null) {
                return NaryLogicalExpression.fromJson(onode);
            } else {
                return ComparisonExpression.fromJson(onode);
            }
        } else {
            throw Error.get(QueryConstants.ERR_INVALID_QUERY, node.toString());
        }
    }
}
