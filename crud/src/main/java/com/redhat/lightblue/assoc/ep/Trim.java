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
 * Modifies the documents to contain only the root entity. This only
 * works for root
 *
 * Input: Step<ResultDocument>
 * Output: Step<ResultDocument>
 */
public class Trim extends Step<ResultDocument> {

    private final Step<ResultDocument> source;
    
    public Trim(ExecutionBlock block,Step<ResultDocument> source) {
        super(block);
        this.source=source;
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        // return new StepResultWrapper<ResultDocument>(source.getResults(ctx)) {
        //     @Override
        //     public Stream<ResultDocument> stream() {
        //         return super.stream().map(ResultDocument::trim);
        //     }
        // };
        return null;
    }           

    @Override
    public JsonNode toJson() {
        ObjectNode o=JsonNodeFactory.instance.objectNode();
        o.set("strip",source.toJson());
        return o;
    }
}
