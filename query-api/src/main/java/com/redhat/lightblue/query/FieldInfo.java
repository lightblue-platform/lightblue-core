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

import java.io.Serializable;

import com.redhat.lightblue.util.Path;

/**
 * This class contains field information: the field name, and the
 * query clause containing the field.
 */
public class FieldInfo  implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Path absFieldName;
    private final Path context;
    private final QueryExpression clause;

    /**
     * Constructs the field info with the given information
     *
     * @param absFieldName Absolute field name
     * @param context The context path under which the field is interpreted
     * @param clause The query clause containing the field
     */
    public FieldInfo(Path absFieldName,
                     Path context,
                     QueryExpression clause) {
        this.absFieldName=absFieldName;
        this.context=context;
        this.clause=clause;
    }

    /**
     * Copy ctor, shallow copy
     */
    public FieldInfo(FieldInfo f) {
        this(f.absFieldName,f.context,f.clause);
    }
    
    /**
     * Returns the absolute field name
     */
    public Path getAbsFieldName() {
        return absFieldName;
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
        return absFieldName +"@("+clause+")";
    }
}
