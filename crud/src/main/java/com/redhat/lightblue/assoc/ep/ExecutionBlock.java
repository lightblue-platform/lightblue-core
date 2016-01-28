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
package com.redhat.lightblue.assoc.ep;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * An execution block is a node in the execution plan, and it is a
 * sequence of operations to be performed on the data.
 */
public abstract class ExecutionBlock {

    private final List<ExecutionStep> pipeline=new ArrayList<>();
    private final List<ExecutionBlock> childBlocks=new ArrayList<>();
    private final List<ExecutionBlock> parentBlocks=new ArrayList<>();
    private final ExecutionPlan ep;
    
    public ExecutionBlock(ExecutionPlan plan) {
        this.ep=plan;
    }

    /**
     * Add a new execution step to the end of the execution block
     */
    public void append(ExecutionStep step) {
        pipeline.add(step);
    }

    public List<ExecutionBlock> getChildBlocks() {
        return childBlocks;
    }

    /**
     * Add a new execution step after the given step
     *
     * @param newStep The step to add
     * @param after Add the new step after this one
     */
    public void addAfter(ExecutionStep newStep,ExecutionStep after) {
        int i=pipeline.indexOf(after);
        if(i==-1)
            append(newStep);
        else 
            pipeline.add(i+1,newStep);
    }

    /**
     * Adds a destination block to this block
     */
    public void addDestination(ExecutionBlock b) {
        childBlocks.add(b);
        b.parentBlocks.add(this);
    }

    public JsonNode toJson() {
        return toJson(JsonNodeFactory.instance.objectNode());
    }

    protected JsonNode toJson(ObjectNode node) {
        node.set("steps",JsonNodeFactory.instance.textNode(pipeline.toString()));
        if(!childBlocks.isEmpty()) {
            ArrayNode children=JsonNodeFactory.instance.arrayNode();
            node.set("children",children);
            for(ExecutionBlock child:childBlocks) {
                children.add(child.toJson());
            }
        }
        return node;
    }
    
    @Override
    public String toString() {
        return pipeline.toString();
    }
}
