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

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Limits the resultset to at most n
 */
public class Limit<T> extends Step<T> {

    private final int limit;
    private final Source<T> source;

    public Limit(ExecutionBlock block, int n, Source<T> source) {
        super(block);
        this.source = source;
        limit = n;
    }

    @Override
    public StepResult<T> getResults(ExecutionContext ctx) {
        return new StepResultWrapper<T>(source.getStep().getResults(ctx)) {
            @Override
            public Stream<T> stream() {
                return super.stream().limit(limit);
            }
        };
    }

    @Override
    public JsonNode toJson() {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.set("limit", JsonNodeFactory.instance.numberNode(limit));
        o.set("source", source.getStep().toJson());
        return o;
    }
}
