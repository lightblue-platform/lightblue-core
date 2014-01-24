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

import com.redhat.lightblue.query.ArrayProjection;

/**
 * Base class for array projectors.
 */
public abstract class ArrayProjector extends Projector {
    private final Path arrayFieldPattern;
    private final boolean include;
    private final Projector nestedProjector;
    private boolean lastMatch;

    protected boolean isLastMatch() {
        return lastMatch;
    }
    
    protected void setLastMatch(boolean lastMatch) {
        this.lastMatch = lastMatch;
    }
    
    protected boolean isIncluded() {
        return include;
    }
    
    /**
     * Sets up the projector context
     */
    public ArrayProjector(ArrayProjection p, Path ctxPath, FieldTreeNode context) {
        super(ctxPath, context);
        arrayFieldPattern = new Path(ctxPath, p.getField());
        include = p.isInclude();
        FieldTreeNode nestedCtx = context.resolve(p.getField());
        if (nestedCtx instanceof ArrayField) {
            nestedProjector = Projector.getInstance(p.getProject(),
                    new Path(arrayFieldPattern, Path.ANYPATH),
                    ((ArrayField) nestedCtx).getElement());
        } else {
            throw new EvaluationError("Expecting array element for " + arrayFieldPattern);
        }
    }

    /**
     * Returns the nested projector if the last projection is a match
     */
    @Override
    public Projector getNestedProjector() {
        return lastMatch ? nestedProjector : null;
    }

    @Override
    public Boolean project(Path p, QueryEvaluationContext ctx) {
        lastMatch = false;
        if (p.matchingPrefix(arrayFieldPattern)) {
            return include ? Boolean.TRUE : Boolean.FALSE;
        }
        // Is this field pointing to an element of the array
        // It is so if 'p' has one more element than 'arrayFieldPattern', and
        // if it is a matching descendant
        if (p.numSegments() == arrayFieldPattern.numSegments() + 1
                && p.matchingDescendant(arrayFieldPattern)) {
            return projectArray(p, ctx);
        }
        return null;
    }

    /**
     * Check if the array element matches. This is called after determining that the path points to a field that can be
     * interpreted by this projector.
     */
    protected abstract Boolean projectArray(Path p, QueryEvaluationContext ctx);
}
