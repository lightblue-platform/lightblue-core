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

/**
 * Base class for all relational expression
 */
public abstract class RelationalExpression extends ComparisonExpression {

    /**
     * Parses a relational expression using the given object node
     */
    public static RelationalExpression fromJson(ObjectNode node) {
        JsonNode x=node.get("regex");
        if(x!=null)
            return RegexMatchExpression.fromJson(node);
        else {
            x=node.get("op");
            if(x!=null) {
                String op=x.asText();
                if(BinaryComparisonOperator.fromString(op)!=null) {
                    return BinaryRelationalExpression.fromJson(node);
                } else if(NaryRelationalOperator.fromString(op)!=null) {
                    return NaryRelationalExpression.fromJson(node);
                }
            }
        }
        throw Error.get(INVALID_COMPARISON_EXPRESSION,node.toString());
    }
}
