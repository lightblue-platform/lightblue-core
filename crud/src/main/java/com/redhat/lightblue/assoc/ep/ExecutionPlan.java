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
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.RewriteQuery;
import com.redhat.lightblue.assoc.QueryPlanData;

import com.redhat.lightblue.util.JsonUtils;

/**
 * Execution plan is a tree of execution blocks. Every node in the
 * query plan is converted to an execution block, and connected in the
 * same way query plan nodes are connected. Each execution block
 * contains a pipeline, and every step of the pipeline performs a step
 * of the operation.
 */
public class ExecutionPlan {

    private final CompositeMetadata rootMd;

    private ExecutionBlock rootBlock;
    
    private int parallelism=1;

    /**
     * Creates an execution plan
     *
     * @param requestProjection projection requested by the client
     * @param requestSort sort requested by the client.
     * @param from request.from
     * @param to request.to
     * @param rootMd Root entity composite metadata
     * @param searchQueryPlan if the results of a search is to be
     * retrieved in a second pass, not null. Otherwise, null.
     * @param retrievalQueryPlan Never null. Contains the plan for the
     * retrieval of found documents. If the searchQueryPlan is not
     * null, retrieves the documents found by that search. If
     * searchQueryPlan is null, this plan performs the search and
     * retrieval.
     */
    public ExecutionPlan(Projection requestProjection,
                         Sort requestSort,
                         Long from,
                         Long to,
                         CompositeMetadata rootMd,
                         QueryPlan searchQueryPlan,
                         QueryPlan retrievalQueryPlan) {
        this.rootMd=rootMd;
        // either both searchQueryPlan and retrievalQueryPlan is nonnull, in which case
        // we append retrievalQueryPlan to searchQueryPlan and create an execution plan
        // from those, or searchQueryPlan is null, and there is only
        // retrievalQueryPlan
        QueryPlanNode[] searchQPNodes=searchQueryPlan==null?null:searchQueryPlan.getAllNodes();
        QueryPlanNode[] retrievalQPNodes=retrievalQueryPlan.getAllNodes();

        // Create an execution block for each query plan node
        PlanNodeExecutionBlock searchRoot=null;
        // if(searchQPNodes!=null) {
            
        //     // First, create a block for each query plan node
        //     for(QueryPlanNode qpNode:searchQPNodes) {
        //         PlanNodeExecutionBlock block=new PlanNodeExecutionBlock(this,qpNode);
        //         qpNode.setProperty(PlanNodeExecutionBlock.class, block);
        //         if(block.isRootEntity())
        //             searchRoot=block;
        //     }

        //     for(QueryPlanNode qpNode:searchQPNodes) {
        //         PlanNodeExecutionBlock block=qpNode.getProperty(PlanNodeExecutionBlock.class);
        //         RewriteQuery rewriter=new RewriteQuery(rootMd,qpNode.getMetadata());
                
        //         QueryPlanNode[] sources=qpNode.getSources();
        //         if(sources.length>0) {
        //             List<QueryPlanNode> children=new ArrayList<>(sources.length);
        //             QueryPlanNode parent=splitParentAndChildren(qpNode,sources,children);
        //             //   sources: array of all source nodes
        //             //   parent: Pointer to a source node that is the parent
        //             //   entity if this node, can be null
        //             //   children: List of nodes that are child entities of this node,
        //             //   can be empty
                    
        //             // A source node that is a parent entity is the
        //             // straight forward solution. We execute this search
        //             // node for every document retrieved for the parent
        //             // entity. For doc in parent node, there can be
        //             // multiple child docs, but for every child doc in
        //             // this node, there is only one parent.
                    
        //             // A source node that is a child entity is more
        //             // involved. A document in this node can be associated
        //             // with multiple documents in the source node, because
        //             // those docs are child docs.
        //             block.addStep(new JoinStep(parent,children));
                    



            

            
        //         SearchStep searchStep=block.getStep(SearchStep.class);
        //         searchStep.setProjection(FieldProjection.ALL);
        //        } 
        //     }
        // }
        // for(QueryPlanNode qpNode:retrievalQPNodes) {
        //     if(qpNode.getMetadata().getParent()==null) {
        //         // This is the root entity node
        //         // If searchRoot is nonnull, then
        //         // we have to get the results of that block, limit and
        //         // sort them, and continue building rest of the
        //         // execution plan rooted at a search and retrieve block
        //         if(searchRoot!=null) {
        //             // Sort and limit the result set
        //             List<Conjunct> clauses=searchQueryPlan.getUnassignedClauses();
        //             if(!clauses.isEmpty()) {
        //                 searchRoot.append(new FilterStep(clauses));
        //             }
        //             if(requestSort!=null) {
        //                 searchRoot.append(new SortStep(requestSort));
        //             }
        //             if(from!=null||to!=null) {
        //                 searchRoot.append(new LimitStep(from,to));
        //             }
        //             // Embed this search plan into search and retrieve block
        //             rootBlock=new SearchAndRetrieveBlock(this,qpNode,searchRoot);
        //             qpNode.setProperty(PlanNodeExecutionBlock.class,(PlanNodeExecutionBlock)rootBlock);
        //         } else {
        //             // There was no search plan, initialize 
        //             rootBlock=initializeBlock(qpNode);
        //             // Since this is the root block, we can sort and limit at the search step
        //             SearchStep search=rootBlock.getStep(SearchStep.class);
        //             search.setSort(requestSort);
        //             search.setLimit(from,to);
        //         }
        //     } else {
        //         initializeBlock(qpNode);
        //     }
        // }

        // // All the nodes of the execution plan are created. Now we process the edges

        // // If there is a search plan:
        // //
        // // Search plan includes only those nodes that are required to find 
        // // the root-level objects. So, execution plan creates the same
        // // node layout of the search plan
        // if(searchQPNodes!=null) {
        //     for(QueryPlanNode qpNode:searchQPNodes) {
        //         PlanNodeExecutionBlock block=qpNode.getProperty(PlanNodeExecutionBlock.class);
        //         SearchStep searchStep=block.getStep(SearchStep.class);
        //         searchStep.setProjection(FieldProjection.ALL);

        //         QueryPlanNode[] sources=qpNode.getSources();                
        //         if(sources.length==0) {
        //             // This is a source node
        //             // No edge queries

        //         } else {
                    
        //             List<QueryPlanNode> children=new ArrayList<>(sources.length);
        //             QueryPlanNode parent=splitParentAndChildren(qpNode,sources,children);
                    
        //             // Here:
        //             //   sources: array of all source nodes
        //             //   parent: Pointer to a source node that is the parent
        //             //   entity if this node, can be null
        //             //   children: List of nodes that are child entities of this node,
        //             //   can be empty
                    
        //             // A source node that is a parent entity is the
        //             // straight forward solution. We execute this search
        //             // node for every document retrieved for the parent
        //             // entity. For doc in parent node, there can be
        //             // multiple child docs, but for every child doc in
        //             // this node, there is only one parent.
                    
        //             // A source node that is a child entity is more
        //             // involved. A document in this node can be associated
        //             // with multiple documents in the source node, because
        //             // those docs are child docs.
                    
        //             // Since there are some parent nodes, we first join, and compute tuples
        //             block.addBefore(new JoinStep(parent,children),searchStep);

        //             // If there are child documents in the parent block, then
        //             // we have to associate the documents obtained from the search step
        //             // with the documents in the parent block
        //             if(!children.isEmpty()) {
        //                 block.addAfter(new AssociateStep(),searchStep);
        //             }
                    
        //             for(QueryPlanNode source:sources) {
        //                 PlanNodeExecutionBlock sourceBlock=source.getProperty(PlanNodeExecutionBlock.class);
        //                 block.addSourceBlock(sourceBlock);
        //                 // Move edge queries into the destination block
        //                 QueryPlanData edgeData=searchQueryPlan.getEdgeData(source,qpNode);
        //                 if(edgeData!=null) {
        //                     addQueries(edgeData.getConjuncts(),searchStep,
        //                                new RewriteQuery(rootMd,qpNode.getMetadata()));
        //                 }
        //             }
        //         }
        //     }
        // }
        
        
        // // We process edges of the retrieval plan
        // for(QueryPlanNode qpNode:retrievalQPNodes) {
        //     PlanNodeExecutionBlock block=qpNode.getProperty(PlanNodeExecutionBlock.class);
        //     SearchStep searchStep=block.getStep(SearchStep.class);
        //     searchStep.setProjection(FieldProjection.ALL);
            
        //     QueryPlanNode[] sources=qpNode.getSources();
        //     if(sources.length>0) {
        //         List<QueryPlanNode> children=new ArrayList<>(sources.length);
        //         QueryPlanNode parent=splitParentAndChildren(qpNode,sources,children);
        //         block.addBefore(new JoinStep(parent,children),searchStep);
                
        //         if(!children.isEmpty()) {
        //             block.addAfter(new AssociateStep(),searchStep);
        //         }
        //         for(QueryPlanNode source:sources) {
        //             PlanNodeExecutionBlock sourceBlock=source.getProperty(PlanNodeExecutionBlock.class);
        //             block.addSourceBlock(sourceBlock);
        //             // Move edge queries into the destination block
        //             QueryPlanData edgeData=retrievalQueryPlan.getEdgeData(source,qpNode);
        //             if(edgeData!=null) {
        //                 addQueries(edgeData.getConjuncts(),searchStep,
        //                            new RewriteQuery(rootMd,qpNode.getMetadata()));
        //             }
        //         }
        //     }
        // }
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

    
    private void addQueries(List<Conjunct> conjuncts,SearchStep search,RewriteQuery rewriter) {
        if(conjuncts!=null) {
            for(Conjunct q:conjuncts) {
                RewriteQuery.RewriteQueryResult result=rewriter.rewriteQuery(q.getClause(),
                                                                             q.getFieldInfo());
                search.addQueryClause(q,result.query,result.bindings);
            }
        }
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
    private QueryPlanNode splitParentAndChildren(QueryPlanNode qpNode,
                                                 QueryPlanNode[] sources,
                                                 List<QueryPlanNode> children) {
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
        rootNode.set("root",rootBlock.toJson());
        return rootNode;
    }
    
    @Override
    public String toString() {
        return JsonUtils.prettyPrint(toJson());
    }
}

