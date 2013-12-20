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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;

public class UnsetExpression extends FieldUpdateExpression {
    private List<Path> fields;

    public UnsetExpression() {}

    public UnsetExpression(List<Path> fields) {
        this.fields=fields;
    }

    public UnsetExpression(Path... i) {
        this(Arrays.asList(i));
    }

    public UpdateOperator getOp() {
        return UpdateOperator._unset;
    }

    public List<Path> getFields() {
        return fields;
    }

    public void setFields(List<Path> l) {
        fields=l;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=factory.objectNode();
        JsonNode child;
        if(fields.size()==1) {
            child=factory.textNode(fields.get(0).toString());
        } else {
            child=factory.arrayNode();
            for(Path x:fields) {
                ((ArrayNode)child).add(factory.textNode(x.toString()));
            }
        }
        node.put(getOp().toString(),child);
        return node;
    }

    public static UnsetExpression fromJson(JsonNode node) {
        List<Path> list=new ArrayList<Path>();
        if(node instanceof ArrayNode) {
            for(Iterator<JsonNode> itr=((ArrayNode)node).elements();
                itr.hasNext();) {
                list.add(new Path(itr.next().asText()));
            }
        } else {
            list.add(new Path(node.asText()));
        }
        return new UnsetExpression(list);
    }
}
