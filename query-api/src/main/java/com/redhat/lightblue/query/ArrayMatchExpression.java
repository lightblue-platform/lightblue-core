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

public class ArrayMatchExpression extends ArrayComparisonExpression {
    private Path array;
    private QueryExpression elemMatch;

    public final Path getArray() {
        return this.array;
    }

    public final void setArray(final Path argArray) {
        this.array = argArray;
    }

    public final QueryExpression getElemMatch() {
        return this.elemMatch;
    }

    public final void setElemMatch(final QueryExpression argElemMatch) {
        this.elemMatch = argElemMatch;
    }

    public JsonNode toJson() {
        return factory.objectNode().put("array",array.toString()).
            put("elemMatch",elemMatch.toJson());
    }
}
