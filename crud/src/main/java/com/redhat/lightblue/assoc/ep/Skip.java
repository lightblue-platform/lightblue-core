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
 * Skips the first n results in a stream. 
 *
 * Input: Step<T>
 * Output: Step<T>
 */
public class Skip<T> extends Step<T> {

    private final int skip;
    private final Step<T> source;
    
    public Skip(ExecutionBlock block,int n,Step<T> source) {
        super(block);
        this.source=source;
        skip=n;
    }

    @Override
    public StepResult<T> getResults(ExecutionContext ctx) {
        return new StepResultWrapper<T>(source.getResults(ctx)) {
            @Override
            public Stream<T> stream() {
                return super.stream().skip(skip);
            }
        };
    }           

    @Override
    public JsonNode toJson() {
        ObjectNode o=JsonNodeFactory.instance.objectNode();
        o.set("skip",JsonNodeFactory.instance.numberNode(skip));
        o.set("source",source.toJson());
        return o;
    }
}
