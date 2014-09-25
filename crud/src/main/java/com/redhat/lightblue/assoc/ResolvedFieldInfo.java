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

import com.redhat.lightblue.query.FieldInfo;

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

    public ResolvedFieldInfo(FieldInfo x,CompositeMetadata root) {
        super(x);
        // This is the field name as it appears in the query
        Path fld=getAbsFieldName();
        // Get the field node for the field, interpret field name with respect to root
        fieldNode=root.resolve(fld);
        ResolvedReferenceField rr=root.getResolvedReferenceOfField(fieldNode);
        if(rr==null)
            fieldEntityMetadata=root;
        else
            fieldEntityMetadata=rr.getOriginalMetadata();

        entityRelativeField=root.getEntityRelativeFieldName(fieldNode);
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
}
