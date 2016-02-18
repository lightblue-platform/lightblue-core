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
package com.redhat.lightblue.assoc;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;

import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.Factory;

import com.redhat.lightblue.metadata.CompositeMetadata;

import com.redhat.lightblue.mediator.Finder;
import com.redhat.lightblue.mediator.OperationContext;

import com.redhat.lightblue.assoc.scorers.IndexedFieldScorer;
import com.redhat.lightblue.assoc.iterators.BruteForceQueryPlanIterator;
import com.redhat.lightblue.assoc.scorers.SimpleScorer;
import com.redhat.lightblue.assoc.iterators.First;
import com.redhat.lightblue.assoc.ep.ExecutionPlan;

public class CompositeFindImpl implements Finder {
    
    private static final Logger LOGGER=LoggerFactory.getLogger(CompositeFindImpl.class);
    
    private final CompositeMetadata root;
    private final Factory factory;

    // Instance state data. This class is meant to be thrown away after used once.

    // Query analysis information for the request query
    private transient List<QueryFieldInfo> requestQueryInfo;

    // The search and retrieval query plans
    // If searchQPlan is null, then retrievalQPlan performs both search and retrieval
    // If searchQPlan is not null, then searchQPlan searches the documents, and retrievalQPlan
    // retrieves the found documents
    private transient QueryPlan searchQPlan;
    private transient QueryPlan retrievalQPlan;

    private transient ExecutionPlan executionPlan;

    public CompositeFindImpl(CompositeMetadata md,
                             Factory factory) {
        this.root=md;
        this.factory=factory;
    }

    @Override
    public CRUDFindResponse find(OperationContext ctx,
                                 CRUDFindRequest req) {
        LOGGER.debug("Composite find: start");

        // Composite find algorithm works like this:
        //   1) Find the minimal entity tree required to evaluate the request.query
        //   2) Find a query plan for that minimal entity tree
        //   3) If the query plan root is also the root entity, then expand that query
        //      plan to include all nodes
        //   4) Execute search based on query plan
        //   5) If query plan node is not the entity root, find documents found for entity root,
        //      and re-retrieve the documents

        // First: detemine minimal entity tree containing the nodes sufficient to
        // evaluate the query
        Set<CompositeMetadata> minimalTree=findMinimalSetOfQueryEntities(req.getQuery(),
                                                                         ctx.getTopLevelEntityMetadata());

        selectQueryPlan(req.getQuery(),minimalTree);
        LOGGER.debug("Search query plan:{}, retrieval query plan:{}",searchQPlan,retrievalQPlan);

        executionPlan=new ExecutionPlan(req.getProjection(),
                                        req.getSort(),
                                        req.getFrom(),
                                        req.getTo(),
                                        root,
                                        searchQPlan,
                                        retrievalQPlan);
        LOGGER.debug("Execution plan:{}",executionPlan);
        
        
        LOGGER.debug("Composite find: end");
        return null;
   }

    /**
     * Selects the search and retrieval query plans based on the minimal tree and request query. 
     *
     * There is either only a retrieval plan, or both a search plan
     * and retrieval plan. If search plan cannot retrieve the root
     * entity, then we use the search plan to collect the entities
     * matching the search criteria, and then the retrieval plan to
     * retrieve those entities completely. If the search plan can both
     * search and retrieve the entities, there will be only a
     * retrieval plan.
     *
     * 
     */
    private void selectQueryPlan(QueryExpression requestQuery,
                                 Set<CompositeMetadata> minimalTree) {
        searchQPlan=retrievalQPlan=null;
        
        if(minimalTree.size()>1) {
            // There are multiple entities required to evaluate the query
            
            // Choose a query plan
            QueryPlan searchQP=new QueryPlanChooser(root,
                                                    new BruteForceQueryPlanIterator(),
                                                    new IndexedFieldScorer(),
                                                    requestQuery,
                                                    minimalTree).choose();
            LOGGER.debug("Candidate plan: {}",searchQP);
            // If the query plan has only one source, and that source is the root, then
            // we don't need to search and retrieve in two separate steps, we can simply
            // retrieve everything while we search
            QueryPlanNode[] roots=searchQP.getSources();
            if(roots.length==1&&roots[0].getMetadata()==root) {
                LOGGER.debug("Search is trivial, root node is at query plan root, so search and retrieve");
                // Build a new query plan containing all entities. This plan should
                // have the same root as before. If not, something must be
                // wrong, and we fall back to a search/retrieve query
                QueryPlan fullPlan=new QueryPlanChooser(root,
                                                        new BruteForceQueryPlanIterator(),
                                                        new IndexedFieldScorer(),
                                                        requestQuery,
                                                        null).choose();
                // This plan must also have a single root
                roots=fullPlan.getSources();
                if(roots.length==1&&roots[0].getMetadata()==root) {
                    // Retrieve everything, no separate search plan
                    retrievalQPlan=fullPlan;
                    searchQPlan=null;
                } else {
                    // Search and retrieve in separate phases
                    searchQPlan=searchQP;
                }
            } else {
                // Multiple roots, search and retrieve in separate phases
                searchQPlan=searchQP;
            }
        } else {
            // Minimal tree has only one entity. That entity must be the root entity
            // That means, a single retrieval plan can search and retrieve            
            searchQPlan=null;
        }
        if(retrievalQPlan==null) {
            if(searchQPlan==null) {
                // Search and retrieve
                retrievalQPlan=new QueryPlanChooser(root,
                                                    new BruteForceQueryPlanIterator(),
                                                    new IndexedFieldScorer(),
                                                    requestQuery,
                                                    null).choose();
            } else {
                // No search, only retrieve. No query.
                retrievalQPlan=new QueryPlanChooser(root,
                                                    new First(),
                                                    new SimpleScorer(),
                                                    null,
                                                    null).choose();
            }
        }
    }
    
    
    /**
     * Determine which entities are required to evaluate the given query
     */
    private Set<CompositeMetadata> findMinimalSetOfQueryEntities(QueryExpression query,
                                                                 CompositeMetadata md) {
        Set<CompositeMetadata> entities=new HashSet<>();
        if(query!=null) {
            AnalyzeQuery aq=new AnalyzeQuery(md,null);
            aq.iterate(query);
            requestQueryInfo=aq.getFieldInfo();
            LOGGER.debug("Analyze query results for {}: {}",query,requestQueryInfo);
            for(QueryFieldInfo fi:requestQueryInfo) {
                CompositeMetadata e=fi.getFieldEntity();
                if(e!=md)
                    entities.add(e);
            }
            // All entities on the path from every entity to the root
            // should also be included
            Set<CompositeMetadata> intermediates=new HashSet<>();
            for(CompositeMetadata x:entities) {
                CompositeMetadata trc=x.getParent();
                while(trc!=null) {
                    intermediates.add(trc);
                    trc=trc.getParent();
                }
            }
            entities.addAll(intermediates);
        }
        // At this point, entities contains all required entities, but maybe not the root
        entities.add(md);
        LOGGER.debug("Minimal set of entities required to evaluate {}:{}",query,entities);
        return entities;
    }
}
