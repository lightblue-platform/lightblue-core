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

import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.FieldTreeNode;

import com.redhat.lightblue.query.QueryExpression;

import com.redhat.lightblue.util.Path;

/**
 * Contains information about a field in the query
 *
 * <pre>
 *    { field; 'x.y', op:'=', rvalue=<value> }
 * </pre>
 *
 * <ul>
 * <li>fieldNameInClause: x.y</li>
 * <li>fullFieldName: x.y</li>
 * <li>fieldMd: Metadata node for x.y</li>
 * <li>fieldEntity: The metadata for the entity containing x.y</li>
 * <li>entityRelativeFieldName: x.y, the relative field name of the field in the entity containing it</li>
 * <li>clause: The query clause</li>
 * </ul>
 * 
 * <pre>
 *  { array: 'a.b', elemMatch: { field:'x.y', op:'=',rvalue:<value>} }
 * </pre>
 * 
 * <ul>
 * <li>fieldNameInClause: x.y</li>
 * <li>fullFieldName: a.b.*.x.y</li>
 * <li>entityRelativeFieldName: If a.b.* is a reference field, entity relative field name is x.y.<li>
 * <li>clause: {field:'x.y', op:'=',rvalue:<value>}</li>
 * </ul>
 */
public class QueryFieldInfo {
    private final Path fieldNameInClause;
    private final Path fullFieldName;
    private final FieldTreeNode fieldMd;
    private final CompositeMetadata fieldEntity;
    private final Path entityRelativeFieldName;
    private final QueryExpression clause;

    public QueryFieldInfo(Path fieldNameInClause,
                          Path fullFieldName,
                          FieldTreeNode fieldMd,
                          CompositeMetadata fieldEntity,
                          Path entityRelativeFieldName,
                          QueryExpression clause) {
        this.fieldNameInClause=fieldNameInClause;
        this.fullFieldName=fullFieldName;
        this.fieldMd=fieldMd;
        this.fieldEntity=fieldEntity;
        this.entityRelativeFieldName=entityRelativeFieldName;
        this.clause=clause;
    }

    /**
     * Name of the field in the clause containing the field. 
     */
    public Path getFieldNameInClause() {
        return fieldNameInClause;
    }

    /**
     * Full name of the field. This includes the array name if the
     * field appears in an elemMatch clause.
     */
    public Path getFullFieldName() {
        return fullFieldName;
    }

    /**
     * Field metadata
     */
    public FieldTreeNode getFieldMd() {
        return fieldMd;
    }

    /**
     * The composite metadata for the entity containing the field
     */
    public CompositeMetadata getFieldEntity() {
        return fieldEntity;
    }

    /**
     * The name of the field relative to the entity containing it
     */
    public Path getEntityRelativeFieldName() {
        return entityRelativeFieldName;
    }

    /**
     * The clause containing the field
     */
    public QueryExpression getClause() {
        return clause;
    }

    @Override
    public String toString() {
        if(entityRelativeFieldName.equals(fullFieldName))
            return fullFieldName+"@"+fieldEntity.getName();
        else
            return entityRelativeFieldName+"@"+fieldEntity.getName()+":"+fullFieldName;
    }
}
