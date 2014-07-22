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
package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.common.rdbms.Parameter;
import com.redhat.lightblue.common.rdbms.RDBMSContext;
import com.redhat.lightblue.common.rdbms.TableField;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;

import java.util.HashSet;

public class QueryTranslator {

    public static <T> void translate(RDBMSContext<T> rdbmsContext,
                                     CRUDOperationContext crudOperationContext,
                                     QueryExpression queryExpression, Sort sort, Long from, Long to,
                                     EntityMetadata entityMetadata) {
        //TODO get the query and sub queries, get their fields, get the tables related to them, translate

        final StringBuilder queryBeginning = new StringBuilder();
        final StringBuilder queryEnding = new StringBuilder();
        rdbmsContext.setTableFields(new HashSet<TableField>());
        rdbmsContext.setParameters(new HashSet<Parameter>());

        recursiveQueryBuild(rdbmsContext, crudOperationContext, queryExpression, sort, from, to, entityMetadata, queryBeginning, queryEnding, "");

        rdbmsContext.setStatement(queryBeginning.toString() + queryEnding.toString());
    }

    private static <T> void recursiveQueryBuild(RDBMSContext<T> rdbmsContext, CRUDOperationContext crudOperationContext, QueryExpression queryExpression, Sort sort, Long from, Long to, EntityMetadata entityMetadata, StringBuilder queryBeginning, StringBuilder queryEnding, String linkPath) {
        if (queryExpression == null) {
            return;
        }

    }
}
