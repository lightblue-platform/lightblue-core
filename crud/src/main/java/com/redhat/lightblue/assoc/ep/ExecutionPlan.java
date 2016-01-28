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

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;

import com.redhat.lightblue.util.JsonUtils;

/**
 * Execution plan is a tree of execution blocks. Every node in the
 * query plan is converted to an execution block, and connected in the
 * same way query plan nodes are connected. Each execution block
 * contains a pipeline, and every step of the pipeline performs a step
 * of the operation.
 */
public class ExecutionPlan {

    private final ForkBlock root;    
    private int parallelism=1;
    
    public ExecutionPlan(QueryExpression requestQuery,
                         Projection requestProjection,
                         Sort requestSort,
                         CompositeMetadata rootMd,
                         QueryPlan searchQueryPlan,
                         QueryPlan retrievalQueryPlan) {
        // either both searchQueryPlan and retrievalQueryPlan is nonnull, in which case
        // we append retrievalQueryPlan to searchQueryPlan and create an execution plan
        // from those, or searchQueryPlan is null, and there is only
        // retrievalQueryPlan
        QueryPlanNode[] searchQPNodes=searchQueryPlan==null?null:searchQueryPlan.getAllNodes();
        QueryPlanNode[] retrievalQPNodes=retrievalQueryPlan==null?null:retrievalQueryPlan.getAllNodes();

        // Create an execution block for each query plan node
        if(searchQPNodes!=null) {
            for(QueryPlanNode qpNode:searchQPNodes) {
                qpNode.setProperty(PlanNodeExecutionBlock.class, new PlanNodeExecutionBlock(this,qpNode));
            }
        }
        if(retrievalQPNodes!=null) {
            for(QueryPlanNode qpNode:retrievalQPNodes) {
                qpNode.setProperty(PlanNodeExecutionBlock.class, new PlanNodeExecutionBlock(this,qpNode));
            }
        }

        // We want to have a single root for simplicity, so we start with a fork
        root=new ForkBlock(this);
        
        // We process edges of the query plan, and connect execution blocks based on
        // how the entities are defined in the metadata, and how they are ordered in the
        // query plan
        PlanNodeExecutionBlock rootEntityBlock=null;
        if(searchQPNodes!=null) {
            rootEntityBlock=buildExecutionPlan(searchQPNodes,root);
        }
        if(retrievalQPNodes!=null) {
            if(rootEntityBlock!=null) {
                ForkBlock secondaryFork=new ForkBlock(this);
                rootEntityBlock.addDestination(secondaryFork);
                buildExecutionPlan(retrievalQPNodes,secondaryFork);
            } else {
                buildExecutionPlan(retrievalQPNodes,root);
            }
        }                
    }

    /**
     * Set maximum number of threads that can run in parallel. There's a hard limit on 10
     */
    public void setParallelism(int n) {
        parallelism=n;
        if(parallelism<1)
            parallelism=1;
        if(parallelism>10)
            parallelism=10;
    }

    private PlanNodeExecutionBlock buildExecutionPlan(QueryPlanNode[] qpNodes,ForkBlock rootFork) {
        PlanNodeExecutionBlock rootEntityBlock=null;
        for(QueryPlanNode qpNode:qpNodes) {               
            PlanNodeExecutionBlock block=qpNode.getProperty(PlanNodeExecutionBlock.class);
            QueryPlanNode[] sources=qpNode.getSources();
            if(sources.length==0) {
                // This is a root node, assign it to the root fork
                rootFork.addDestination(block);
                
                // Create the search step for this block
                SearchStep search=new SearchStep();
                block.append(search);
                
            } else {
                List<QueryPlanNode> children=new ArrayList<>(sources.length);
                QueryPlanNode parent=splitParentAndChildren(qpNode,sources,children);
                
                // Here:
                //   sources: array of all source nodes
                //   parent: Pointer to a source node that is the parent entity if this node, can be null
                //   children: List of nodes that are child entities of this node, can be empty
                
                // A source node that is a parent entity is the
                // straight forward solution. We execute this search
                // node for every document retrieved for the parent
                // entity. For doc in parent node, there can be
                // multiple child docs, but for every child doc in
                // this node, there is only one parent.
                
                // A source node that is a child entity is more
                // involved. A document in this node can be associated
                // with multiple documents in the source node, because
                // those docs are child docs.
                
                if(sources.length>1) {
                    block.append(new MergeStep());
                }
                for(QueryPlanNode source:sources) {
                    source.getProperty(PlanNodeExecutionBlock.class).addDestination(block);
                }
                
                // Since there are some parent nodes, we first join, and compute tuples
                block.append(new JoinStep(parent,children));
                // Perform the search
                block.append(new SearchStep());
                // If there are child documents in the parent block, then
                // we have to associate the documents obtained from the search step
                // with the documents in the parent block
                if(!children.isEmpty()) {
                    block.append(new AssociateStep());
                }
                // Is this the root entity?  Then, we need to manually sort and limit the result set
                if(block.isRootEntity()) {
                    block.append(new SortStep());
                    block.append(new LimitStep());
                    // This is where we'll attach the retrieval plan
                    rootEntityBlock=block;
                }
            }
        }
        return rootEntityBlock;
    }
    
    /**
     * Split the parent entity and child entities in sources
     *
     * @param qpNode This node
     * @param sources The source query plan nodes
     * @param children The list that will receive the child nodes
     *
     * @return The parent node, or null if there isn't a parent node
     */
    private QueryPlanNode splitParentAndChildren(QueryPlanNode qpNode,QueryPlanNode[] sources,List<QueryPlanNode> children) {
        QueryPlanNode parentNode=null;
        for(int i=0;i<sources.length;i++) {
            if(qpNode.getMetadata().getParent()==sources[i].getMetadata()) {
                // Found the parent node
                parentNode=sources[i];
                for(int j=0;j<sources.length;j++) {
                    if(j!=i)
                        children.add(sources[i]);
                }
            }
        }
        if(parentNode==null) {
            for(QueryPlanNode node:sources)
                children.add(node);
        }
        return parentNode;
    }

    public JsonNode toJson() {
        ObjectNode rootNode=JsonNodeFactory.instance.objectNode();
        rootNode.set("root",root.toJson());
        return rootNode;
    }
    
    @Override
    public String toString() {
        return JsonUtils.prettyPrint(toJson());
    }
}

