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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.util.Path;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;

import com.redhat.lightblue.query.Projection;

import com.redhat.lightblue.query.ArrayQueryMatchProjection;

/**
 * Projector to return array elements that match a query
 */
public class ArrayQueryProjector extends ArrayProjector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayQueryProjector.class);

    private final QueryEvaluator query;

    /**
     * Ctor
     *
     * @param p The projection expression
     * @param ctxPath The absolute path relative to which this is to be
     * interpreted
     * @param context The metadata node at which this is to be interpreted
     */
    public ArrayQueryProjector(ArrayQueryMatchProjection p, Path ctxPath, FieldTreeNode context) {
        super(p, ctxPath, context);
        FieldTreeNode nestedCtx = context.resolve(p.getField());
        query = QueryEvaluator.getInstance(p.getMatch(), ((ArrayField) nestedCtx).getElement());
    }

    @Override
    protected Projection.Inclusion projectArray(Path p, QueryEvaluationContext ctx) {
        LOGGER.debug("Evaluating array query projection for {}", p);
        Path contextRoot = ctx.getPath();
        QueryEvaluationContext nestedContext = ctx.getNestedContext(contextRoot.isEmpty() ? p
                : p.suffix(-contextRoot.numSegments()));
        if (query.evaluate(nestedContext)) {
            LOGGER.debug("query evaluates to true");
            return isIncluded() ? Projection.Inclusion.explicit_inclusion : Projection.Inclusion.explicit_exclusion;
        }
        return isIncluded() ? Projection.Inclusion.explicit_exclusion : Projection.Inclusion.explicit_inclusion;
    }
}
