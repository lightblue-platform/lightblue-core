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

import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.query.FieldInfo;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.GetQueryFields;

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.Path;

public class ResolvedFieldInfo extends FieldInfo {

    /**
     * The field metadata
     */
    private final FieldTreeNode fieldNode;

    /**
     * The entity-relative field name
     */
    private final Path entityRelativeField;

    /**
     * The metadata of the entity containing the field
     */
    private final EntityMetadata fieldEntityMetadata;

    /**
     * The composite metadata of the entity containing the field
     */
    private final CompositeMetadata fieldCmd;

    /**
     * The query plan node associated with the field
     */
    private final QueryPlanNode qnode;

    public ResolvedFieldInfo(Path clauseFieldName,Path ctx,QueryExpression clause,
                             CompositeMetadata root,ResolvedReferenceField context,QueryPlan qplan) {
        super(clauseFieldName,ctx,clause);
        // Get the field node for the field, interpret field name with respect to context
        fieldNode=context==null?root.resolve(getFieldName()):context.getElement().resolve(getFieldName());
        ResolvedReferenceField rr=root.getResolvedReferenceOfField(fieldNode);
        if(rr==null)
            fieldEntityMetadata=fieldCmd=root;
        else {
            fieldEntityMetadata=rr.getOriginalMetadata();
            fieldCmd=rr.getReferencedMetadata();
        }
        entityRelativeField=getEntityRelativeFieldName(fieldNode.getFullPath(),getFieldName(),fieldCmd.getEntityPath());
        qnode=qplan.getNode(fieldCmd);
        if(qnode==null)
            throw new IllegalArgumentException("An entity referenced in a query is not in composite metadata."+
                                               " field:"+getFieldName()+" Composite metadata:"+fieldCmd);
    }

    /**
     * Returns the field metadata
     */
    public FieldTreeNode getFieldNode() {
        return fieldNode;
    }

    /**
     * Get the field name relative to the entity it is contained in
     */
    public Path getEntityRelativeFieldName() {
        return entityRelativeField;
    }

    /**
     * Returns the entity metadata for the entity containing this field.
     */
    public EntityMetadata getFieldEntityMetadata() {
        return fieldEntityMetadata;
    }

    /**
     * Returns the entity composite metatada
     */
    public CompositeMetadata getFieldEntityCompositeMetadata() {
        return fieldCmd;
    }

    public QueryPlanNode getFieldQueryPlanNode() {
        return qnode;
    }

    /**
     * Path is a normalized field name, like $parent.1.x.y. $parent can only appear at the beggining.
     * fullPath is the full name of the same field, something like a.b.*.x.y.
     * We calculate how many non-$parent segments path has, then we get that suffix of path,
     * remove that many elements from fullPath, and append to get a.b.1.x.y
     */
    private static Path removeParents(Path fullPath,Path path) {
        path=path.normalize();
        int pathN=path.numSegments();
        // We will go backwards until we see $parent or the beginning
        int nNonParent=0;
        for(int n=pathN-1;n>=0;n--) {
            if(path.head(n).equals(Path.PARENT))
                break;
            else
                nNonParent++;
        }
        return new Path(fullPath.prefix(-nNonParent),path.suffix(nNonParent));
    }

    /**
     * Rewrites a field name relative to its entity.
     *
     * @param fullPath the fullpath of the field name
     * @param path The field name as it appears in an expression. suffix of this should be the same as fullPath
     * @param cmdPrefix The metadata entity path for the composite metadata containing the field
     */
    public static Path getEntityRelativeFieldName(Path fullPath, Path path,Path cmdPrefix) {
        Path normalized=removeParents(fullPath,path);
        if(cmdPrefix.numSegments()>0)
            return normalized.suffix(-(cmdPrefix.numSegments()+1));
        else
            return normalized;
    }

    private static class GetFields extends GetQueryFields<ResolvedFieldInfo> {
        private final CompositeMetadata root;
        private final ResolvedReferenceField context;
        private final QueryPlan qplan;
        
        public GetFields(List<ResolvedFieldInfo> list,CompositeMetadata root,ResolvedReferenceField context,QueryPlan qplan) {
            super(list);
            this.root=root;
            this.context=context;
            this.qplan=qplan;
        }

        @Override
        protected ResolvedFieldInfo newFieldInfo(Path clauseField,Path ctx,QueryExpression clause) {
            return new ResolvedFieldInfo(clauseField,ctx,clause,root,context,qplan);
        }
    }

    public static List<ResolvedFieldInfo> getQueryFields(QueryExpression q,
                                                         CompositeMetadata root,
                                                         ResolvedReferenceField context,
                                                         QueryPlan qplan) {
        List<ResolvedFieldInfo> list=new ArrayList<>();
        new GetFields(list,root,context,qplan).iterate(q);
        return list;
    }
}
