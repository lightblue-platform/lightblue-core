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
package com.redhat.lightblue.crud;

import com.redhat.lightblue.crud.SetExpression;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;

public abstract class PartialUpdateExpression extends UpdateExpression {

    public static final String ERR_INVALID_UPDATE_EXPRESSION = "INVALID_UPDATE_EXPRESSION";
    public static final String ERR_INVALID_OPERATOR = "INVALID_OPERATOR";

    public static PartialUpdateExpression fromJson(ObjectNode node) {
        // Expect one property with update operator as the property name
        if (node.size() == 1) {
            String fld = node.fieldNames().next();
            UpdateOperator op = UpdateOperator.fromString(fld);
            if (op == null) {
                throw Error.get(ERR_INVALID_OPERATOR, fld);
            }
            JsonNode value = node.get(fld);
            switch (op) {
                case _set:
                case _add:
                case _setOnInsert:
                    return SetExpression.fromJson(op, (ObjectNode) value);
                case _unset:
                    return UnsetExpression.fromJson(value);
                case _pop:
                    return ArrayUpdateExpression.fromJson(op, (ObjectNode) value);
            }
        }
        throw Error.get(ERR_INVALID_UPDATE_EXPRESSION, node.toString());
    }
}
