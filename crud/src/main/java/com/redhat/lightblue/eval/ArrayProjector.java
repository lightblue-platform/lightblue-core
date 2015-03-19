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
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.query.CompositeSortKey;
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

    private final static class SortFieldInfo {
        final SimpleField field;
        final Path name;
        final boolean descending;

        SortFieldInfo(SimpleField field,Path name,boolean descending) {
            this.field=field;
            this.name=name;
            this.descending=descending;
        }
    }

    private final static class SortableElement implements Comparable<SortableElement> {
        final Object[] keyValues;
        final JsonNode node;
        final SortFieldInfo[] sortFields;

        public SortableElement(JsonNode node,SortFieldInfo[] sortFields) {
            this.node=node;
            this.sortFields=sortFields;
            keyValues=new Object[sortFields.length];
            for(int i=0;i<sortFields.length;i++) {
                JsonNode valueNode=JsonDoc.get(node,sortFields[i].name);
                keyValues[i]=sortFields[i].field.getType().fromJson(valueNode);
            }
        }

        @Override
        public boolean equals(Object x) {
            if(x instanceof SortableElement)
                return compareTo((SortableElement)x)==0;
            else
                return false;
        }

        @Override
        public int hashCode() {
            StringBuilder buff = new StringBuilder();

            for(int i=0;i<keyValues.length;i++) {
                buff.append(sortFields[i].descending?"-":"+");
                if(keyValues[i]!=null) {
                    buff.append("_");
                } else {
                    buff.append(sortFields[i].field.getType());
                }
            }

            return buff.toString().hashCode();
        }

        @Override
        public int compareTo(SortableElement el) {
            for(int i=0;i<keyValues.length;i++) {
                int dir=sortFields[i].descending?-1:1;
                if(keyValues[i]==null) {
                    if(el.keyValues[i]==null) {
                        ;
                    } else {
                        return -1*dir;
                    }
                } else {
                    if(el.keyValues[i]==null) {
                        return 1*dir;
                    } else {
                        int result=sortFields[i].field.getType().compare(keyValues[i],el.keyValues[i]);
                        if(result!=0)
                            return result*dir;
                    }
                }
            }
            return 0;
        }
    }
        

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
                sortFields=buildSortFields(sort,((ArrayField)nestedCtx).getElement());
            } else {
                sortFields=null;
            }
        } else {
            throw new EvaluationError(CrudConstants.ERR_EXPECTED_ARRAY_ELEMENT + arrayFieldPattern);
        }
    }

    private SortFieldInfo[] buildSortFields(Sort sort,FieldTreeNode context) {
        if(sort instanceof SortKey) {
            return new SortFieldInfo[] {getSortField(((SortKey)sort).getField(),context,((SortKey)sort).isDesc())};
        } else {
            List<SortKey> keys=((CompositeSortKey)sort).getKeys();
            SortFieldInfo[] arr=new SortFieldInfo[ keys.size() ];
            int i=0;
            for(SortKey key:keys) {
                arr[i]=getSortField(key.getField(),context,key.isDesc());
            }
            return arr;
        }
    }

    private SortFieldInfo getSortField(Path field,FieldTreeNode context,boolean descending) {
        FieldTreeNode fieldMd=context.resolve(field);
        if(! (fieldMd instanceof SimpleField) ) {
            throw new EvaluationError(CrudConstants.ERR_EXPECTED_VALUE+":"+field);
        }
        return new SortFieldInfo((SimpleField)fieldMd,field,descending);
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
    public Boolean project(Path p, QueryEvaluationContext ctx) {
        lastMatch=false;
        LOGGER.debug("Evaluating array projection for {}, arrayField={}",p,arrayFieldPattern);
        // Is this field pointing to an element of the array
        // It is so if 'p' has one more element than 'arrayFieldPattern', and
        // if it is a matching descendant
        if (p.numSegments() == arrayFieldPattern.numSegments() + 1 && p.matchingDescendant(arrayFieldPattern)) {
            Boolean ret=projectArray(p, ctx);
            LOGGER.debug("Projecting array element {}:{}",p,ret);
            lastMatch=ret!=null&&ret;
            return ret;
        }
        return null;
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
            List<SortableElement> list=new ArrayList<>(array.size());
            for(Iterator<JsonNode> itr=array.elements();itr.hasNext();) {
                list.add(new SortableElement(itr.next(),sortFields));
            }
            Collections.sort(list);
            ArrayNode newNode=factory.arrayNode();
            for(SortableElement x:list)
                newNode.add(x.node);
            return newNode;
        }
    }

    /**
     * Check if the array element matches. This is called after determining that
     * the path points to a field that can be interpreted by this projector.
     */
    protected abstract Boolean projectArray(Path p, QueryEvaluationContext ctx);
}
