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
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.metadata.Indexes;
import com.redhat.lightblue.metadata.Index;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.Field;

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

    @Override
    public QueryPlanData newDataInstance() {
        return new IndexedFieldScorerData();
    }
    @Override
    public Comparable score(QueryPlan qp) {
        LOGGER.debug("score begin");

        QueryPlanNode[] nodes=qp.getAllNodes();
        QueryPlanNode[] sources=qp.getSources();
        QueryPlanNode root=null;
        for(QueryPlanNode node:nodes) {
            if(node.getMetadata().getParent()==null)
                root=node;
        }

        if (null == root) {
            // a never happen scenario, but CYA...
            throw new IllegalStateException("Unable to find root metadata");
        }

        BigInteger finalCost;
        // If the root entity node is the root node, then there is only the cost of retrieval
        // Otherwise, the cost of retrieval and cost of query must be considered
        if(sources.length==1&&sources[0]==root) {            
            // If the root entity has identity search and the query plan
            // node for the root entity is at the plan root, then this is
            // the lowest cost option
            if(((IndexedFieldScorerData)root.getData()).isIdentitySearch() ) {
                finalCost=BigInteger.ZERO;
            } else {
                finalCost=computeRetrievalCost(root);
            }
        } else {
            // Multiple roots, there is both a query and retrieval phase
            CostAndSize cs=computeQueryCost(root);
            // Have to retrieve for every result in query
            finalCost=cs.cost.add(cs.size);
        }
        LOGGER.debug("Final cost:{}",finalCost);
        return finalCost;
    }

    private BigInteger computeRetrievalCost(QueryPlanNode root) {
        CostAndSize cs=((IndexedFieldScorerData)root.getData()).getCostAndSize();
        BigInteger cost=cs.cost;
        // Compute cost to retrieve destination nodes
        for(QueryPlanNode dest:root.getDestinations()) {
            cost=cost.add(cs.size.multiply(computeRetrievalCost(dest)));
        }
        return cost;
    }

    private CostAndSize computeQueryCost(QueryPlanNode root) {
        // Compute cost to retrieve source nodes
        QueryPlanNode[] sources=root.getSources();
        BigInteger cost=BigInteger.ONE;
        if(sources!=null&&sources.length>0) {
            // There is a join, that means, the result set size is a product of sources
            BigInteger size=BigInteger.ONE;
            for(QueryPlanNode source:sources)
                size=size.multiply(((IndexedFieldScorerData)source.getData()).getCostAndSize().size);
            cost=((IndexedFieldScorerData)root.getData()).getCostAndSize().cost.multiply(size);
        }
        return new CostAndSize(cost,((IndexedFieldScorerData)root.getData()).getCostAndSize().size);
    }

    @Override
    public void reset(QueryPlanChooser c) {
        LOGGER.debug("reset");
        // Conjuncts associated with nodes will not move from one node to another
        // So we can measure the cost associated with them from the start
        for(QueryPlanNode node:c.getQueryPlan().getAllNodes()) {
            if (!(node.getData() instanceof IndexedFieldScorerData)) {
                throw new IllegalStateException("Expected instance of " + IndexedFieldScorerData.class.getName() + " but got: " + node.getData().getClass().getName());
            }
            IndexedFieldScorerData data=(IndexedFieldScorerData)node.getData();
            CompositeMetadata md=node.getMetadata();
            data.setRootNode(md.getParent()==null);
            Field[] identities=md.getEntitySchema().getIdentityFields();
            if(identities!=null&&identities.length>0) {
                List<Path> idFields=new ArrayList<Path>(identities.length);
                for(Field f:identities)
                    idFields.add(md.getEntityRelativeFieldName(f));
                Map<Index,Set<Path>> idIndexes=md.getEntityInfo().getIndexes().getUsefulIndexes(idFields);
                for(Map.Entry<Index,Set<Path>> s:idIndexes.entrySet()) {
                    if(s.getValue().size()==idFields.size()  ) {
                        data.setIdIndex(s.getKey());
                        break;
                    }
                }
                data.setIdFields(idFields);
            }
            
            Set<Path> indexableFields=new HashSet<>();
            for(Conjunct cj:data.getConjuncts()) {
                ResolvedFieldInfo[] cjFields=cj.getFieldInfo();
                // If conjunct has one field, index can be used to retrieve it
                if(cjFields.length==1) {
                    indexableFields.add(cjFields[0].getEntityRelativeFieldName());
                }
            }
            data.setIndexableFields(indexableFields);
            if(md.getEntityInfo().getIndexes()!=null) {
                data.setIndexMap(md.getEntityInfo().getIndexes().getUsefulIndexes(indexableFields));
                Index idIndex=data.getIdIndex();
                if(idIndex!=null) {
                    Set<Path> usefulness=idIndex.getUsefulness(indexableFields);
                    if(usefulness.size()==idIndex.getFields().size())
                        data.setIdentitySearch(true);
                }
            } else
                data.setIndexMap(new HashMap<Index,Set<Path>>());
            LOGGER.debug("Node data for node {} is {}",node.getName(),data);
        }
    }

}
