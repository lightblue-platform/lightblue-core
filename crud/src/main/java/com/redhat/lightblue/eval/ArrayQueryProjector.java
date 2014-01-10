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
package com.redhat.lightblue.eval;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;

import com.redhat.lightblue.query.ArrayQueryMatchProjection;

/**
 * Projector to return array elements that match a query
 */
public class ArrayQueryProjector extends ArrayProjector {

    private final QueryEvaluator query;

    /**
     * Ctor
     *
     * @param p The projection expression
     * @param ctxPath The absolute path relative to which this is to be interpreted
     * @param context The metadata node at which this is to be interpreted
     */
    public ArrayQueryProjector(ArrayQueryMatchProjection p, Path ctxPath, FieldTreeNode context) {
        super(p, ctxPath, context);
        FieldTreeNode nestedCtx = context.resolve(p.getField());
        query = QueryEvaluator.getInstance(p.getMatch(), ((ArrayField) nestedCtx).getElement());
    }

    @Override
    protected Boolean projectArray(Path p, QueryEvaluationContext ctx) {
        Path contextRoot = ctx.getPath();
        QueryEvaluationContext nestedContext = ctx.getNestedContext(contextRoot.isEmpty() ? p
                : p.suffix(-contextRoot.numSegments()));
        if (query.evaluate(nestedContext)) {
            lastMatch = true;
            return include ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }
}
