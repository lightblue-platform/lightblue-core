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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.NaryLogicalExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.mediator.Finder;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.mediator.SimpleFindImpl;

import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDFindResponse;
import com.redhat.lightblue.assoc.Conjunct;

/**
 * Shared functionality for search steps
 */
public abstract class AbstractSearchStep extends Step<ResultDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSearchStep.class);

    protected QueryExpression query;
    protected Projection projection;
    protected Sort sort;
    protected Long from, to;
    protected List<Conjunct> conjuncts;
    protected boolean recordResultSetSize = false;

    public AbstractSearchStep(ExecutionBlock block) {
        super(block);
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public void setLimit(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    public void setProjection(Projection p) {
        this.projection = p;
    }

    public void setQueries(List<Conjunct> conjuncts) {
        this.conjuncts = conjuncts;
        List<QueryExpression> l = new ArrayList<>(conjuncts.size());
        for (Conjunct c : conjuncts) {
            l.add(c.getClause());
        }
        if (l.isEmpty()) {
            query = null;
        } else if (l.size() == 1) {
            query = l.get(0);
        } else {
            query = new NaryLogicalExpression(NaryLogicalOperator._and, l);
        }
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        List<ResultDocument> list = getSearchResults(ctx);
        return new ListStepResult(list);
    }

    protected abstract List<ResultDocument> getSearchResults(ExecutionContext ctx);

    public OperationContext search(ExecutionContext ctx, CRUDFindRequest req) {
        return search(block, ctx, req);
    }


    public OperationContext search(ExecutionBlock block, ExecutionContext ctx, CRUDFindRequest req) {
        OperationContext searchCtx = ctx.getOperationContext().
                getDerivedOperationContext(block.getMetadata().getName(), req);
        LOGGER.debug("SearchStep {}: entity={}, query={}, projection={}, sort={}, from={}, to={}",
                block.getQueryPlanNode().getName(),
                searchCtx.getEntityName(),
                req.getQuery(),
                req.getProjection(),
                req.getSort(),
                req.getFrom(),
                req.getTo());

        Finder finder = new SimpleFindImpl(block.getMetadata(), searchCtx.getFactory());
        CRUDFindResponse response = finder.find(searchCtx, req);

        if (searchCtx.hasErrors()) {
            ctx.getOperationContext().addErrors(searchCtx.getErrors());
            searchCtx = null;
        } else {
            if (recordResultSetSize) {
                ctx.setMatchCount((int) response.getSize());
            }
            LOGGER.debug("execute {}: returning {} documents",
                    block.getQueryPlanNode().getName(),
                    searchCtx.getDocuments().size());
        }
        return searchCtx;
    }

    public void setRecordResultSetSize(boolean b) {
        recordResultSetSize = b;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.set("entity", JsonNodeFactory.instance.textNode(block.getMetadata().getName()));
        o.set("search", query == null ? JsonNodeFactory.instance.nullNode() : query.toJson());
        if (projection != null) {
            o.set("projection", projection.toJson());
        }
        if (sort != null) {
            o.set("sort", sort.toJson());
        }
        if (from != null) {
            o.set("from", JsonNodeFactory.instance.numberNode(from));
        }
        if (to != null) {
            o.set("to", JsonNodeFactory.instance.numberNode(to));
        }
        return o;
    }

}
