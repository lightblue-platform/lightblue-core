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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.crud.CRUDFindRequest;

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
    protected List<ResultDocument> getSearchResults(ExecutionContext ctx) {
        BatchQueryExecutor executor = new BatchQueryExecutor(256, ctx);
        source.getStep().getResults(ctx).stream().forEach(x -> executor.add(x));
        return executor.getResults();
    }

    public class BatchQueryExecutor {
        private final int batchSize;
        private List<JoinTuple> jtBatch;
        ;
        private List<QueryExpression> qBatch;
        private final ExecutionContext ctx;
        private List<ResultDocument> docs = new ArrayList<>();

        public BatchQueryExecutor(int batchSize, ExecutionContext ctx) {
            this.batchSize = batchSize;
            this.jtBatch = new ArrayList<>(batchSize);
            this.qBatch = new ArrayList<>(batchSize);
            this.ctx = ctx;
        }

        public void add(JoinTuple tuple) {
            jtBatch.add(tuple);
            qBatch.addAll(Searches.writeQueriesForJoinTuple(tuple, block));
            if (qBatch.size() >= batchSize) {
                executeBatch();
                qBatch = new ArrayList<>(batchSize);
                jtBatch = new ArrayList<>(batchSize);
            }
        }

        public void executeBatch() {
            if (!qBatch.isEmpty()) {
                QueryExpression q = Searches.combine(NaryLogicalOperator._or, qBatch);
                CRUDFindRequest findRequest = new CRUDFindRequest();
                findRequest.setQuery(Searches.and(q, query));
                findRequest.setProjection(projection);
                findRequest.setSort(sort);
                findRequest.setFrom(from);
                findRequest.setTo(to);
                OperationContext opctx = search(ctx, findRequest);
                opctx.getDocuments().stream().
                        forEach(doc -> docs.add(new ResultDocument(block, doc.getOutputDocument())));
            }
        }

        public List<ResultDocument> getResults() {
            executeBatch();
            return docs;
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
