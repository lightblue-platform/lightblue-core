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
        if(queryExpression == null){
            return;
        }

    }
}
