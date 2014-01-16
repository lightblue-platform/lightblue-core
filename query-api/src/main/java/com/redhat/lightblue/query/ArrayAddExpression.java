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
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Array append and insert operations
 * <pre>
 * array_update_expression := { $append : { path : rvalue_expression } } |  
 *                            { $append : { path : [ rvalue_expression, ... ] }} |  
 *                            { $insert : { path : rvalue_expression } } |  
 *                            { $insert : { path : [ rvalue_expression,...] }} 
 * </pre>
 */
public class ArrayAddExpression extends ArrayUpdateExpression {
    
    private final List<RValueExpression> values;
    private final UpdateOperator op;
    private final Path field;
    
    /**
     * Constructs an array update expression for insert and append operations
     */
    public ArrayAddExpression(Path field,UpdateOperator op,List<RValueExpression> list) {
        this.op=op;
        this.values=list;
        this.field=field;
    }

    /**
     * The array field to operate on
     */
    public Path getField() {
        return field;
    }

    /**
     * Values to be inserted or appended
     */
    public List<RValueExpression> getValues() {
        return values;
    }

    /**
     * The update operator
     */
    public UpdateOperator getOp() {
        return op;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=getFactory().objectNode();
        ObjectNode args=getFactory().objectNode();
        if(values.size()==1)
            args.set(field.toString(),values.get(0).toJson());
        else {
            ArrayNode arr=getFactory().arrayNode();
            for(RValueExpression v:values)
                arr.add(v.toJson());
            args.set(field.toString(),arr);
        }
        node.set(op.toString(),args);
        return node;
    }
    
    /**
     * Parses an array update expression using the given json object
     */
    public static ArrayAddExpression fromJson(ObjectNode node) {
        if(node.size()==1) {
            UpdateOperator op=UpdateOperator._append;
            JsonNode arg=node.get(UpdateOperator._append.toString());
            if(arg==null) {
                arg=node.get(UpdateOperator._insert.toString());
                op=UpdateOperator._insert;
            }
            if(arg instanceof ObjectNode) {
                ObjectNode objArg=(ObjectNode)arg;
                if(objArg.size()==1) {
                    Map.Entry<String,JsonNode> item=objArg.fields().next();
                    Path field=new Path(item.getKey());
                    JsonNode valueNode=item.getValue();
                    List<RValueExpression> rvalues=new ArrayList<RValueExpression>();
                    if(valueNode instanceof ArrayNode) {
                        for(Iterator<JsonNode> itr=((ArrayNode)valueNode).elements();itr.hasNext();)
                            rvalues.add(RValueExpression.fromJson(itr.next()));
                    } else {
                        rvalues.add(RValueExpression.fromJson(valueNode));
                    }
                    return new ArrayAddExpression(field,op,rvalues);
                }
            }
        }
        throw Error.get(ERR_INVALID_ARRAY_UPDATE_EXPRESSION,node.toString());
    }
}
