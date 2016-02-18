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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;

import com.redhat.lightblue.mediator.Finder;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.mediator.SimpleFindImpl;

import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.FieldBinding;

/**
 * Performs searches based on the n-tuple of result documents obtained from the source steps
 * 
 * Input: [ ResultDocument ]
 * Output: ResultDocument
 */
public class ConstrainedSearch extends Search {
    
    private static final Logger LOGGER=LoggerFactory.getLogger(ConstrainedSearch.class);

    private Step<List<ResultDocument>> source;
    private ExecutionBlock block;

    private List<AssociationQuery> associationQueries;
    private QueryExpression combinedAssociationQuery;
    
    public ConstrainedSearch(Step<List<ResultDocument>> source) {
        this.source=source;
    }
    
    @Override
    public ResultStream<ResultDocument> getResults(ExecutionContext ctx) {
        return new ConstrainedSearchResultStream(source.getResults(ctx),ctx);
    }

    @Override
    protected CRUDFindRequest buildFindRequest(ExecutionContext ctx) {
    }

    /**
     * Returns true if the entity associated with sourceNode is the
     * parent entity of the entity associated with this node
     */
    private boolean isParentEntity(QueryPlanNode sourceNode) {
        return block.getQueryPlanNode().getMetada().getParent()==sourceNode.getMetadata();
    }

    private void initialize() {
        if(associationQueries==null) {
            List<ExecutionBlock> sources=block.getSourceBlocks();
            if(sources!=null) {
                associationQueries=sources.stream().
                    map(t->block.getAssociationQueryForEdge(t.getQueryPlanNode().getProperty(ExecutionBlock.class))).
                    collect(Collectors.toList());
                List<QueryExpression> ql=associationQueries.stream().
                    map(AssociationQuery::getQuery).
                    filter(q-> q!=null).
                    collect(Collectors.toList());
                if(ql.size()==1)
                    combinedAssociationQuery=ql.get(0);
                else if(ql.size()>0)
                    combinedAssociationQuery=new NaryLogicalExpression(NaryLogicalOperator._and,ql);
            } 
        }
    }


    /**
     * Return a list of queries based on the document tuple retrieved from a join
     */
    private List<QueryExpression> getQueries(List<ResultDocument> tuple) {

        // Lazy initialization of result document bindings
        int n=tuple.size();
        for(int i=0;i<n;i++) {
            ResultDocument doc=tuple.get(i);
            if(doc.getBindings()==null) {
                doc.initializeBindings(aqList.get(i));
            }
        }

        // Create a tuple from all document binder values
        Tuples<Binder> bindingTuples=new Tuples<>();
        for(int i=0;i<n;i++) {
            ResultDocument doc=tuple.get(i);
            for(List<Binder> bind:doc.getBindings().getBindings().values)
                bindingTuples.add(bind);
        }

        // Iterate the tuple, and create a query for each value
        // If there are no tuples, there is only one query
        List<QueryExpression> queries=new ArrayList<>();
        for(Iterator<List<Binder>> itr=tuples.iterator();itr.hasNext();) {
            hasTuples=true;
            List<Binder> binders=itr.next();
            BindQuery binder=new BindQuery(binders);
            QueryExpression query=binder.iterate(combinedAssociationQuery);
            queries.add(query);
        }
        if(queries.isEmpty()) {
            // Nothing to bind in the query
            if(combinedAssociationQuery!=null)
                queries.add(combinedAssociationQuery);
        }
        LOGGER.debug("Queries to execute: {}",queries);
        return queries;
    }

    private List<ResultDocument> executeQuery(ExecutionContext ctx,QueryExpression query) {
        CRUDFindRequest findRequest=new CRUDFindRequest();
        findRequest.setQuery(query);
        findRequest.setProjection(projection);
        findRequest.setSort(sort);
        findRequest.setFrom(from);
        findRequest.setTo(to);
        OperationContext octx=super.search(ctx,findRequest);
        if(octx!=null) {
            return octx.getDocuments();
        } else {
            return null;
        }
    }

    /**
     * Insert the child document into the parent document
     *
     * @param parentDoc The parent document
     * @param childDocs A list of child docs
     * @param dest The destination field name to insert the result set
     */
    private static ResultDocument insertChildDocs(ResultDocument parentDoc,
                                                  List<ResultDocument>> childDocs,
                                                  Path dest) {

    }
    
    @Override
    public ResultStream<ResultDocument> getResults(ExecutionContext ctx) {
        initialize();
        return new ConstrainedSearchResultStream(source.getResults(ctx));
    }

    
    
    private class ConstrainedSearchResultStream extends AbstractResultStream<ResultDocument> {
        
        private final ResultStream<List<ResultDocument>> sourceStream;

        public ConstrainedSearchResultStream(ResultStream<List<ResultDocument>> sourceStream) {
            this.sourceStream=sourceStream;
        }
        
        protected ResultDocument nextItem() {
            List<ResultDocument> tuple=sourceStream.next();
            if(tuple!=null) {
                
            }
            return null;
        }
    }
}

