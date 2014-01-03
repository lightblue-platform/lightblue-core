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

import com.redhat.lightblue.crud.ArrayUpdateExpression;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.redhat.lightblue.query.UpdateOperator;
import com.redhat.lightblue.query.UpdateOperator;
import com.redhat.lightblue.query.Value;
import com.redhat.lightblue.query.Value;

import com.redhat.lightblue.util.Path;

public class ArrayPushExpression extends ArrayUpdateExpression {
    private List<Value> values;

    public ArrayPushExpression() {
    }

    public ArrayPushExpression(Path field, List<Value> values) {
        super(field);
        this.values = values;
    }

    public ArrayPushExpression(Path field,
                               Value... l) {
        this(field, Arrays.asList(l));
    }

    @Override
    public UpdateOperator getOp() {
        return UpdateOperator._push;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> l) {
        values = l;
    }

    @Override
    protected JsonNode jsonValue() {
        ArrayNode node = getFactory().arrayNode();
        for (Value x : values) {
            node.add(x.toJson());
        }
        return node;
    }

    public static ArrayPushExpression fromJson(Path field, JsonNode node) {
        ArrayList<Value> list = new ArrayList<>();
        if (node instanceof ArrayNode) {
            for (Iterator<JsonNode> itr = ((ArrayNode) node).elements();
                    itr.hasNext();) {
                list.add(Value.fromJson(itr.next()));
            }
        } else {
            list.add(Value.fromJson(node));
        }
        return new ArrayPushExpression(field, list);
    }
}
