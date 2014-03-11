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
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Collections;

/**
 * Represents a list of partial update expressions
 * <pre>
 * update_expression := partial_update_expression | [ partial_update_expression,...]
 * </pre>
 */
public class UpdateExpressionList extends UpdateExpression {

    private static final long serialVersionUID = 1L;

    private final List<PartialUpdateExpression> list;

    /**
     * Constructs an update expression list using the given expression list
     */
    public UpdateExpressionList(List<PartialUpdateExpression> l) {
        list = l;
    }

    /**
     * Constructs an update expression list using the given expression list
     */
    public UpdateExpressionList(PartialUpdateExpression... l) {
        list = Arrays.asList(l);
    }

    /**
     * The list of update expressions
     */
    public List<PartialUpdateExpression> getList() {
        return list;
    }

    @Override
    public JsonNode toJson() {
        ArrayNode node = getFactory().arrayNode();
        for (PartialUpdateExpression x : list != null ? list : Collections.<PartialUpdateExpression>emptyList()) {
            if (x != null) {
                node.add(x.toJson());
            }
        }
        return node;
    }

    /**
     * Parses an update expression list using the given json object
     */
    public static UpdateExpressionList fromJson(ArrayNode node) {
        ArrayList<PartialUpdateExpression> list = new ArrayList<PartialUpdateExpression>(node.size());
        for (Iterator<JsonNode> itr = node.elements(); itr.hasNext();) {
            list.add(PartialUpdateExpression.fromJson((ObjectNode) itr.next()));
        }
        return new UpdateExpressionList(list);
    }
}
