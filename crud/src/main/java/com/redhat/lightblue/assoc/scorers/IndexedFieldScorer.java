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
import com.redhat.lightblue.assoc.QueryFieldInfo;
import com.redhat.lightblue.assoc.QueryPlan;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.QueryPlanData;
import com.redhat.lightblue.assoc.QueryPlanChooser;
import com.redhat.lightblue.assoc.AssocConstants;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Query plan scoring based on search clauses on indexed fields. These are the
 * heuristics it uses:
 *
 * <ul>
 * <li>No indexed queries on any node: MAX</li>
 * <li>We want nodes with indexed queries as close to the sources as
 * possible</li>
 * <li>We want the root node to be the last indexed query node, if there is
 * indexed queries for root node</li>
 * </ul>
 */
public class IndexedFieldScorer implements QueryPlanScorer, Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexedFieldScorer.class);

    @Override
    public QueryPlanData newDataInstance() {
        return new IndexedFieldScorerData();
    }

    @Override
    public Comparable score(QueryPlan qp) {
        LOGGER.debug("score begin");

        QueryPlanNode[] nodes = qp.getAllNodes();
        QueryPlanNode[] sources = qp.getSources();
        
        QueryPlanNode root = null;
        int numNodeQueries=0;
        for (QueryPlanNode node : nodes) {
            if (node.getMetadata().getParent() == null) {
                root = node;
            }
            if(getData(node).hasQueries())
                numNodeQueries++;
        }

        if (null == root) {
            // a never happen scenario, but CYA...
            throw new IllegalStateException("Unable to find root metadata");
        }
        if(numNodeQueries==0) {
            // No queries assigned to nodes

            // If the plan matches metadata, return low cost, otherwise, return very high cost
            // This forces the use of the plan that matches metadata if there are no queries
            if(qp.isPlanMatchesMetadata())
                return BigInteger.ZERO;
            else
                return new BigInteger("999999999");
        }
        // Here, we know that there are some queries assigned to query plan nodes

        BigInteger penalty=BigInteger.ONE;

        // Query plans that have query-free nodes in front of queried nodes are bad
        // Query plans with multiple root nodes are bad
        if(sources.length>1)
            penalty=BigInteger.TEN;
        for(QueryPlanNode node:sources)
            if(!getData(node).hasQueries())
                penalty=penalty.multiply(new BigInteger("100"));
        // Any intermediate node with no queries that is above the data root node is troble
        for(QueryPlanNode node:nodes) {
            if(!getData(node).hasQueries()) {
                if(aboveRoot(node,root)) {
                    QueryPlanNode[] s=node.getSources();
                    if(s!=null&&s.length>0) {
                        penalty=penalty.multiply(new BigInteger("5"));
                    }
                }
            }            
        }
        LOGGER.debug("penalty={}",penalty);
        
        BigInteger finalCost;
        // If the root entity node is the root node, then there is only the cost of retrieval
        // Otherwise, the cost of retrieval and cost of query must be considered
        if (sources.length == 1 && sources[0] == root) {
            // If the root entity has identity search and the query plan
            // node for the root entity is at the plan root, then this is
            // the lowest cost option
            if (getData(root).isIdentitySearch()) {
                finalCost = BigInteger.ZERO;
            } else {
                finalCost = computeRetrievalCost(root);
            }
        } else {
            // Multiple roots, there is both a query and retrieval phase
            CostAndSize cs = computeQueryCost(root, qp);
            // Have to retrieve for every result in query
            finalCost = cs.cost.add(cs.size);
        }
        LOGGER.debug("Final cost w/o penalty:{}", finalCost);
        finalCost=finalCost.multiply(penalty);
        LOGGER.debug("Final cost:{}", finalCost);
        return finalCost;
    }

    private boolean aboveRoot(QueryPlanNode node,QueryPlanNode root) {
        QueryPlanNode[] sources=root.getSources();
        if(sources!=null) {
            for(QueryPlanNode source:sources)
                if(source==node||aboveRoot(node,source)) {
                    return true;
                }
        }
        return false;
    }

    private BigInteger computeRetrievalCost(QueryPlanNode root) {
        CostAndSize cs = getData(root).getCostAndSize();
        BigInteger cost = cs.cost;
        // Compute cost to retrieve destination nodes
        for (QueryPlanNode dest : root.getDestinations()) {
            cost = cost.add(cs.size.multiply(computeRetrievalCost(dest)));
        }
        return cost;
    }

    private CostAndSize computeQueryCost(QueryPlanNode root, QueryPlan qp) {
        // Compute cost to retrieve source nodes
        QueryPlanNode[] sources = root.getSources();
        BigInteger cost = BigInteger.ONE;
        if (sources != null && sources.length > 0) {
            // There is a join, that means, the result set size is a product of sources
            BigInteger size = BigInteger.ONE;
            BigInteger sourceCost = BigInteger.ZERO;
            BigInteger totalAssociationCost = BigInteger.ZERO;
            for (QueryPlanNode source : sources) {
                CostAndSize src = computeQueryCost(source, qp);
                sourceCost = sourceCost.add(src.cost);
                size = size.multiply(src.size);
                // If there is an edge query with a usable index, the cost is low
                // Otherwise, the cost is dependent on the node query only
                // We'll assume that if there is an edge query, that is an efficient query
                BigInteger associationCost = getData(root).getCostAndSize().cost;
                QueryPlanData edgeData = qp.getEdgeData(root, source);
                if (edgeData != null) {
                    List<Conjunct> conjuncts = edgeData.getConjuncts();
                    if (conjuncts != null && !conjuncts.isEmpty()) {
                        associationCost = IndexedFieldScorerData.estimateCost(false, true, true).min(associationCost);
                    }
                }
                totalAssociationCost = totalAssociationCost.add(associationCost);
            }
            cost = totalAssociationCost.multiply(size);
            cost = cost.add(sourceCost);
        }
        return new CostAndSize(cost, getData(root).getCostAndSize().size);
    }

    @Override
    public void reset(QueryPlanChooser c) {
        LOGGER.debug("reset");
        for (QueryPlanNode node : c.getQueryPlan().getAllNodes()) {
            if (!(node.getData() instanceof IndexedFieldScorerData)) {
                throw new IllegalStateException("Expected instance of " + IndexedFieldScorerData.class.getName() + " but got: " + node.getData().getClass().getName());
            }
            IndexedFieldScorerData data = getData(node);
            CompositeMetadata md = node.getMetadata();
            data.setRootNode(md.getParent() == null);
            Field[] identities = md.getEntitySchema().getIdentityFields();
            if (identities != null && identities.length > 0) {
                List<Path> idFields = new ArrayList<Path>(identities.length);
                for (Field f : identities) {
                    idFields.add(md.getEntityRelativeFieldName(f));
                }
                Map<Index, Set<Path>> idIndexes = md.getEntityInfo().getIndexes().getUsefulIndexes(idFields);
                for (Map.Entry<Index, Set<Path>> s : idIndexes.entrySet()) {
                    if (s.getValue().size() == idFields.size()) {
                        data.setIdIndex(s.getKey());
                        break;
                    }
                }
                data.setIdFields(idFields);
            }

            Set<Path> indexableFields = new HashSet<>();
            for (Conjunct cj : data.getConjuncts()) {
                List<QueryFieldInfo> cjFields = cj.getFieldInfo();
                // If conjunct has one field, index can be used to retrieve it
                if (cjFields.size() == 1) {
                    indexableFields.add(cjFields.get(0).getEntityRelativeFieldNameWithContext());
                }
            }
            data.setIndexableFields(indexableFields);
            if (md.getEntityInfo().getIndexes() != null) {
                data.setIndexMap(md.getEntityInfo().getIndexes().getUsefulIndexes(indexableFields));
                Index idIndex = data.getIdIndex();
                if (idIndex != null) {
                    Set<Path> usefulness = idIndex.getUsefulness(indexableFields);
                    if (usefulness.size() == idIndex.getFields().size()) {
                        data.setIdentitySearch(true);
                    }
                }
            } else {
                data.setIndexMap(new HashMap<Index, Set<Path>>());
            }
            LOGGER.debug("Node data for node {} is {}", node.getName(), data);
        }
    }

    private IndexedFieldScorerData getData(QueryPlanNode node) {
        try {
            return (IndexedFieldScorerData) node.getData();
        } catch (ClassCastException e) {
            throw Error.get(AssocConstants.ERR_INVALID_QUERYPLAN);
        }
    }
}
