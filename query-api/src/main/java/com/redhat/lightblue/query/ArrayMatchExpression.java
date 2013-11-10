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
import com.redhat.lightblue.util.Error;

public class ArrayMatchExpression extends ArrayComparisonExpression {
    private Path array;
    private QueryExpression elemMatch;

    public ArrayMatchExpression() {}

    public ArrayMatchExpression(Path array,
                                QueryExpression elemMatch) {
        this.array=array;
        this.elemMatch=elemMatch;
    }

    public Path getArray() {
        return this.array;
    }

    public void setArray(Path argArray) {
        this.array = argArray;
    }

    public QueryExpression getElemMatch() {
        return this.elemMatch;
    }

    public void setElemMatch(QueryExpression argElemMatch) {
        this.elemMatch = argElemMatch;
    }

    public JsonNode toJson() {
        return factory.objectNode().put("array",array.toString()).
            put("elemMatch",elemMatch.toJson());
    }

    public static ArrayMatchExpression fromJson(ObjectNode node) {
        JsonNode x=node.get("array");
        if(x!=null) {
            Path field=new Path(x.asText());
            x=node.get("elemMatch");
            if(x!=null) {
                return new ArrayMatchExpression(field,QueryExpression.fromJson(x));
            }
        }
        throw Error.get(INVALID_ARRAY_COMPARISON_EXPRESSION,node.toString());
    }
}
