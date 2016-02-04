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
package com.redhat.lightblue.query;

import com.redhat.lightblue.util.Path;

import java.io.Serializable;

/**
 * This class contains field information: the field name, and the query clause
 * containing the field.
 */
public class FieldInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Path fieldName;    
    private final Path clauseFieldName;
    private final Path context;
    private final QueryExpression clause;

    /**
     * Constructs the field info with the given information
     *
     * @param clauseFieldName Field name
     * @param context The context path under which the field is interpreted
     * @param clause The query clause containing the field
     */
    public FieldInfo(Path clauseFieldName,
                     Path context,
                     QueryExpression clause) {
        this.fieldName = context.isEmpty()?clauseFieldName:new Path(context,clauseFieldName);
        this.clauseFieldName = clauseFieldName;
        this.context = context;
        this.clause = clause;
    }

    /**
     * Copy ctor, shallow copy
     */
    public FieldInfo(FieldInfo f) {
        this.fieldName=f.fieldName;
        this.clauseFieldName=f.clauseFieldName;
        this.context=f.context;
        this.clause=f.clause;
    }

    public FieldInfo(Path fieldName,
                     Path clauseFieldName,
                     Path context,
                     QueryExpression clause) {
        this.fieldName=fieldName;
        this.clauseFieldName=clauseFieldName;
        this.context=context;
        this.clause=clause;
    }

    /**
     * Returns the field name within its context
     */
    public Path getFieldName() {
        return fieldName;
    }

    /**
     * Returns the field name as it appears in the query clause, without the context
     */
    public Path getClauseFieldName() {
        return clauseFieldName;
    }

    /**
     * Returns the context path under which the field name is interpreted
     */
    public Path getContext() {
        return context;
    }

    /**
     * Returns the query clause containing the field
     */
    public QueryExpression getClause() {
        return clause;
    }

    public String toString() {
        return fieldName + "@(" + clause + ")";
    }
}
