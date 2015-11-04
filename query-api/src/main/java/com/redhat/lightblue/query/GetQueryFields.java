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

import com.redhat.lightblue.util.Path;

/**
 * Query iterator that collects field information from a query
 * expression. A FieldInfo object is returned for every field the
 * query. For array elemMatch queries, the fields from the nested
 * expressions are returned along with their context, and absolute
 * field names. For instance:
 * <pre>
 *  { array:a, elemMatch: {field:x, op:$eq, rvalue:1}}
 * </pre>
 * For this query, a fieldInfo is returned for 'a.*.x', but not 'a'.
 */
public class GetQueryFields extends QueryIterator {
    private List<FieldInfo> fields;
    
    public GetQueryFields(List<FieldInfo> fields) {
        this.fields = fields;
    }

    /**
     * Override this method to create a subclass of FieldInfo if necessary
     */
    protected FieldInfo newFieldInfo(Path clauseField,Path ctx,QueryExpression clause) {
        return new FieldInfo(ctx.isEmpty()?clauseField:new Path(ctx,clauseField),ctx,clause);
    }
    
    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path ctx) {
        fields.add(newFieldInfo(q.getArray(), ctx, q));
        return q;
    }
    
    @Override
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path ctx) {
        // Array match expression does not have the array itself as a field.
        // All the fields references in the nested expression are the fields used by this expression
        return super.itrArrayMatchExpression(q,ctx);
    }
    
    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path ctx) {
        fields.add(newFieldInfo(q.getField(), ctx, q));
        fields.add(newFieldInfo(q.getRfield(), ctx, q));
        return q;
    }
    
    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path ctx) {
        fields.add(newFieldInfo(q.getField(), ctx, q));
        return q;
    }
    
    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path ctx) {
        fields.add(newFieldInfo(q.getField(), ctx, q));
        fields.add(newFieldInfo(q.getRfield(), ctx, q));
        return q;
    }
    
    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path ctx) {
        fields.add(newFieldInfo(q.getField(), ctx, q));
        return q;
    }
    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path ctx) {
        fields.add(newFieldInfo(q.getField(), ctx, q));
        return q;
    }

    /**
     * Returns field information about the query
     */
    public static List<FieldInfo> getQueryFields(QueryExpression q) {
        List<FieldInfo> list = new ArrayList<FieldInfo>(16);
        getQueryFields(list,q);
        return list;
    }

    /**
     * Returns field information about the query
     *
     * @param fields The call adds the field information to this list
     */
    public static void getQueryFields(List<FieldInfo> fields,QueryExpression q) {
        getQueryFields(fields, q, Path.EMPTY);
    }

    /**
     * The implementation should populate the list with the field information
     */
    public static void getQueryFields(List<FieldInfo> fields, QueryExpression q,Path ctx) {
        new GetQueryFields(fields).iterate(q, ctx);
    }
    
}
