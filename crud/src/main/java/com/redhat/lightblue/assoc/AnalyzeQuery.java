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
package com.redhat.lightblue.assoc;

import java.util.ArrayList;
import java.util.List;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayElement;

import com.redhat.lightblue.query.*;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.MutablePath;
import com.redhat.lightblue.util.Error;

/**
 * Analyzes a query to collect field information. For each field in a query, these are collected:
 * <ul>
 * <li>fieldNameInClause: Name of the field as it appears in the smallest clause containing it.</li>
 * <li>fullFieldName: Full name of the field as known to the query. For fields in an array elemMatch query,
 *     this is the name of the field containing the array name and field name under the array.</li>
 * <li>fieldMd: Metadata node for the field</li>
 * <li>fieldEntity: The metadata for the entity containing the field</li>
 * <li>entityRelativeFieldName: The relative field name of the field in the entity containing it</li>
 * <li>clause: The query clause</li>
 * </ul>
 */
public class AnalyzeQuery extends QueryIterator {
    
    /**
     * The root composite metadata
     */
    private final CompositeMetadata root;

    /**
     * The reference field containing the query. Null if the query is a request query
     */
    private final ResolvedReferenceField ref;

    private List<QueryFieldInfo> fieldInfo;
    private int nested=0;

    /**
     * Ctor
     *
     * @param root The root entity
     * @param referenceField The resolved reference for the reference
     * field containing the query. null if the query is a request
     * query
     */
    public AnalyzeQuery(CompositeMetadata root,
                        ResolvedReferenceField referenceField) {
        this.root=root;
        this.ref=referenceField;
    }

    public List<QueryFieldInfo> getFieldInfo() {
        return fieldInfo;
    }

    @Override
    public QueryExpression iterate(QueryExpression q, Path context) {
    	if(nested==0)
            fieldInfo=new ArrayList<>();
        nested++;
    	try {
            return super.iterate(q,context);
    	} finally {
            nested--;
    	}
    }
    
    /**
     * Resolve the given field, find its metadata. Throw exception if it cannot be resolved.
     */
    private FieldTreeNode resolve(Path field) {
        FieldTreeNode fieldNode=ref==null?root.resolve(field):
            ref.getElement().resolve(field);
        if(fieldNode==null)
            throw Error.get(AssocConstants.ERR_CANNOT_FIND_FIELD,field.toString());
        return fieldNode;
    }

    private CompositeMetadata getFieldMd(FieldTreeNode fieldNode) {
        // rr: The first resolved reference ancestor of the field
        ResolvedReferenceField rr=root.getResolvedReferenceOfField(fieldNode instanceof ResolvedReferenceField?fieldNode.getParent():fieldNode);
        // fieldMd: The composite metadata containing the field
        return rr==null?root:rr.getReferencedMetadata();
    }

    private Path computeEntityRelativeFieldName(Path normalizedFieldName,FieldTreeNode fieldNode) {
        // Now compute the relative field name within its entity
        // start from fieldNode, go backwards until the entity boundary
        // copy the indexes from the normalizedFieldName
        FieldTreeNode trc=fieldNode;
        int n=normalizedFieldName.numSegments()-1;
        ArrayList<String> list=new ArrayList<>(n);
        while(!((trc instanceof ArrayElement&&
                ((ArrayElement)trc).getParent() instanceof ResolvedReferenceField) ||
                trc.getParent()==null )) {
            String name=trc.getName();
            if(Path.ANY.equals(name)) {
            	if(n>=0) {
            		String head=normalizedFieldName.head(n);
            		if(!head.equals("$parent")) {
            			list.add(head);
            		} else {
            			list.add(Path.ANY);
            		}
            	} else {
            		list.add(Path.ANY);
            	}
            } else {
                list.add(name);
            }
            n--;
            trc=trc.getParent();
        } 
        MutablePath p=new MutablePath();
        n=list.size();
        for(int i=n-1;i>=0;i--)
            p.push(list.get(i));
        return p.immutableCopy();
    }

    /**
     * Returns true if 'ancestor' is an ancestor of 'descendant'. Does not cross entity boundaries.
     */
    private boolean isAnAncestor(FieldTreeNode ancestor,FieldTreeNode descendant) {
        FieldTreeNode trc=descendant;
        do {
            if(trc==ancestor) {
                return true;
            } else {
                trc=trc.getParent();
            }
        } while(trc.getParent()!=null&&!(trc instanceof ArrayElement &&
                                         ((ArrayElement)trc).getParent() instanceof ResolvedReferenceField ));
        return false;
    }


    private static class NearestCommonNode {
        final FieldTreeNode node; // The nearest node, or null of root
        final int numSegments; // Number of segment we have to back in context

        public NearestCommonNode(FieldTreeNode node,int n) {
            this.node=node;
            this.numSegments=n;
        }
    }
    
    /**
     * If contextNode and fieldNode are both in the same entity,
     * returns the nearest common node in the context's ancestors, or null if there is none. 
     * Also returns the number of nodes passed to get there.
     */
    private NearestCommonNode findNearestCommonNode(FieldTreeNode contextNode,FieldTreeNode fieldNode) {
        int n=0;
        FieldTreeNode trc=contextNode;
        do {
            if(isAnAncestor(trc,fieldNode)) {
                return new NearestCommonNode(trc,n);
            } else {
                trc=trc.getParent();
                n++;
            }
        } while(trc.getParent()!=null&&!(trc instanceof ArrayElement &&
                                         ((ArrayElement)trc).getParent() instanceof ResolvedReferenceField ));
        return new NearestCommonNode(null,n);
    }

