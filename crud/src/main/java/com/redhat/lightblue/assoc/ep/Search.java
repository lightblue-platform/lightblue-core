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

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.mediator.SimpleFindImpl;

import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.DocCtx;

import com.redhat.lightblue.util.JsonDoc;

/**
 * Performs search
 *
 * Input: n/a Output: ResultDocument
 */
public class Search extends AbstractSearchStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(Search.class);

    public Search(ExecutionBlock block) {
        super(block);
    }

    protected List<ResultDocument> postProcess(OperationContext result, ExecutionContext ctx) {
        return result.getDocuments().stream().
                map(doc -> new ResultDocument(block, doc.getOutputDocument())).
                collect(Collectors.toList());
    }

    public OperationContext search(ExecutionContext ctx) {
        CRUDFindRequest req = buildFindRequest(ctx);
        if (req != null) {
            return search(ctx, req);
        } else {
            return null;
        }
    }

    protected CRUDFindRequest buildFindRequest(ExecutionContext ctx) {
        CRUDFindRequest findRequest = new CRUDFindRequest();
        findRequest.setQuery(query);
        findRequest.setProjection(projection);
        findRequest.setSort(sort);
        findRequest.setFrom(from);
        findRequest.setTo(to);
        return findRequest;
    }

    @Override
    protected List<ResultDocument> getSearchResults(ExecutionContext ctx) {
        OperationContext result = search(ctx);
        if (result != null) {
            return postProcess(result, ctx);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public JsonNode explain(ExecutionContext ctx) {
        ObjectNode node=(ObjectNode)toJson();
        CRUDFindRequest req=buildFindRequest(ctx);
        OperationContext searchCtx = ctx.getOperationContext().
            getDerivedOperationContext(block.getMetadata().getName(), req);
        new SimpleFindImpl(block.getMetadata(), searchCtx.getFactory()).explain(searchCtx,req);
        List<JsonDoc> docs = searchCtx.getOutputDocumentsWithoutErrors();
        if(docs!=null&&!docs.isEmpty()) {
            if(docs.size()==1) {
                node.set("implementation",docs.get(0).getRoot());
            } else {
                ArrayNode arr=JsonNodeFactory.instance.arrayNode();
                for(JsonDoc doc:docs) {
                    arr.add(doc.getRoot());
                }
                node.set("implementation",arr);
            }
        }
        return node;
    }

}
