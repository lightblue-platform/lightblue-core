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

public class ArrayMatchingElementsProjector extends Projector {

    private final Path arrayFieldPattern;
    private final boolean include;
    private final Projector nestedProjector;

    public ArrayMatchingElementsProjector(ArrayMatchingElementsProjection p,FieldTreeNode ctx) {
        arrayFieldPattern=p.getField();
        include=p.isInclude();
        nestedProjector=Projector.getInstance(p.getProject(),ctx);
    }

    @Override
    public Projector getNestedProjector() {
        return nestedProjector;
    }

    @Override
    public Boolean project(Path p,QueryEvaluationContext ctx) {
        // Is this field pointing to an element of the array
        // It is so if 'p' has one more element than 'arrayFieldPattern', and
        // if it is a matching descendant
        if(p.numSegments()==arrayFieldPattern.numSegments()+1&&
           p.matchingDescendant(arrayFieldPattern)) {            
            if(ctx.isMatchingElement(p))
                return include?Boolean.TRUE:Boolean.FALSE;
        } 
        return null;
    }
}
