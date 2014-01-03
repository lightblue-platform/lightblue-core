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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a list of projections
 */
public class ProjectionList extends Projection {

    private List<Projection> items;

    /**
     * Ctor with the given list
     */
    public ProjectionList(List<Projection> items) {
        this.items = items;
    }

    /**
     * Returns the nested projections
     */
    public List<Projection> getItems() {
        return items;
    }

    @Override
    public JsonNode toJson() {
        ArrayNode arr = getFactory().arrayNode();
        for (Projection x : items) {
            arr.add(x.toJson());
        }
        return arr;
    }

    public static ProjectionList fromJson(ArrayNode node) {
        ArrayList<Projection> list = new ArrayList<Projection>(node.size());
        for (Iterator<JsonNode> itr = node.elements();
                itr.hasNext();) {
            list.add(BasicProjection.fromJson((ObjectNode) itr.next()));
        }
        return new ProjectionList(list);
    }
}
