package com.redhat.lightblue.crud.rdbms;

import com.redhat.lightblue.common.rdbms.RDBMSContext;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;

public class QueryTranslator {
    public static <T> void translate(RDBMSContext<T> rdbmsContext, CRUDOperationContext crudOperationContext, QueryExpression queryExpression, Sort sort, Long from, Long to, EntityMetadata md) {
        //TODO get the query and sub queries, get their fields, get the tables related to them, translate

    }
}
