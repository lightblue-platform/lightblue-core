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

import com.redhat.lightblue.eval.SortFieldInfo;
import com.redhat.lightblue.eval.SortableItem;

/**
 * Sorts the result set
 */
public class Sort extends Step<ResultDocument> {

    private final SortFieldInfo[] sortFields;
    private final Sort sort;
    private final Step<ResultDocument> source;
    
    public Sort(ExecutionBlock block,Step<ResultDocument> source,Sort sort) {
        super(block);
        this.source=source;
        this.sort=sort;
        this.sortFields=SortFieldInfo.buildSortFields(sort,block.getMetadata());
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        return new StepResultWrapper<ResultDocument>(source.getResults(ctx)) {
            @Override
            public Stream<ResultDocument> stream() {
                return super.stream().
                    map(d->new SortableDoc(d,sortFields)).
                    sort().
                    map(d->getDoc());
        }
        };        
    }

    private static class SortableDoc extends SortableItem {
        private final ResultDocument doc;
        public SortableDoc(ResultDocument doc,SortFieldInfo[] fields) {
            super(doc.getDoc().getRoot(),fields);
            this.doc=doc;
        }
        public ResultDocument getDoc() {
            return doc;
        }
    }

    @Override
    public JsonNode toJson() {
        ObjectNode o=JsonNodeFactory.instance.objectNode();
        o.set("sort",sort.toJson());
        o.set("source",source.toJson());
        return o;
    }
}
