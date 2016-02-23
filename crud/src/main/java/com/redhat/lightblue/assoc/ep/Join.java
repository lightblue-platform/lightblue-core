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

import com.redhat.lightblue.util.Tuples;

/**
 * Given n source steps, returns n-tuples containing the documents from those steps
 *
 * Input: Result documents from multiple sources
 * Output: List [ ResultDocument ], each element of the list is a ResultDocument 
 * from the corresponding source
 */
public class Join extends Step<List<ResultDocument>> {

    private static final Logger LOGGER=LoggerFactory.getLogger(Join.class);
    
    private final Step<ResultDocument>[] sources;

    /**
     * Construct the join with the given sources
     */
    public Join(ExecutionBlock block,Step<ResultDocument>[] sources) {
        super(block);
        this.sources=sources;
    }

    /**
     * Returns the sources of the join
     */
    public Step<ResultDocument>[] getSources() {
        return sources;
    }

    /**
     * Asynchronously retrieves results from the sources, and returns
     * a stream that joins them
     */
    @Override
    public StepResult<List<ResultDocument>> getResults(ExecutionContext ctx) {
        // get all document streams from result steps
        Future<StepResult<ResultDocument>> [] futureResults=new Future[sources.length];
        int i=0;
        for(Step<ResultDocument> source:sources) {
            futureResults[i++]=ctx.getExecutor().submit(() -> source.getResults(ctx));
        }
        
        Tuples<ResultDocument> tuples=new Tuples();
        i=0;
        for(Future<StepResult<ResultDocument>> futureResult:futureResults) {
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
        return new JoinStream(tuples);
    }

       
    private class JoinStream implements StepResult<List<ResultDocument>> {
        private final Tuples<ResultDocument> tuples;

        public JoinStream(Tuples<ResultDocument> tuples) {
            this.tuples=tuples;
        }

        @Override
        public Stream<List<ResultDocument>> stream() {
            Iterable<List<ResultDocument>> itr=() -> tuples.tuples();
            return StreamSupport.stream(itr.spliterator(),false);
        }
    }

    @Override
    public JsonNode toJson() {
        ObjectNode o=JsonNodeFactory.instance.objectNode();
        ArrayNode arr=JsonNodeFactory.instance.arrayNode();
        for(Step<ResultDocument> s:sources) {
            arr.add(s.toJson());
        }
        o.set("join",arr);
        return o;
    }
}

