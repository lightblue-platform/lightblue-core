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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.stream.Collectors;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.NaryLogicalOperator;

import com.redhat.lightblue.assoc.BindQuery;

import com.redhat.lightblue.eval.QueryEvaluator;

/**
 * There are two sides to an Assemble step: Assemble gets results from the
 * source, and for each of those documents, it runs the associated queries on
 * the destinations, gets the results, and inserts those documents to the
 * document it got from the source side.
 */
public class Assemble extends Step<ResultDocument> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Assemble.class);

    private final ExecutionBlock[] destinationBlocks;
    private final Source<ResultDocument> source;
    private Map<ExecutionBlock, Assemble> destinations;

    public Assemble(ExecutionBlock block,
                    Source<ResultDocument> source,
                    ExecutionBlock[] destinationBlocks) {
        super(block);
        this.source = source;
        this.destinationBlocks = destinationBlocks;
    }

    public List<ResultDocument> getResultList(QueryExpression q, ExecutionContext ctx) {
        LOGGER.debug("getResultList q={} block={}", q, block);
        Retrieve r = block.getStep(Retrieve.class);
        if (r != null) {
            r.setQuery(q);
            StepResult<ResultDocument> results = block.getResultStep().getResults(ctx);
            return results.stream().collect(Collectors.toList());
        } else {
            throw new IllegalStateException("Cannot find a Retrieve step in block");
        }
    }

    @Override
    public void initialize() {
        destinations = new HashMap<ExecutionBlock, Assemble>();
        for (ExecutionBlock x : destinationBlocks) {
            Assemble a = x.getStep(Assemble.class);
            if (a != null) {
                destinations.put(x, a);
            } else {
                throw new IllegalArgumentException("No assemble step in " + x);
            }
        }
    }
    
    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        LOGGER.debug("getResults, source:{}, destinations={}", source, destinations);
        // Get the results from the source
        StepResult<ResultDocument> sourceResults = source.getStep().getResults(ctx);
        List<ResultDocument> results = sourceResults.stream().collect(Collectors.toList());
        if (ctx.hasErrors()) {
            return StepResult.EMPTY;
        }

        List<Future> assemblers = new ArrayList<>();
        for (Map.Entry<ExecutionBlock, Assemble> destination : destinations.entrySet()) {
            AssociationQuery aq = destination.getKey().getAssociationQueryForEdge(block);
            LOGGER.debug("Scheduling batch assembler with aq={} block={}", aq, destination.getKey());
            BatchAssembler batchAssembler = new BatchAssembler(256, aq, destination.getValue(), ctx);
            assemblers.add(ctx.getExecutor().submit(() -> {
                if (aq.getQuery() == null) {
                    if(aq.isAlwaysTrue()) {
                        results.stream().forEach(batchAssembler::addDoc);
                        batchAssembler.endDoc();
                    }
                } else {
                    results.stream().forEach(doc -> {
                        batchAssembler.addDoc(doc);
                        Map<ChildSlot, QueryExpression> queries = Searches.
                                writeChildQueriesFromParentDoc(aq, doc);
                        queries.values().stream().forEach(batchAssembler::addQuery);
                        batchAssembler.endDoc();
                    });
                }
                batchAssembler.commit();
            }));
        }
        try {
            for (Future x : assemblers) {
                x.get();
            }
        } catch (Exception ie) {
            throw new RuntimeException(ie);
        }
        if (ctx.hasErrors()) {
            return StepResult.EMPTY;
        }
        // Stream results
        return new ListStepResult(results);
    }

    private static class DocAndQ {
        private final ResultDocument doc;
        private final List<QueryExpression> queries = new ArrayList<>();

        public DocAndQ(ResultDocument doc) {
            this.doc = doc;
        }
    }

    private static class BatchAssembler {
        private List<DocAndQ> docs = new ArrayList<>();
        private List<QueryExpression> queries = new ArrayList<>();
        private final int batchSize;
        private final AssociationQuery aq;
        private final Assemble dest;
        private final ExecutionContext ctx;

        public BatchAssembler(int batchSize, AssociationQuery aq, Assemble dest, ExecutionContext ctx) {
            this.batchSize = batchSize;
            this.dest = dest;
            this.aq = aq;
            this.ctx = ctx;
        }

        public void addQuery(QueryExpression q) {
            docs.get(docs.size() - 1).queries.add(q);
            queries.add(q);
        }

        public void addDoc(ResultDocument doc) {
            docs.add(new DocAndQ(doc));
        }

        public void endDoc() {
            if (queries.size() >= batchSize) {
                commit();
            }
        }

        public void commit() {
            if (!docs.isEmpty()) {
                QueryExpression combinedQuery;
                if (!queries.isEmpty()) {
                    combinedQuery = Searches.combine(NaryLogicalOperator._or, queries);
                    LOGGER.debug("Combined retrieval query:{}", combinedQuery);
                } else {
                    combinedQuery = null;
                }
                List<ResultDocument> destResults = dest.getResultList(combinedQuery, ctx);
                for (DocAndQ parentDocAndQ : docs) {
                    associateDocs(parentDocAndQ.doc, destResults, aq);
                }
            }
            docs = new ArrayList<>();
            queries = new ArrayList<>();
        }
    }
    
    private JsonNode toJson(Step.ToJsonCb<Step> scb,Step.ToJsonCb<ExecutionBlock> bcb) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        ObjectNode a = JsonNodeFactory.instance.objectNode();
        o.set("assemble", a);
        a.set("entity", JsonNodeFactory.instance.textNode(block.getMetadata().getName()));
        a.set("left", scb.toJson(source.getStep()));
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        a.set("right", array);
        for (ExecutionBlock b : destinationBlocks) {
            ObjectNode detail=JsonNodeFactory.instance.objectNode();
            AssociationQuery aq = b.getAssociationQueryForEdge(block);
            detail.set("associationQuery",aq.getQuery().toJson());
            detail.set("source",bcb.toJson(b));
            array.add(detail);
        }
        return o;
    }

    /**
     * Associates child documents obtained from 'aq' to all the slots in the
     * parent document
     */
    public static void associateDocs(ResultDocument parentDoc,
                                     List<ResultDocument> childDocs,
                                     AssociationQuery aq) {
        List<ChildSlot> slots = parentDoc.getSlots().get(aq.getReference());
        for (ChildSlot slot : slots) {
            associateDocs(parentDoc, slot, childDocs, aq);
        }
    }

    /**
     * Associate child documents with their parents. The association query is
     * for the association from the child to the parent, so caller must flip it
     * before sending it in if necessary. The caller also make sure parentDocs
     * is a unique stream.
     *
     * @param parentDoc The parent document
     * @param parentSlot The slot in parent docuemnt to which the results will
     * be attached
     * @param childDocs The child documents
     * @param aq The association query from parent to child. This may not be the
     * same association query between the blocks. If the child block is before
     * the parent block, a new aq must be constructed for the association from
     * the parent to the child
     */
    public static void associateDocs(ResultDocument parentDoc,
                                     ChildSlot parentSlot,
                                     List<ResultDocument> childDocs,
                                     AssociationQuery aq) {
        if (!childDocs.isEmpty()) {
            LOGGER.debug("Associating docs");
            ExecutionBlock childBlock = childDocs.get(0).getBlock();
            ArrayNode destNode = (ArrayNode) parentDoc.getDoc().get(parentSlot.getSlotFieldName());
            BindQuery binders = parentDoc.getBindersForSlot(parentSlot, aq);
            // No binders means all child docs will be added to the parent            
            if (binders.getBindings().isEmpty()) {
                if (destNode == null) {
                    destNode = JsonNodeFactory.instance.arrayNode();
                    parentDoc.getDoc().modify(parentSlot.getSlotFieldName(), destNode, true);
                }
                for (ResultDocument d : childDocs) {
                    destNode.add(d.getDoc().getRoot());
                }
            } else {
                QueryExpression boundQuery = binders.iterate(aq.getQuery());
                LOGGER.debug("Association query:{}", boundQuery);
                QueryEvaluator qeval = QueryEvaluator.getInstance(boundQuery, childBlock.getMetadata());
                for (ResultDocument childDoc : childDocs) {
                    if (qeval.evaluate(childDoc.getDoc()).getResult()) {
                        if (destNode == null) {
                            destNode = JsonNodeFactory.instance.arrayNode();
                            parentDoc.getDoc().modify(parentSlot.getSlotFieldName(), destNode, true);
                        }
                        destNode.add(childDoc.getDoc().getRoot());
                    }
                }
            }
        }
    }

    
    @Override
    public JsonNode toJson() {
        return toJson(Step::toJson,ExecutionBlock::toJson);
    }

    @Override
    public JsonNode explain(ExecutionContext ctx) {
        return toJson(s->{return s.explain(ctx);},
                      t->{return t.explain(ctx);});
    }

}
