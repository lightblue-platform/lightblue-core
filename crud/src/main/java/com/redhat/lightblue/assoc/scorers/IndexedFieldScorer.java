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
package com.redhat.lightblue.assoc.scorers;

import java.math.BigInteger;

import java.io.Serializable;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.Indexes;
import com.redhat.lightblue.metadata.Index;

import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanScorer;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryPlanChooser;
import com.redhat.lightblue.assoc.ResolvedFieldInfo;

import com.redhat.lightblue.util.Path;

/**
 * Query plan scoring based on search clauses on indexed fields. These are the heuristics it uses:
 *
 * <ul>
 * <li>No indexed queries on any node: MAX</li>
 * <li>We want nodes with indexed queries as close to the sources as possible</li>
 * <li>We want the root node to be the last indexed query node, if there is indexed queries for root node</li>
 * </ul>
 */
public class IndexedFieldScorer implements QueryPlanScorer, Serializable {

    private static final long serialVersionUID=1l;

    private static final Logger LOGGER=LoggerFactory.getLogger(IndexedFieldScorer.class);

    private CompositeMetadata cmd;

    private static class Score implements Comparable {

        private BigInteger cost;

        public Score(BigInteger cost) {
            this.cost=cost;
        }

        @Override
        public int compareTo(Object t) {
            if(t instanceof Score) {
                return cost.compareTo( ((Score)t).cost);
            } else
                throw new IllegalArgumentException("Expecting a score, got "+t);
        }
        
        public String toString() {
            return "cost:"+cost;
        }
    } 

    private static final class MaxScore extends Score {

        MaxScore() {
            super(null);
        }

        @Override
        public int compareTo(Object value) {
            return (value instanceof MaxScore)?0:1;
        }
    }

    public static final Comparable MAX=new MaxScore();


    @Override
    public QueryPlanData newDataInstance() {
        return new IndexedFieldScorerData();
    }

    private static class CostAndSize {
        BigInteger cost=BigInteger.ONE;
        BigInteger size=BigInteger.ONE;

        public String toString() {
            return "cost:"+cost+" size:"+size;
        }
    }

    @Override
    public Comparable score(QueryPlan qp) {
        LOGGER.debug("score begin");
        
        // Compute the cost of retrieval up to root node
        // We get root, and then go backwards from there
        QueryPlanNode[] nodes=qp.getAllNodes();
        QueryPlanNode root=null;
        for(int i=0;i<nodes.length;i++)
            if(nodes[i].getMetadata().getParent()==null) {
                root=nodes[i];
                break;
            }

        CostAndSize rootCost=getAncestorCostAndSize(root);
        LOGGER.debug("up to root: {}",rootCost);
        
        // Evaluation changes after root is retrieved. Any queury
        // evaluation won't affect the result set size, but add to the
        // cost, because that means we have to manually filter out
        // results

        BigInteger cost=BigInteger.ONE;
        for(QueryPlanNode dst:root.getDestinations()) {
            // If there's a query here, double the cost
            cost=cost.multiply(((IndexedFieldScorerData)dst.getData()).estimatedRootDescendantCost(rootCost.size));
        }
        cost=cost.multiply(rootCost.cost);
        LOGGER.debug("Final cost:{}",cost);
        return new Score(cost);

    }

    private CostAndSize getAncestorCostAndSize(QueryPlanNode anc) {
        CostAndSize incoming=null;
        for(QueryPlanNode src:anc.getSources()) {
            CostAndSize acs=getAncestorCostAndSize(src);
            
            if(incoming==null)
                incoming=new CostAndSize();
            
            // The input result set size is the join size of all incoming nodes
            incoming.size=incoming.size.multiply( acs.size );
            // Add up costs
            incoming.cost=incoming.cost.add( acs.cost );
        }
        CostAndSize cs=new CostAndSize();

        if(incoming!=null) {
            // We'll run this node incoming.size times
            cs.cost=incoming.size.multiply( ((IndexedFieldScorerData)anc.getData()).estimatedCost() );
            cs.size=incoming.size.multiply( ((IndexedFieldScorerData)anc.getData()).estimatedResultSize() );
        } else {
            cs.cost= ((IndexedFieldScorerData)anc.getData()).estimatedCost();
            cs.size= ((IndexedFieldScorerData)anc.getData()).estimatedResultSize();
        }
        return cs;
    }


    @Override
    public void reset(QueryPlanChooser c) {
        LOGGER.debug("reset");
        cmd=c.getMetadata();
        // Conjuncts associated with nodes will not move from one node to another
        // So we can measure the cost associated with them from the start
        for(QueryPlanNode node:c.getQueryPlan().getAllNodes()) {
            IndexedFieldScorerData data=(IndexedFieldScorerData)node.getData();
            data.setRootNode(node.getMetadata().getParent()==null);
            Set<Path> indexableFields=new HashSet<>();
            Indexes indexes=null;
            for(Conjunct cj:data.getConjuncts()) {
                List<ResolvedFieldInfo> cjFields=cj.getFieldInfo();
                // If conjunct has one field, index can be used to retrieve it
                if(cjFields.size()==1) {
                    indexableFields.add(cjFields.get(0).getEntityRelativeFieldName());
                    indexes=cjFields.get(0).getFieldEntityMetadata().getEntityInfo().getIndexes();
                }
            }
            data.setIndexableFields(indexableFields);
            if(indexes!=null) 
                data.setIndexMap(indexes.getUsefulIndexes(indexableFields));
            else
                data.setIndexMap(new HashMap<Index,Set<Path>>());
            LOGGER.debug("Node data for node {} is {}",node.getName(),data);
        }
    }

}
