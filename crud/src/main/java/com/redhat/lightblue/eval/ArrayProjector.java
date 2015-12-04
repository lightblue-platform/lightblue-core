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

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.SimpleField;
import com.redhat.lightblue.query.ArrayProjection;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.JsonDoc;

/**
 * Base class for array projectors.
 */
public abstract class ArrayProjector extends Projector {

    private static final Logger LOGGER=LoggerFactory.getLogger(ArrayProjector.class);

    private final Path arrayFieldPattern;
    private final boolean include;
    private final Projector nestedProjector;
    private final Sort sort;
    private boolean lastMatch;
    private final SortFieldInfo[] sortFields;        

    protected boolean isIncluded() {
        return include;
    }

    public Path getArrayFieldPattern() {
        return arrayFieldPattern;
    }

    /**
     * Sets up the projector context
     */
    public ArrayProjector(ArrayProjection p, Path ctxPath, FieldTreeNode context) {
        super(ctxPath, context);
        sort=p.getSort();
        arrayFieldPattern = new Path(ctxPath, p.getField());
        include = p.isInclude();
        FieldTreeNode nestedCtx = context.resolve(p.getField());
        if (nestedCtx instanceof ArrayField) {
            nestedProjector = Projector.getInstance(p.getProject(), new Path(arrayFieldPattern, Path.ANYPATH), ((ArrayField) nestedCtx).getElement());
            if(sort!=null) {
                sortFields=SortFieldInfo.buildSortFields(sort,((ArrayField)nestedCtx).getElement());
            } else {
                sortFields=null;
            }
        } else {
            throw new EvaluationError(CrudConstants.ERR_EXPECTED_ARRAY_ELEMENT + arrayFieldPattern);
        }
    }

    public Sort getSort() {
        return sort;
    }

    /**
     * Returns the nested projector 
     */
    @Override
    public Projector getNestedProjector() {
        return lastMatch?nestedProjector:null;
    }

    @Override
    public Projection.Inclusion project(Path p, QueryEvaluationContext ctx) {
        lastMatch=false;
        LOGGER.debug("Evaluating array projection for {}, arrayField={}",p,arrayFieldPattern);
        // Is this field pointing to an element of the array
        // It is so if 'p' has one more element than 'arrayFieldPattern', and
        // if it is a matching descendant
        if (p.numSegments() == arrayFieldPattern.numSegments() + 1 && p.matchingDescendant(arrayFieldPattern)) {
            Projection.Inclusion ret=projectArray(p, ctx);
            LOGGER.debug("Projecting array element {}:{}",p,ret);
            lastMatch=ret==Projection.Inclusion.implicit_inclusion||ret==Projection.Inclusion.explicit_inclusion;
            return ret;
        }
        return Projection.Inclusion.undecided;
    }

    /**
     * Sorts the given array node using the sort criteria given in this ArrayProjector
     *
     * @param array The array node to sort
     * @param factory Json node factory
     *
     * If there is a sort criteria defined in <code>this</code>, the array elements are
     * sorted using that.
     *
     * @return A new ArrayNode containing the sorted elements, or if
     * there is no sort defined, the <code>array</code> itself
     */
    public ArrayNode sortArray(ArrayNode array,JsonNodeFactory factory) {
        if(sort==null) {
            return array;
        } else {
            List<SortableItem> list=new ArrayList<>(array.size());
            for(Iterator<JsonNode> itr=array.elements();itr.hasNext();) {
                list.add(new SortableItem(itr.next(),sortFields));
            }
            Collections.sort(list);
            ArrayNode newNode=factory.arrayNode();
            for(SortableItem x:list)
                newNode.add(x.getNode());
            return newNode;
        }
    }

    /**
     * Check if the array element matches. This is called after determining that
     * the path points to a field that can be interpreted by this projector.
     */
    protected abstract Projection.Inclusion projectArray(Path p, QueryEvaluationContext ctx);
}
