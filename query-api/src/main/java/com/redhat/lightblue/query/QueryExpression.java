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
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonObject;

public abstract class QueryExpression extends JsonObject {

    public static final String INVALID_QUERY="INVALID_QUERY";

    public static QueryExpression fromJson(JsonNode node) {
        if(node instanceof ObjectNode) {
            ObjectNode onode=(ObjectNode)node;
            // If there is only one field, then that field must be a
            // logical operator
            String firstField=onode.fieldNames().next();
            if(UnaryLogicalOperator.fromString(firstField)!=null)
                return UnaryLogicalExpression.fromJson(onode);
            else if(NaryLogicalOperator.fromString(firstField)!=null)
                return NaryLogicalExpression.fromJson(onode);
            else
                return ComparisonExpression.fromJson(onode);
        } else
            throw Error.get(INVALID_QUERY,node.toString());
    }
}
