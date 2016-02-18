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
 * Performs search
 * 
 * Input: n/a
 * Output: ResultDocument
 */
public class Search implements Step<ResultDocument> {
    
    private static final Logger LOGGER=LoggerFactory.getLogger(SearchStep.class);
    
    protected QueryExpression query;
    protected Projection projection;
    protected Sort sort;
    protected Long from,to;

    public void setSort(Sort sort) {
        this.sort=sort;
    }
    
    public void setLimit(Long from,Long to) {
        this.from=from;
        this.to=to;
    }
    
    public void setProjection(Projection p) {
        this.projection=p;
    }
    
    public void addQueryClause(List<Conjunct> conjuncts) {

    }

    @Override
    public Stream<ResultDocument> getResults(ExecutionContext ctx) {
        OperationContext result=search(ctx);
        if(result!=null) {
            List<DocCtx> documents=result.getDocuments();
            return documents.stream().map(doc -> doc.getOutputDocument());
        }
    }

    protected CRUDFindRequest buildFindRequest(ExecutionContext ctx) {
        CRUDFindRequest findRequest=new CRUDFindRequest();
        findRequest.setQuery(query);
        findRequest.setProjection(projection);
        findRequest.setSort(sort);
        findRequest.setFrom(from);
        findRequest.setTo(to);
        return findRequest;
    }

    protected OperationContext search(ExecutionContext ctx) {
        CRUDFindRequest req=buildFindRequest(ctx);
        if(req!=null)
            return search(ctx,req);
        else
            return null;
    }
    
    protected OperationContext search(ExecutionContext ctx,CRUDFindRequest findRequest) {
        OperationContext searchCtx=null;
        CRUDFindRequest findRequest=buildFindRequest(ctx);
        if(findRequest!=null) {
            PlanNodeExecutionBlock block=getPlanNodeBlock();
            OperationContext searchCtx=ctx.getOperationContext().
                getDerivedOperationContext(block.getEntityMetadata().getName(),
                                           findRequest);
            LOGGER.debug("SearchStep {}: entity={}, query={}, projection={}, sort={}, from={}, to={}",
                         block.getQueryPlanNode().getName(),
                         searchCtx.getEntityName(),
                         findRequest.getQuery(),
                         findRequest.getProjection(),
                         findRequest.getSort(),
                         findRequest.getFrom(),
                         findRequest.getTo());
                        
            Finder finder=new SimpleFindImpl(block.getEntityMetadata(),ctx.getFactory());
            CRUDFindResponse response=finder.find(searchCtx,findRequest);
            
            if(searchCtx.hasErrors()) {
                ctx.getOperationContext().addErrors(searchCtx.getErrors());
                searchCtx=null;
            } else {
                LOGGER.debug("execute {}: returning {} documents",
                             block.getQueryPlanNode().getName(),
                             searchCtx.getDocuments().size());
            }
        } 
        return searchCtx;
    }    
}

