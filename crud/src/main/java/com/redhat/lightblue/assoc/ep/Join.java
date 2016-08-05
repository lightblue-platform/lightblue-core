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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.ResolvedReferenceField;

import com.redhat.lightblue.util.Tuples;

/**
 * Given n source steps, returns n-tuples containing the documents from those
 * steps
 *
 * Input: Result documents from multiple sources Output: List [ ResultDocument
 * ], each element of the list is a ResultDocument from the corresponding source
 */
public class Join extends Step<JoinTuple> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Join.class);

    private final Source<ResultDocument>[] sources;

    /**
     * Construct the join with the given sources
     */
    public Join(ExecutionBlock block, Source<ResultDocument>[] sources) {
        super(block);
        this.sources = sources;
    }

    /**
     * Returns the sources of the join
     */
    public Source<ResultDocument>[] getSources() {
        return sources;
    }

    /**
     * Asynchronously retrieves results from the sources, and returns a stream
     * that joins them
     */
    @Override
    public StepResult<JoinTuple> getResults(ExecutionContext ctx) {
        // One of the parent blocks can be a parent document block, separate that out
        int parentIndex = -1;
        // get all document streams from result steps
        Future<StepResult<ResultDocument>>[] futureResults = new Future[sources.length];
        int i = 0;
        for (Source<ResultDocument> source : sources) {
            if (source.getStep().getBlock().getMetadata() == block.getMetadata().getParent()) {
                parentIndex = i;
            }
            futureResults[i++] = ctx.getExecutor().submit(() -> {
                return source.getStep().getResults(ctx);
            });
        }

        Tuples<ResultDocument> tuples = new Tuples();
        i = 0;
        for (Future<StepResult<ResultDocument>> futureResult : futureResults) {
            tuples.add(() -> {
                try {
                    return futureResult.get().stream().iterator();
                } catch (InterruptedException x) {
                    throw new RuntimeException(x);
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
            );
        }
        if (ctx.hasErrors()) {
            return StepResult.EMPTY;
        }
        return new JoinStream(tuples, parentIndex, parentIndex == -1 ? null
                : block.getAssociationQueryForEdge(sources[parentIndex].getBlock()).
                getReference());
    }

    private static class JoinStream implements StepResult<JoinTuple> {
        private final Tuples<ResultDocument> tuples;
        private final int parentIndex;
        private final ResolvedReferenceField parentReference;

        public JoinStream(Tuples<ResultDocument> tuples,
                          int parentIndex,
                          ResolvedReferenceField parentReference) {
            this.tuples = tuples;
            this.parentIndex = parentIndex;
            this.parentReference = parentReference;
        }

        @Override
        public Stream<JoinTuple> stream() {
            Iterable<JoinTuple> itr = () -> new JoinTupleIterator(tuples.tuples(), parentIndex, parentReference);
            return StreamSupport.stream(itr.spliterator(), false);
        }
    }

    /**
     * Converts an iterator over doc tuples to an iterator over join tuples. The
     * difference is that if there is a source block that is a parent document
     * of this block, then the slots of that parent doc is iterated.
     */
    private static class JoinTupleIterator implements Iterator {
        private final Iterator<List<ResultDocument>> tuples;
        private final int parentIndex;
        private final ResolvedReferenceField reference;

        private List<ResultDocument> currentTuple;
        private Iterator<JoinTuple> joinTuples;
        private JoinTuple nextTuple;

        public JoinTupleIterator(Iterator<List<ResultDocument>> tuples,
                                 int parentIndex,
                                 ResolvedReferenceField reference) {
            this.tuples = tuples;
            this.parentIndex = parentIndex;
            this.reference = reference;
        }

        @Override
        public boolean hasNext() {
            if (nextTuple == null) {
                nextTuple = getNext();
            }
            return nextTuple != null;
        }

        public JoinTuple next() {
            if (nextTuple == null) {
                nextTuple = getNext();
            }
            if (nextTuple == null) {
                throw new NoSuchElementException();
            }
            JoinTuple ret = nextTuple;
            nextTuple = null;
            return ret;
        }

        private JoinTuple getNext() {
            do {
                if (joinTuples == null) {
                    seekNextDoc();
                    if (joinTuples == null) {
                        return null;
                    }
                }
                if (joinTuples.hasNext()) {
                    return joinTuples.next();
                } else {
                    joinTuples = null;
                }
            } while (true);

        }

        /**
         * Moves to the next document tuple in tuples
         */
        private void seekNextDoc() {
            currentTuple = null;
            joinTuples = null;
            if (tuples.hasNext()) {
                currentTuple = tuples.next();
                computeJoinTuples();
            }
        }

        /**
         * Computes a new list of join tuples based on current tuple
         */
        private void computeJoinTuples() {
            List<JoinTuple> l = new ArrayList<>();
            if (parentIndex != -1) {
                ResultDocument parentDoc = currentTuple.get(parentIndex);
                List<ChildSlot> slots = parentDoc.getSlots().get(reference);
                List<ResultDocument> childDocs = new ArrayList<>(currentTuple.size());
                for (ResultDocument doc : currentTuple) {
                    if (doc != parentDoc) {
                        childDocs.add(doc);
                    }
                }
                if (slots != null) {
                    for (ChildSlot slot : slots) {
                        l.add(new JoinTuple(parentDoc, slot, childDocs));
                    }
                }
            } else {
                l.add(new JoinTuple(null, null, currentTuple));
            }
            joinTuples = l.iterator();
        }
    }

    private JsonNode toJson(ToJsonCb<Step> cb) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        for (Source<ResultDocument> s : sources) {
            arr.add(cb.toJson(s.getStep()));
        }
        o.set("join", arr);
        return o;
    }

    @Override
    public JsonNode toJson() {
        return toJson(Step::toJson);
    }

    @Override
    public JsonNode explain(ExecutionContext ctx) {
        return toJson(s->{return s.explain(ctx);});
    }
}
