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
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

public abstract class ArrayUpdateExpression extends PartialUpdateExpression {
    private Path field;

    public ArrayUpdateExpression() {}

    public ArrayUpdateExpression(Path field) {
        this.field=field;
    }

    public abstract UpdateOperator getOp();

    public Path getField() {
        return field;
    }

    public void setPath(Path p) {
        field=p;
    }

    public JsonNode toJson() {
        ObjectNode node=factory.objectNode();
        ObjectNode child=factory.objectNode();
        child.put(field.toString(),jsonValue());
        node.put(getOp().toString(),child);
        return node;
    }

    protected abstract JsonNode jsonValue();

    public static ArrayUpdateExpression fromJson(UpdateOperator op,ObjectNode node) {
        if(node.size()==1) {
            String fld=node.fieldNames().next();
            JsonNode value=node.get(fld);
            Path field=new Path(fld);
            switch(op) {
            case _pop:
                return ArrayPopExpression.fromJson(field,value);
            case _remove:
                if(value instanceof ArrayNode)
                    return ArrayRemoveValuesExpression.fromJson(field,(ArrayNode)value);
                else
                    return ArrayRemoveByQueryExpression.fromJson(field,value);
            case _push:
                return ArrayPushExpression.fromJson(field,value);
            }
        }
        throw Error.get(ERR_INVALID_UPDATE_EXPRESSION,node.toString());
    }
}
