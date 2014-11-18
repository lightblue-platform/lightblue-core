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

import com.redhat.lightblue.util.Path;

public class ArrayQueryMatchProjection extends ArrayProjection {

    private static final long serialVersionUID = 1L;

    private final QueryExpression match;

    public ArrayQueryMatchProjection(Path field,
                                     boolean include,
                                     Projection project,
                                     Sort sort,
                                     QueryExpression match) {
        super(field, include, project,sort);
        this.match = match;
    }

    public ArrayQueryMatchProjection(Path field,
                                     boolean include,
                                     Projection project,
                                     QueryExpression match) {
        this(field, include, project,null,match);
    }

    public QueryExpression getMatch() {
        return this.match;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node = (ObjectNode) super.toJson();
        node.set("match", match.toJson());
        return node;
    }
}