    /**
     * This is the function that builds the QueryFieldInfo. Given the
     * clause field name (the field name as it appears in the clause),
     * the context (any array references if this is an elem-match
     * query), and the closest query clause, this function determines
     * the field metadata, the entity the field is in, and the entity
     * relative field name of the field, and returns this information in QueryFieldInfo
     */
    private QueryFieldInfo resolveField(Path clauseFieldName,
                                        Path context,
                                        QueryExpression clause) {
        // fullFieldName: The name of the field, including any enclosing elemMatch queries
        Path fullFieldName=context.isEmpty()?clauseFieldName:new Path(context,clauseFieldName);
        // The field node in metadata. Resolved relative to the
        // reference field if the query is for a reference, or
        // resolved relative to the root if the query is a request
        // query
        FieldTreeNode fieldNode=resolve(fullFieldName);
        CompositeMetadata fieldMd=getFieldMd(fieldNode);

        Path entityRelativeContext=Path.EMPTY;
        FieldTreeNode contextNode=null;
        // Is context in the same entity as the field? If so, then a
        // suffix of the context should be removed from
        // entityRelativeFieldNameWithContext Otherwise, there is no
        // need to remove the context
        if(!context.isEmpty()) {
            contextNode=resolve(context);            
            CompositeMetadata contextMd=getFieldMd(contextNode);
            if(contextMd==fieldMd) {
                entityRelativeContext=computeEntityRelativeFieldName(context.normalize(),contextNode);
            }
        }
        
        // normalizedFieldName: the field name where $parent can only appear at the beginning
        // No $this can appear
        Path normalizedFieldName=fullFieldName.normalize();
        Path entityRelativeFieldNameWithContext=computeEntityRelativeFieldName(normalizedFieldName,fieldNode);

        // If context is nonempty, and it is in the same entity as the
        // field, we have to compute entity relative field name
        // without context
        Path entityRelativeFieldName;
        if(!entityRelativeContext.isEmpty()) {
            NearestCommonNode ncn=findNearestCommonNode(contextNode,fieldNode);
            // A prefix of entityRelativeFieldNameWithContext must resolve to the nearest common node
            // That prefix needs to be removed
            // then, numSegments $parents should be prepended
            int n=entityRelativeFieldNameWithContext.numSegments();
            Path inContextSuffix=entityRelativeFieldNameWithContext;
            for(int i=0;i<n;i++) {
                if(i==0) {
                    // No prefix, entity root.
                    if(ncn.node==null) {
                        // We found the common prefix-root
                        inContextSuffix=entityRelativeFieldNameWithContext;
                        break;
                    }
                } else {
                    FieldTreeNode node=fieldMd.resolve(entityRelativeFieldNameWithContext.prefix(i));
                    if(node==ncn.node) {
                        // We found the common prefix
                        // cut it
                        inContextSuffix=entityRelativeFieldNameWithContext.suffix(-i);
                        break;
                    }
                }
            }
            if(ncn.numSegments>0) {
                MutablePath p=new MutablePath();
                for(int i=0;i<ncn.numSegments;i++)
                    p.push("$parent");
                entityRelativeFieldName=new Path(p,inContextSuffix);
            } else {
                entityRelativeFieldName=inContextSuffix;
            }
        } else
            entityRelativeFieldName=entityRelativeFieldNameWithContext;
        
        return new QueryFieldInfo(clauseFieldName,
                                  fullFieldName,
                                  fieldNode,
                                  fieldMd,
                                  entityRelativeFieldName,
                                  entityRelativeFieldNameWithContext,
                                  clause);
    }
    
    @Override
    protected QueryExpression itrValueComparisonExpression(ValueComparisonExpression q, Path context) {
        fieldInfo.add(resolveField(q.getField(),context,q));
        return q;
    }

    @Override
    protected QueryExpression itrFieldComparisonExpression(FieldComparisonExpression q, Path context) {
        QueryFieldInfo lField=resolveField(q.getField(),context,q);
        QueryFieldInfo rField=resolveField(q.getRfield(),context,q);
        fieldInfo.add(lField);
        fieldInfo.add(rField);
        return q;
    }

    @Override
    protected QueryExpression itrRegexMatchExpression(RegexMatchExpression q, Path context) {
        fieldInfo.add(resolveField(q.getField(),context,q));
        return q;
    }

    @Override
    protected QueryExpression itrNaryValueRelationalExpression(NaryValueRelationalExpression q, Path context) {
        fieldInfo.add(resolveField(q.getField(),context,q));
        return q;
    }

    @Override
    protected QueryExpression itrNaryFieldRelationalExpression(NaryFieldRelationalExpression q, Path context) {
        QueryFieldInfo lField=resolveField(q.getField(),context,q);
        QueryFieldInfo rField=resolveField(q.getRfield(),context,q);
        fieldInfo.add(lField);
        fieldInfo.add(rField);
        return q;
    }

    @Override
    protected QueryExpression itrArrayContainsExpression(ArrayContainsExpression q, Path context) {
        fieldInfo.add(resolveField(q.getArray(),context,q));
        return q;
    }

    @Override
    protected QueryExpression itrUnaryLogicalExpression(UnaryLogicalExpression q, Path context) {
        return super.itrUnaryLogicalExpression(q,context);
    }

    @Override
    protected QueryExpression itrNaryLogicalExpression(NaryLogicalExpression q, Path context) {
        return super.itrNaryLogicalExpression(q,context);
    }

    @Override
    protected QueryExpression itrArrayMatchExpression(ArrayMatchExpression q, Path context) {
        fieldInfo.add(resolveField(q.getArray(),context,q));
        return super.itrArrayMatchExpression(q,context);
    }

}
