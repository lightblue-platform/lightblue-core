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

public abstract class ArrayProjection extends BasicProjection {

    private static final long serialVersionUID = 1L;

    private final Path field;
    private final boolean include;
    private final Projection project;
    private final Sort sort;

    public ArrayProjection(Path field,
                           boolean include,
                           Projection project,
                           Sort sort) {
        this.field = field;
        this.include = include;
        this.project = project == null ? FieldProjection.ALL : project;
        this.sort = sort;
    }

    public Path getField() {
        return this.field;
    }

    public boolean isInclude() {
        return this.include;
    }

    public Projection getProject() {
        return this.project;
    }

    public Sort getSort() {
        return sort;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node = getFactory().objectNode().
                put("field", field.toString()).
                put("include", include);
        node.set("projection", project.toJson());
        if (sort != null) {
            node.set("sort", sort.toJson());
        }
        return node;
    }

}
