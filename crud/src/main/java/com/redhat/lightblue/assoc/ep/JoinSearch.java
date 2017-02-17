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
import java.util.NoSuchElementException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.DocumentStream;
import com.redhat.lightblue.crud.DocCtx;

/**
 * Performs searches based on the n-tuple of result documents obtained from the
 * source steps
 *
 * Input: JoinTuple Output: ResultDocument
 */
public class JoinSearch extends AbstractSearchStep {

    private static final Logger LOGGER = LoggerFactory.getLogger(JoinSearch.class);

    private final Source<JoinTuple> source;

    public JoinSearch(ExecutionBlock block, Source<JoinTuple> source) {
        super(block);
        this.source = source;
    }

    @Override
    protected DocumentStream<ResultDocument> getSearchResults(final ExecutionContext ctx) {
        return new BatchQueryIterator(256,ctx);
    }

    /**
     * Streaming batch query executor/iterator
     *
     * When the results are retrieved from the stream, executes a
     * batch of queries, computes results, and streams them to the
     * caller
     */
    private class BatchQueryIterator implements DocumentStream<ResultDocument> {
        private final int batchSize;
        private final ExecutionContext ctx;
        private final Iterator<JoinTuple> sourceStream;

        private DocumentStream<DocCtx> currentIterator;
        private boolean done=false; // Are we still iterating, or are we done?
        
        public BatchQueryIterator(int batchSize,ExecutionContext ctx) {
            this.batchSize=batchSize;
            this.ctx=ctx;
            sourceStream=source.getStep().getResults(ctx).stream().iterator();
        }

        @Override
        public boolean hasNext() {
            if(!done) {
                if(currentIterator==null||!currentIterator.hasNext())
                    retrieveNextBatch();
                if(done)
                    return false;
                else
                    return currentIterator.hasNext();
            } else {
                return false;
            }
        }
        
        @Override
        public ResultDocument next() {
            if(!done) {
                if(currentIterator==null||!currentIterator.hasNext())
                    retrieveNextBatch();
                if(currentIterator!=null)
                    return new ResultDocument(block,currentIterator.next().getOutputDocument());
            }
            throw new NoSuchElementException();
        }

        @Override
        public void close() {
            if(currentIterator!=null)
                currentIterator.close();
        }            
        
        private void retrieveNextBatch() {
            do {
                int n=0;
                ArrayList<QueryExpression> qBatch=new ArrayList<>(batchSize);
                if(currentIterator!=null) {
                    currentIterator.close();
                    currentIterator=null;
                }
                while(sourceStream.hasNext()&&n<batchSize) {
                    JoinTuple t=sourceStream.next();
                    qBatch.addAll(Searches.writeQueriesForJoinTuple(t, block));
                    n++;
                }
                if(!qBatch.isEmpty()) {
                    QueryExpression q = Searches.combine(NaryLogicalOperator._or, qBatch);
                    CRUDFindRequest findRequest = new CRUDFindRequest();
                    findRequest.setQuery(Searches.and(q, query));
                    findRequest.setProjection(projection);
                    findRequest.setSort(sort);
                    findRequest.setFrom(from);
                    findRequest.setTo(to);
                    OperationContext opctx = search(ctx, findRequest);
                    currentIterator=opctx.getDocumentStream();
                    if(!currentIterator.hasNext()) {
                        currentIterator.close();
                        currentIterator=null;
                    }
                } else {
                    done=true;
                }
            } while(!done&&currentIterator==null);
        }   
            
    }

    @Override
    public JsonNode toJson() {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.set("join-search", source.getStep().toJson());
        if (query != null) {
            o.set("query", query.toJson());
        }
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

    @Override
    public JsonNode explain(ExecutionContext ctx) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.set("join-search", source.getStep().explain(ctx));
        if (query != null) {
            o.set("query", query.toJson());
        }
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
