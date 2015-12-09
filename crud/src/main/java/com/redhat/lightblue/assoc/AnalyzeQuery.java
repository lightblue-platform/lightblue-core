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

    private List<QueryFieldInfo> fieldInfo=new ArrayList<>();

    public AnalyzeQuery(CompositeMetadata root,
                        ResolvedReferenceField referenceField) {
        this.root=root;
        this.ref=referenceField;
    }

    public List<QueryFieldInfo> getFieldInfo() {
        return fieldInfo;
    }
    
    private QueryFieldInfo resolveField(Path clauseFieldName,
                                        Path context,
                                        QueryExpression clause) {
        // fullFieldName: The name of the field, including any enclosing elemMatch queries
        Path fullFieldName=context.isEmpty()?clauseFieldName:new Path(context,clauseFieldName);
        // The field node in metadata. Resolved relative to the
        // reference field if the query is for a reference, or
        // resolved relative to the root if the query is a request
        // query
        FieldTreeNode fieldNode=ref==null?root.resolve(fullFieldName):
            ref.getElement().resolve(fullFieldName);
        if(fieldNode==null)
            throw Error.get(AssocConstants.ERR_CANNOT_FIND_FIELD,clauseFieldName.toString());
        // rr: The first resolved reference ancestor of the field
        ResolvedReferenceField rr=root.getResolvedReferenceOfField(fieldNode);
        // fieldMd: The composite metadata containing the field
        CompositeMetadata fieldMd=rr==null?root:rr.getReferencedMetadata();
        // Now compute entity relative field name
        // The field name may contain array indexes in it. The metadata field loses that info,
        // replacing all indexes with '*'. We have to put the indexes back into the entity
        // relative field name.
        
        // normalizedFieldName: the field name where $parent can only appear at the beginning
        // No $this can appear
        Path normalizedFieldName=fullFieldName.normalize();

        // Now compute the relative field name within its entity
        // start from fieldNode, go backwards until the entity boundary
        // copy the indexes from the normalizedFieldName
        FieldTreeNode trc=fieldNode;
        int n=normalizedFieldName.numSegments()-1;
        ArrayList<String> list=new ArrayList<>(n);
        do {
            String name=trc.getName();
            if(Path.ANY.equals(name)) {
                list.add(normalizedFieldName.head(n));
            } else {
                list.add(name);
            }
            trc=trc.getParent();
            if( (trc instanceof ArrayElement&&
                 ((ArrayElement)trc).getParent() instanceof ResolvedReferenceField) ||
                trc.getParent()==null ) {
                // Entity boundary, or root
                trc=null;
            }
        } while(trc!=null);
        MutablePath p=new MutablePath();
        n=list.size();
        for(int i=n-1;i>=0;i--)
            p.push(list.get(i));
        Path entityRelativeFieldName=p.immutableCopy();
        return new QueryFieldInfo(clauseFieldName,
                                  fullFieldName,
                                  fieldNode,
                                  fieldMd,
                                  entityRelativeFieldName,
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
        return super.itrArrayMatchExpression(q,context);
    }

}
