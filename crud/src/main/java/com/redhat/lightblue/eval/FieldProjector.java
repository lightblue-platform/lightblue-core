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
    public Boolean project(Path p, QueryEvaluationContext ctx) {
        if (p.matchingPrefix(field)) {
            if (include) {
                return Boolean.TRUE;
            } else if (p.matches(field)) {
                return Boolean.FALSE;
            }
        } else if (recursive && p.numSegments() > field.numSegments() && p.prefix(field.numSegments()).matches(field)) {
            return include ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
    }
}
