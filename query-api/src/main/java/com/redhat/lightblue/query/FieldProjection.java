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

import com.redhat.lightblue.util.Path;

public class FieldProjection extends BasicProjection {

	private static final long serialVersionUID = 1L;
	
    public static final FieldProjection ALL = new FieldProjection(Path.ANYPATH, true, true);

    private final Path field;
    private boolean include = true;
    private boolean recursive = false;

    public FieldProjection(Path field,
                           boolean include,
                           boolean recursive) {
        this.field = field;
        this.include = include;
        this.recursive = recursive;
    }

    public Path getField() {
        return this.field;
    }

    public boolean isInclude() {
        return this.include;
    }

    public boolean isRecursive() {
        return this.recursive;
    }

    @Override
    public JsonNode toJson() {
        return getFactory().objectNode().
                put("field", field.toString()).
                put("include", include).
                put("recursive", recursive);
    }
}
