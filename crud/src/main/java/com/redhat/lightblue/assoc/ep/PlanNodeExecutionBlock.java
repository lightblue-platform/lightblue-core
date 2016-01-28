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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.assoc.QueryPlanNode;

/**
 * An execution block that's associated with a query plan node
 */
public class PlanNodeExecutionBlock extends ExecutionBlock {

    private final QueryPlanNode qpNode;
    
    public PlanNodeExecutionBlock(ExecutionPlan plan,QueryPlanNode qpNode) {
        super(plan);
        this.qpNode=qpNode;
    }

    /**
     * Returns the entity metadata for this block. 
     */
    public CompositeMetadata getEntityMetadata() {
        return qpNode.getMetadata();        
    }

    /**
     * Returns if this block belongs to the root entity
     */
    public boolean isRootEntity() {
        return qpNode.getMetadata().getParent()==null;
    }

    public JsonNode toJson() {
        ObjectNode node=JsonNodeFactory.instance.objectNode();
        node.set("name",JsonNodeFactory.instance.textNode(qpNode.getName()));
        return super.toJson(node);
    }

    @Override
    public String toString() {
        return qpNode.getName()+":"+super.toString();
    }
}
