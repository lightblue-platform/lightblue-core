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
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;

public class ArrayContainsExpression extends  ArrayComparisonExpression {
    private Path array;
    private ContainsOperator op;
    private List<Value> values;

    public ArrayContainsExpression() {}

    public ArrayContainsExpression(Path array,
                                   ContainsOperator op,
                                   List<Value> values) {
        this.array=array;
        this.op=op;
        this.values=values;
    }

    public ArrayContainsExpression(Path array,
                                   ContainsOperator op,
                                   Value... v) {
        this(array,op,Arrays.asList(v));
    }

    public Path getArray() {
        return this.array;
    }

    public void setArray(Path argArray) {
        this.array = argArray;
    }

    public ContainsOperator getOp() {
        return this.op;
    }

    public void setOp(ContainsOperator argOp) {
        this.op = argOp;
    }

    public List<Value> getValues() {
        return values;
    }

    public void setValues(List<Value> v) {
        this.values=v;
    }

    public JsonNode toJson() {
        ArrayNode arr=factory.arrayNode();
        for(Value x:values)
            arr.add(x.toJson());
        return factory.objectNode().
            put("array",array.toString()).
            put("contains",op.toString()).
            put("values",arr);
    }
}
