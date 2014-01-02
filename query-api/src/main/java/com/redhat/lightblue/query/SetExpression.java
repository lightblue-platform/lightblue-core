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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;

public class SetExpression extends FieldUpdateExpression {
    private UpdateOperator op;
    private List<FieldValue> values;

    public SetExpression() {
    }

    public SetExpression(UpdateOperator op) {
        setOp(op);
    }

    public SetExpression(UpdateOperator op,
            List<FieldValue> values) {
        setOp(op);
        this.values = values;
    }

    public SetExpression(UpdateOperator op,
            FieldValue... i) {
        this(op, Arrays.asList(i));
    }

    public UpdateOperator getOp() {
        return op;
    }

    public final void setOp(UpdateOperator op) {
        if (op != null) {
            if (op == UpdateOperator._set
                    || op == UpdateOperator._add
                    || op == UpdateOperator._setOnInsert) {
                this.op = op;
            } else {
                throw new IllegalArgumentException(op.toString());
            }
        } else {
            this.op = null;
        }
    }

    public List<FieldValue> getValues() {
        return values;
    }

    public void setValues(List<FieldValue> l) {
        values = l;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node = getFactory().objectNode();
        ObjectNode child = getFactory().objectNode();
        for (FieldValue x : values) {
            child.put(x.getField().toString(), x.getValue().toJson());
        }
        node.put(op.toString(), child);
        return node;
    }

    public static SetExpression fromJson(UpdateOperator op, ObjectNode node) {
        ArrayList<FieldValue> list = new ArrayList<>(node.size());
        for (Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
                fields.hasNext();) {
            Map.Entry<String, JsonNode> entry = fields.next();
            list.add(new FieldValue(new Path(entry.getKey()),
                    Value.fromJson(entry.getValue())));
        }
        return new SetExpression(op, list);
    }
}
