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
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * Copies the results from another step
 */
public class Copy extends AbstractSearchStep {

    private final Source<ResultDocument> source;

    public Copy(ExecutionBlock block, Source<ResultDocument> source) {
        super(block);
        this.source = source;
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        StepResult<ResultDocument> result=new StepResultWrapper<ResultDocument>(source.getStep().getResults(ctx)) {
                @Override
                public Stream<ResultDocument> stream() {
                    // Create new documents for each document in the source. This will
                    // create the correct slots based on this execution block
                    return super.stream().map(d -> {
                            return new ResultDocument(block, d.getDoc());
                        });
                }
            };
        if(recordResultSetSize) {
            List<ResultDocument> list=result.stream().collect(Collectors.toList());
            result=new ListStepResult<ResultDocument>(list);
            ctx.setMatchCount(list.size());
        }
        return result;
    }

    @Override
    protected final List<ResultDocument> getSearchResults(ExecutionContext ctx) {
        // This should be called at all
        throw new IllegalStateException();
    }

    @Override
    public JsonNode toJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("copy", source.getStep().toJson());
        return node;
    }

    @Override
    public JsonNode explain(ExecutionContext ctx) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set("copy", source.getStep().explain(ctx));
        return node;
    }
}
