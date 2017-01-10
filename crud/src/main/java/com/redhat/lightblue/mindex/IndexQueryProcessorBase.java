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
package com.redhat.lightblue.mindex;

import java.util.List;

import com.redhat.lightblue.assoc.QueryFieldInfo;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.QueryIteratorSkeleton;

import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

abstract class IndexQueryProcessorBase<T> extends QueryIteratorSkeleton<T> {

    public static final String ERR_INDEX_ANALYZE="crud:index:QueryAnalysisError";

    protected final List<QueryFieldInfo> fieldInfo;
    
    public IndexQueryProcessorBase(List<QueryFieldInfo> fields) {
        this.fieldInfo=fields;
    }
    
    protected QueryFieldInfo findFieldInfo(Path field, QueryExpression clause) {
        for (QueryFieldInfo fi : fieldInfo) {
            if (fi.getClause() == clause) {
                if (fi.getFieldNameInClause().equals(field)) {
                    return fi;
                }
            }
        }
        throw Error.get(ERR_INDEX_ANALYZE, field.toString() + "@" + clause.toString());
    }
}
