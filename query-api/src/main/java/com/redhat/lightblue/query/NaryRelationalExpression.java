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
 * Represents a query of the form
 * <pre>
 * nary_relational_expression := { field: <field>,  
 *                                op: nary_comparison_operator,  
 *                                values: value_list_array }  
 * </pre>
 */
public class NaryRelationalExpression extends RelationalExpression {

    private Path field;
    private NaryRelationalOperator op;
    private List<Value> values;
    
    /**
     * Default ctor
     */
    public NaryRelationalExpression() {}

    /**
     * Ctor with the given values
     */
    public NaryRelationalExpression(Path field,
                                    NaryRelationalOperator op,
                                    List<Value> values) {
        this.field=field;
        this.op=op;
        this.values=values;
    }

    /**
     * Ctor with the given values
     */
    public NaryRelationalExpression(Path field,
                                    NaryRelationalOperator op,
                                    Value... v) {
        this(field,op,Arrays.asList(v));
    }

    /**
     * The field. If this is a nested query, the field is relative to the context
     */
    public Path getField() {
        return this.field;
    }

    /**
     * The field. If this is a nested query, the field is relative to the context
     */
    public void setField(Path argField) {
        this.field = argField;
    }

    /**
     * The operator
     */
    public NaryRelationalOperator getOp() {
        return this.op;
    }

    /**
     * The operator
     */
    public void setOp(NaryRelationalOperator argOp) {
        this.op = argOp;
    }

    /**
     * List of values against which to compare the field
     */
    public List<Value> getValues() {
        return values;
    }

    /**
     * List of values against which to compare the field
     */
    public void setValues(List<Value> v) {
        this.values=v;
    }

    /**
     * Returns a json representation of this query
     */
    public JsonNode toJson() {
        ArrayNode arr=getFactory().arrayNode();
        for(Value x:values) {
            arr.add(x.toJson());
        }
        return getFactory().objectNode().put("field",field.toString()).
            put("op",op.toString()).
            set("values",arr);
    }

    /**
     * Parses an n-ary relational expression from the given json object
     */
    public static NaryRelationalExpression fromJson(ObjectNode node) {
        if(node.size()==3) {
            JsonNode x=node.get("op");
            if(x!=null) {
                NaryRelationalOperator op=
                    NaryRelationalOperator.fromString(x.asText());
                if(op!=null) {
                    x=node.get("field");
                    if(x!=null) {
                        Path field=new Path(x.asText());
                        x=node.get("values");
                        if(x instanceof ArrayNode) {
                            ArrayList<Value> values=new ArrayList<Value>(((ArrayNode)x).size());
                            for(Iterator<JsonNode> itr=((ArrayNode)x).elements();
                                itr.hasNext();) {
                                values.add(Value.fromJson(itr.next()));
                            }
                            return new NaryRelationalExpression(field,op,values);
                        }
                    }
                }
            }
        }
        throw Error.get(INVALID_COMPARISON_EXPRESSION,node.toString());
    }
}
