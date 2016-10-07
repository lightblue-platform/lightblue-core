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

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import java.util.stream.Stream;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.metadata.DocId;
import com.redhat.lightblue.metadata.DocIdExtractor;

import com.redhat.lightblue.util.Path;

/**
 * Filters the result set to include only unique documents by id
 */
public class Unique extends Step<ResultDocument> {

    private final DocIdExtractor idx;
    private final Source<ResultDocument> source;
    private boolean recordResultSetSize=false;

    public Unique(ExecutionBlock block, Source<ResultDocument> source) {
        super(block);
        this.source = source;
        this.idx = block.getIdExtractor();
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        // If we need to count the result set size, then we have to store and forward, we can't stream
        // Because any limit() added to the stream will prevent iteration through the resultset.
        StepResult<ResultDocument> result=new StepResultWrapper<ResultDocument>(source.getStep().getResults(ctx)) {
                @Override
                public Stream<ResultDocument> stream() {
                    return super.stream().filter(new Predicate<ResultDocument>() {
                            private final Set<DocId> uniqueIds = new HashSet<>();
                            
                            @Override
                            public boolean test(ResultDocument doc) {
                                return uniqueIds.add(doc.getDocId());
                            }
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

    public void setRecordResultSetSize(boolean b) {
        recordResultSetSize = b;
    }

    private JsonNode toJson(ToJsonCb<Step> cb) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        ArrayNode arr = JsonNodeFactory.instance.arrayNode();
        for (Path p : idx.getIdentityFields()) {
            arr.add(JsonNodeFactory.instance.textNode(p.toString()));
        }
        o.set("unique", arr);
        o.set("source", cb.toJson(source.getStep()));
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
