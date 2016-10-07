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
import java.util.stream.Collectors;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.redhat.lightblue.eval.QueryEvaluator;
import com.redhat.lightblue.query.QueryExpression;

/**
 * Filters the result set based on a query
 */
public class Filter extends Step<ResultDocument> {

    private final QueryEvaluator qe;
    private final QueryExpression q;
    private final Source<ResultDocument> source;
    private boolean recordResultSetSize=false;

    public Filter(ExecutionBlock block, Source<ResultDocument> source, QueryExpression q) {
        super(block);
        this.source = source;
        this.q = q;
        this.qe = QueryEvaluator.getInstance(q, block.getMetadata());
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        StepResult<ResultDocument> result=new StepResultWrapper<ResultDocument>(source.getStep().getResults(ctx)) {
                @Override
                public Stream<ResultDocument> stream() {
                    return super.stream().filter(doc -> {
                            boolean ret=qe.evaluate(doc.getDoc()).getResult();
                            if(ret&&recordResultSetSize)
                                ctx.setMatchCount(ctx.getMatchCount()+1);		
                            return ret;
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
    	recordResultSetSize=b;
    }

    @Override
    public JsonNode toJson() {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.set("filter", q.toJson());
        o.set("source", source.getStep().toJson());
        return o;
    }

    @Override
    public JsonNode explain(ExecutionContext ctx) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.set("filter", q.toJson());
        o.set("source", source.getStep().explain(ctx));
        return o;
    }
}
