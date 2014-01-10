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

import com.redhat.lightblue.crud.PartialUpdateExpression;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a list of partial update expressions
 */
public class UpdateExpressionList extends UpdateExpression {

    private List<PartialUpdateExpression> list;

    /**
     * Default ctor. Creates an empty expression list
     */
    public UpdateExpressionList() {
    }

    /**
     * Creates an expression list using the given list
     */
    public UpdateExpressionList(List<PartialUpdateExpression> items) {
        this.list = items;
    }
    
    /**
     * Creates an expression list using the given expressions
     */
    public UpdateExpressionList(PartialUpdateExpression... i) {
        this(Arrays.asList(i));
    }

    /**
     * Returns the update expressions
     */
    public List<PartialUpdateExpression> getList() {
        return list;
    }

    /**
     * Sets the update expressions
     */
    public void setList(List<PartialUpdateExpression> l) {
        list = l;
    }

    /**
     * Returns JSON representation of this object
     */
    @Override
    public JsonNode toJson() {
        ArrayNode arr = getFactory().arrayNode();
        for (PartialUpdateExpression x : list) {
            arr.add(x.toJson());
        }
        return arr;
    }

    /**
     * Parses an array JSON node and constructs an update expression list
     */
    public static UpdateExpressionList fromJson(ArrayNode node) {
        ArrayList<PartialUpdateExpression> list = new ArrayList<PartialUpdateExpression>(node.size());
        for (Iterator<JsonNode> itr = node.elements();
                itr.hasNext();) {
            list.add(PartialUpdateExpression.fromJson((ObjectNode) itr.next()));
        }
        return new UpdateExpressionList(list);
    }
}
