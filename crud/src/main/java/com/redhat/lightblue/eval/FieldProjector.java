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

import com.redhat.lightblue.query.FieldProjection;

import com.redhat.lightblue.metadata.FieldTreeNode;

public class FieldProjector extends Projector {

    private final Path field;
    private final boolean include;
    private final boolean recursive;

    private boolean exactMatch=false;

    public FieldProjector(FieldProjection p, Path ctxPath, FieldTreeNode ctx) {
        super(ctxPath, ctx);
        field = new Path(ctxPath, p.getField());
        include = p.isInclude();
        recursive = p.isRecursive();
    }

    @Override
    public Projector getNestedProjector() {
        return null;
    }

    @Override
    public boolean exactMatch() {
        return exactMatch;
    }

    @Override
    public Boolean project(Path p, QueryEvaluationContext ctx) {
        exactMatch=false;
        if (p.matchingPrefix(field)) {
            // If this is true, we're checking an ancestor of the
            // projection field, or the projection field itself, but
            // not a field that is a descendant of the projection
            // field
            if (include) {
                exactMatch=true;
                return Boolean.TRUE;
                // Inclusion implies, because if we're going to
                // include a descendant of this field, this field
                // should also be included
            } else if (p.equals(field)) {
                exactMatch=true;
                return Boolean.FALSE;
                // If this field is exclusively excluded, exclude it
            }
            // Otherwise, this projection does not tell anything about this particular field.
        } else if (recursive &&  // If this is a recursive projection
                   p.numSegments() > field.numSegments() &&   // If we're checking a field deeper than our field
                   p.prefix(field.numSegments()).matches(field) // And if we're checking a field under the subtree of our field
                   ) {
            // This is an implied inclusion or exclusion, because the
            // projection is for an ancestor of this field.
            return include ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }
}
