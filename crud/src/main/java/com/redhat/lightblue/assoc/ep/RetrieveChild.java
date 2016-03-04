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
import java.util.Iterator;
import java.util.Map;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.assoc.Conjunct;
import com.redhat.lightblue.assoc.QueryPlanNode;
import com.redhat.lightblue.assoc.BindQuery;
import com.redhat.lightblue.assoc.Binder;

/**
 * Given a parent document, retrieves child documents for a slot, and attaches them to the corresponding slots
 * 
 * Input: ResultDocument
 * Output: ResultDocument
 */
public class RetrieveChild extends Step<ResultDocument> {
    
    private static final Logger LOGGER=LoggerFactory.getLogger(RetrieveChild.class);

    private final Step<ResultDocument> parent;

    public RetrieveChild(ExecutionBlock block,Step<ResultDocument> source) {
        super(block);
        this.parent=source;
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        // return new StepResult<ResultDocument>() {
        //     public Stream<ResultDocument> stream() {
        //         return parent.getResults(ctx).stream().
        //             map(parentDoc -> {
        //                     Map<ChildSlot,QueryExpression> queries=writeChildQueriesFromParentDoc(aq,parentDoc);
        //                     for(Map.Entry<ChildSlot,QueryExpression> q:queries) {
        //                         StepResult<ResultDocument> results=searchAndRetrieve(q);
        //                         parentDoc.insertChildDocs(q.getKey(),results.stream());
        //                     }
        //                     return parentDoc;
        //                 }
        //                 );
        //     };
        // };
        return null;
    }

    @Override
    public JsonNode toJson() {
        return null;
    }
    
    
}

