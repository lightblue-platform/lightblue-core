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

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Base class for update expressions
 * <pre>
 * update_expression := partial_update_expression | [ partial_update_expression,...]  
 * </pre>
 */
public abstract class PartialUpdateExpression extends UpdateExpression {
    
    /**
     * Parses a partial update expression using the given json object
     */
    public static PartialUpdateExpression fromJson(ObjectNode node) {
        if(node.has(UpdateOperator._append.toString())||
           node.has(UpdateOperator._insert.toString())||
           node.has(UpdateOperator._foreach.toString())) {
            return ArrayUpdateExpression.fromJson(node);
        }  else {
            return PrimitiveUpdateExpression.fromJson(node);
        }
    }
}
