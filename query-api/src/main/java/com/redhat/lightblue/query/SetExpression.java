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

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Expression to modify a field (set and add)
 * <pre>
 * primitive_update_expression := { $set : { path : rvalue_expression , ...} } |  
 *                                { $unset : path } |  
 *                                { $unset :[ path, ... ] }  
 *                                { $add : { path : rvalue_expression, ... } }  
 * </pre>
 */
public class SetExpression extends PrimitiveUpdateExpression {

    private static final long serialVersionUID = 1L;

    public static final String ERR_INVALID_SET_EXPRESSION="INVALID_SET_EXPRESSION";

    private final List<FieldAndRValue> fields;
    private UpdateOperator op;

    /**
     * Constructs a set expression using the given list
     */
    public SetExpression(UpdateOperator op,List<FieldAndRValue> list) {
        this.fields=list;
        if(op==UpdateOperator._set||op==UpdateOperator._add) {
            this.op=op;
        } else {
            throw new IllegalArgumentException("Operator:"+op);
        }
    }
    
    /**
     * Constructs a set expression using the given list
     */
    public SetExpression(UpdateOperator op,FieldAndRValue... l) {
        this.fields=Arrays.asList(l);
        if(op==UpdateOperator._set||op==UpdateOperator._add) {
            this.op=op;
        } else {
            throw new IllegalArgumentException("Operator:"+op);
        }
    }

    /**
     * Returns the fields to be updated, and their new values
     */
    public List<FieldAndRValue> getFields() {
        return fields;
    }

    /**
     * Returns the update operator
     */
    public UpdateOperator getOp() {
        return op;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=getFactory().objectNode();
        ObjectNode values=getFactory().objectNode();
        for(FieldAndRValue x:fields) {
            values.set(x.getField().toString(),
                       x.getRValue().toJson());
        }
        node.set(op.toString(),values);
        return node;
    }

    /**
     * Parses a set expression using the given json object
     */
    public static SetExpression fromJson(ObjectNode node) {
        if(node.size()==1) {
            UpdateOperator op=null;
            if(node.has(UpdateOperator._add.toString())) {
                op=UpdateOperator._add;
            } else if(node.has(UpdateOperator._set.toString())) {
                op=UpdateOperator._set;
            }
            if(op!=null) {
                ObjectNode arg=(ObjectNode)node.get(op.toString());
                List<FieldAndRValue> list=new ArrayList<FieldAndRValue>();
                for(Iterator<Map.Entry<String,JsonNode>> itr=arg.fields();itr.hasNext();) {
                    Map.Entry<String,JsonNode> entry=itr.next();
                    Path field=new Path(entry.getKey());
                    RValueExpression rvalue=RValueExpression.fromJson(entry.getValue());
                    list.add(new FieldAndRValue(field,rvalue));
                }
                return new SetExpression(op,list);
            }
        }
        throw Error.get(ERR_INVALID_SET_EXPRESSION,node.toString());
    }
}
