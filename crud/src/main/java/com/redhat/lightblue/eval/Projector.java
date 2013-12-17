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

import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.FieldProjection;
import com.redhat.lightblue.query.ProjectionList;
import com.redhat.lightblue.query.ArrayMatchingElementsProjection;
import com.redhat.lightblue.query.ArrayRangeProjection;
import com.redhat.lightblue.query.ArrayQueryMatchProjection;

/**
 * This class decides whether a path needs to be projected or not
 */
public abstract class Projector {

    /**
     * Returns the nested projector for this path *only if*
     * <code>project</code> returns true. May return null, which means
     * to continue using this projector.
     */
    public abstract Projector getNestedProjector();

    /**
     * Returns true, false, or null if the result cannot be determined.
     *
     * @param p The absolute field path
     * @param ctx Query evaluation context
     */
    public abstract Boolean project(Path p,QueryEvaluationContext ctx);

    public static Projector getInstance(Projection projection,EntityMetadata md) {
        return getInstance(projection,Path.EMPTY,md.getFieldTreeRoot());
    }

    public static Projector getInstance(Projection projection,Path ctxPath,FieldTreeNode ctx) {
        if(projection instanceof FieldProjection) 
            return new FieldProjector((FieldProjection)projection,ctxPath);
        else if(projection instanceof ProjectionList)
            return new ListProjector((ProjectionList)projection,ctxPath,ctx);
        else if(projection instanceof ArrayMatchingElementsProjection) 
            return new ArrayMatchingElementsProjector((ArrayMatchingElementsProjection)projection,ctxPath,ctx);
        else if(projection instanceof ArrayRangeProjection) 
            return new ArrayRangeProjector((ArrayRangeProjection)projection,ctxPath,ctx);
        else 
            return new ArrayQueryProjector((ArrayQueryMatchProjection)projection,ctxPath,ctx);
    }

}
