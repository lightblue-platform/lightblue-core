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
package com.redhat.lightblue.eval;

import java.util.List;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Type;

import com.redhat.lightblue.query.ArrayContainsExpression;
import com.redhat.lightblue.query.ContainsOperator;
import com.redhat.lightblue.query.Value;

/**
 * Initialize the class with the corresponding expression, metadata,
 * and the context path.  If this is a nested query, the context path
 * determines the field from which the query needs to be evaluated.
 */
public class ArrayContainsEvaluator extends QueryEvaluator {
    private final ArrayContainsExpression expr;
    private final SimpleArrayElement elem;

    public ArrayContainsEvaluator(ArrayContainsExpression expr,
                                  FieldTreeNode context) {
        this.expr=expr;
        FieldTreeNode node=context.resolve(expr.getArray());
        if(node==null)
            throw new EvaluationError(expr);
        if(node instanceof SimpleArrayElement) {
            elem=(SimpleArrayElement)node;
        } else
            throw new EvaluationError(expr,"Expected simple array");
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        boolean ret=false;
        JsonNode node=JsonDoc.get(ctx.getCurrentContextNode(),expr.getArray());
        if(node!=null) 
            if(node instanceof ArrayNode) {
                ArrayNode array=(ArrayNode)node;
                List<Value> values=expr.getValues();
                ContainsOperator op=expr.getOp();
                Type t=elem.getType();
                int numElementsContained=0;
                for(Value value:values)
                    for(Iterator<JsonNode> itr=array.elements();itr.hasNext();) {
                        JsonNode valueNode=itr.next();
                        Object v=value.getValue();
                        if(valueNode==null||valueNode instanceof NullNode) {
                            if(v==null)
                                numElementsContained++;
                        } else {
                            if(v!=null) {
                                if(elem.getType().compare(v,t.fromJson(valueNode))==0)
                                    numElementsContained++;
                            }
                        }
                    }
                switch(op) {
                case _any: ret=numElementsContained>0;break;
                case _all: ret=numElementsContained==array.size();break;
                case _none: ret=numElementsContained==0;break;
                }
            }
        ctx.setResult(ret);
        return ret;
    }
}

