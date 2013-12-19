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

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Query of the form
 * <pre>
 * array_contains_expression := { array: <field>,  
 *                               contains: "$any" | "$all" | "$none",  
 *                               values: value_list_array }  
 * </pre>
 */
public class ArrayContainsExpression extends  ArrayComparisonExpression {
    private Path array;
    private ContainsOperator op;
    private List<Value> values;

    /**
     * Default ctor
     */
    public ArrayContainsExpression() {}

    /**
     * Ctor with the given values
     */
    public ArrayContainsExpression(Path array,
                                   ContainsOperator op,
                                   List<Value> values) {
        this.array=array;
        this.op=op;
        this.values=values;
    }

    /**
     * Ctor with multiple values
     *
     * @param array the array field
     * @param op Operator
     * @param v Values
     */
    public ArrayContainsExpression(Path array,
                                   ContainsOperator op,
                                   Value... v) {
        this(array,op,Arrays.asList(v));
    }

    /**
     * The array field. If this is included in a nested query, relative to the context
     */
    public Path getArray() {
        return this.array;
    }

    /**
     * The array field. If this is included in a nested query, relative to the context
     */
    public void setArray(Path argArray) {
        this.array = argArray;
    }

    /**
     * Contains operator
     */
    public ContainsOperator getOp() {
        return this.op;
    }

    /**
     * Contains operator
     */
    public void setOp(ContainsOperator argOp) {
        this.op = argOp;
    }

    /**
     * The values
     */
    public List<Value> getValues() {
        return values;
    }

    /**
     * The values
     */
    public void setValues(List<Value> v) {
        this.values=v;
    }

    /**
     * Returns a json representation of the query
     */
    public JsonNode toJson() {
        ArrayNode arr=factory.arrayNode();
        for(Value x:values)
            arr.add(x.toJson());
        return factory.objectNode().
            put("array",array.toString()).
            put("contains",op.toString()).
            set("values",arr);
    }

    /**
     * Parses an ArrayContainsExpression from a JSON object node.
     */
    public static ArrayContainsExpression fromJson(ObjectNode node) {
        JsonNode x=node.get("array");
        if(x!=null) {
            Path field=new Path(x.asText());
            x=node.get("contains");
            if(x!=null) {
                ContainsOperator op=ContainsOperator.fromString(x.asText());
                if(op!=null) {
                    x=node.get("values");
                    if(x!=null&&x instanceof ArrayNode) {
                        ArrayList<Value> values=new ArrayList<Value>(((ArrayNode)x).size());
                        for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                            itr.hasNext();)
                            values.add(Value.fromJson(itr.next()));
                        return new ArrayContainsExpression(field,op,values);
                    }
                }
            }
        }
        throw Error.get(INVALID_ARRAY_COMPARISON_EXPRESSION,node.toString());
    }
}
