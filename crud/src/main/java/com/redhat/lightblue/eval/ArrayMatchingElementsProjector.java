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

import com.redhat.lightblue.query.ArrayMatchingElementsProjection;

/**
 * Projects the elements of an array that match the search query
 */
public class ArrayMatchingElementsProjector extends ArrayProjector {

    /**
     * Ctor
     *
     * @param p The projection expression
     * @param ctxPath The absolute path relative to which this is to be interpreted
     * @param context The metadata node at which this is to be interpreted
     */
    public ArrayMatchingElementsProjector(ArrayMatchingElementsProjection p, Path ctxPath, FieldTreeNode ctx) {
        super(p, ctxPath, ctx);
    }

    @Override
    protected Boolean projectArray(Path p, QueryEvaluationContext ctx) {
        if (ctx.isMatchingElement(p)) {
            setLastMatch(true);
            return isIncluded() ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }
}
