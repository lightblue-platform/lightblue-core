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

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.Path;

public class ArrayRemoveByQueryExpression extends ArrayUpdateExpression {
    private QueryExpression query;

    public ArrayRemoveByQueryExpression() {
    }

    public ArrayRemoveByQueryExpression(Path field, QueryExpression q) {
        super(field);
        this.query = q;
    }

    @Override
    public UpdateOperator getOp() {
        return UpdateOperator._remove;
    }

    public QueryExpression getQuery() {
        return query;
    }

    public void setQuery(QueryExpression q) {
        query = q;
    }

    @Override
    protected JsonNode jsonValue() {
        return query.toJson();
    }

    public static ArrayRemoveByQueryExpression fromJson(Path field, JsonNode value) {
        return new ArrayRemoveByQueryExpression(field, QueryExpression.fromJson(value));
    }
}
