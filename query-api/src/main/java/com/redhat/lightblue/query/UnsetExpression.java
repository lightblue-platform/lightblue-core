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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * Expression to remove a field
 * <pre>
 * primitive_update_expression := { $set : { path : rvalue_expression , ...} } |  
 *                                { $unset : path } |  
 *                                { $unset :[ path, ... ] }  
 *                                { $add : { path : rvalue_expression, ... } }  
 * </pre>
 */
public class UnsetExpression extends PrimitiveUpdateExpression {

    private static final long serialVersionUID = 1L;
    
    public static final String ERR_INVALID_UNSET_EXPRESSION="INVALID_UNSET_EXPRESSION";

    private final List<Path> fields;

    /**
     * Constructs an unset expression using the given list
     */
    public UnsetExpression(List<Path> list) {
        this.fields=list;
    }
    
    /**
     * Returns the fields to be removed
     */
    public List<Path> getFields() {
        return fields;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node=getFactory().objectNode();
        ArrayNode fieldArr=getFactory().arrayNode();
        for(Path x:fields) {
            fieldArr.add(getFactory().textNode(x.toString()));
        }
        node.set(UpdateOperator._unset.toString(),fieldArr);
        return node;
    }

    /**
     * Parses an unset expression using the given json object
     */
    public static UnsetExpression fromJson(ObjectNode node) {
        if(node.size()==1) {
            JsonNode val=node.get(UpdateOperator._unset.toString());
            if(val!=null) {
                List<Path> fields=new ArrayList<Path>();
                if(val instanceof ArrayNode) {
                    for(Iterator<JsonNode> itr=((ArrayNode)val).elements();itr.hasNext();) {
                        fields.add(new Path(itr.next().asText()));
                    }
                } else if(val.isValueNode()) {
                    fields.add(new Path(val.asText()));
                }
                return new UnsetExpression(fields);
            }
        }
        throw Error.get(ERR_INVALID_UNSET_EXPRESSION,node.toString());
    }
}
