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

/**
 * Copies the results from another step
 */
public class Copy extends AbstractSearchStep {

    private final Step<ResultDocument> source;
    
    public Copy(ExecutionBlock block,Step<ResultDocument> source) {
        super(block);
        this.source=source;
    }

    @Override
    public StepResult<ResultDocument> getResults(ExecutionContext ctx) {
        return source.getResults(ctx);
    }

    @Override
    protected final List<ResultDocument> getSearchResults(ExecutionContext ctx) {
        return null;
    }
}
