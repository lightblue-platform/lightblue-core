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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Path;

public class ArrayRangeProjection extends ArrayProjection {

    private static final long serialVersionUID = 1L;

    private final int from;
    private final int to;

    public ArrayRangeProjection(Path field,
                                boolean include,
                                Projection project,
                                Sort sort,
                                int from,
                                int to) {
        super(field, include, project,sort);
        this.from = from;
        this.to = to;
    }

    public ArrayRangeProjection(Path field,
                                boolean include,
                                Projection project,
                                int from,
                                int to) {
        this(field, include, project,null,from,to);
    }

    public int getFrom() {
        return this.from;
    }

    public int getTo() {
        return this.to;
    }

    @Override
    public JsonNode toJson() {
        ArrayNode arr = getFactory().arrayNode();
        arr.add(getFactory().numberNode(from)).
                add(getFactory().numberNode(to));
        return ((ObjectNode) super.toJson()).set("range", arr);
    }
}
