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
import java.util.ArrayList;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Type;

import com.redhat.lightblue.query.ArrayMatchExpression;

public class ArrayMatchEvaluator extends QueryEvaluator {
    private final Path field;
    private final QueryEvaluator ev;
    private final ObjectArrayElement elem;

    public ArrayMatchEvaluator(ArrayMatchExpression expr,
                               FieldTreeNode context) {
        // field needs to be resolved relative to the current context
        field=expr.getArray();
        FieldTreeNode node=context.resolve(field);
        if(node==null)
            throw new EvaluationError(expr);
        if(node instanceof ObjectArrayElement) {
            elem=(ObjectArrayElement)node;
            ev=QueryEvaluator.getInstance(expr.getElemMatch(),context);
        } else
            throw new EvaluationError(expr,"Expected object array for "+field);
    }

    @Override
    public boolean evaluate(QueryEvaluationContext ctx) {
        boolean ret=false;
        JsonNode node=JsonDoc.get(ctx.getCurrentContextNode(),field);
        if(node!=null) 
            if(node instanceof ArrayNode) {
                ArrayNode array=(ArrayNode)node;
                Path arrayPath=ctx.getCurrentContextPath();
                int index=0;
                ctx.push(node,field);
                for(Iterator<JsonNode> itr=array.elements();itr.hasNext();) {
                    JsonNode elem=itr.next();
                    if(index==0)
                        ctx.push(elem,index);
                    else
                        ctx.setLast(elem,index);
                    if(ev.evaluate(ctx)) {
                        ctx.addMatchingArrayElement(arrayPath,index);
                        ret=true;
                    }
                    index++;
                }
                if(index>0)
                    ctx.pop();
                ctx.pop();
            }
        ctx.setResult(ret);
        return ret;
    }
}

