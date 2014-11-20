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
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Represents a sort key of the form:
 * <pre>
 * { fieldName: $asc} or { fieldName: $desc }
 * </pre>
 */
public class SortKey extends Sort {
    private static final long serialVersionUID = 1L;

    private final Path field;
    private final boolean desc;

    /**
     * Constructs a sort key with the values
     */
    public SortKey(Path field, boolean desc) {
        this.field = field;
        this.desc = desc;
    }

    /**
     * Returns the sort field
     */
    public Path getField() {
        return this.field;
    }

    /**
     * Returns if the sort is descending
     */
    public boolean isDesc() {
        return this.desc;
    }

    @Override
    public JsonNode toJson() {
        return getFactory().objectNode().put(field.toString(), desc ? "$desc" : "$asc");
    }

    public static SortKey fromJson(ObjectNode node) {
        if (node.size() != 1) {
            throw Error.get(QueryConstants.ERR_INVALID_SORT, node.toString());
        }
        String fieldString = node.fieldNames().next();
        String dir = node.get(fieldString).asText();
        Path field = new Path(fieldString);
        boolean desc = false;
        switch (dir) {
            case "$asc":
                desc = false;
                break;
            case "$desc":
                desc = true;
                break;
            default:
                throw Error.get(QueryConstants.ERR_INVALID_SORT, node.toString());
        }
        return new SortKey(field, desc);
    }
}
