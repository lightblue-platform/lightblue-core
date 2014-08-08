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

import com.redhat.lightblue.metadata.rdbms.converter.RDBMSContext;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.common.rdbms.RDBMSDataSourceResolver;
import com.redhat.lightblue.common.rdbms.RDBMSDataStore;
import com.redhat.lightblue.crud.*;
import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;
import com.redhat.lightblue.eval.Projector;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.rdbms.model.RDBMS;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.Error;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 Implements the relationship between Metdata from Lightblue and the Data from RDBMS
 */
public class RDBMSCRUDController implements CRUDController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RDBMSCRUDController.class);

    private final JsonNodeFactory nodeFactory;
    private final RDBMSDataSourceResolver rds;

    public RDBMSCRUDController(RDBMSDataSourceResolver rds) {
        this(JsonNodeFactory.withExactBigDecimals(true), rds);
    }

    public RDBMSCRUDController(JsonNodeFactory factory, RDBMSDataSourceResolver rds) {
        this.nodeFactory = factory;
        this.rds = rds;
    }

    @Override
    public CRUDInsertionResponse insert(CRUDOperationContext ctx, Projection projection) {
        LOGGER.debug("insert() start");
        Error.push("insert() start");

        CRUDInsertionResponse response = new CRUDInsertionResponse();
        int n = 0;
        response.setNumInserted(n);

        Error.pop();
        LOGGER.debug("insert() stop");
        return response;
    }

    @Override
    public CRUDSaveResponse save(CRUDOperationContext ctx, boolean upsert, Projection projection) {
        LOGGER.debug("save() start");
        Error.push("");

        CRUDSaveResponse response = new CRUDSaveResponse();
        int n = 0;
        response.setNumSaved(n);

        Error.pop();
        LOGGER.debug("save() stop");
        return response;
    }

    @Override
    public CRUDUpdateResponse update(CRUDOperationContext ctx,
                                     QueryExpression query,
                                     UpdateExpression update,
                                     Projection projection) {
        if (query == null) {
            throw new IllegalArgumentException("No query informed");
        }
        LOGGER.debug("update start: q:{} u:{} p:{}", query, update, projection);
        Error.push("");

        CRUDUpdateResponse response = new CRUDUpdateResponse();

        Error.pop();
        LOGGER.debug("update end: updated: {}, failed: {}", response.getNumUpdated(), response.getNumFailed());
        return response;
    }

    @Override
    public CRUDDeleteResponse delete(CRUDOperationContext ctx,
                                     QueryExpression query) {
        if (query == null) {
            throw new IllegalArgumentException("No query informed");
        }
        LOGGER.debug("delete start: q:{}", query);
        Error.push("");

        CRUDDeleteResponse response = new CRUDDeleteResponse();

        Error.pop();
        LOGGER.debug("delete end: deleted: {}}", response.getNumDeleted());
        return response;
    }

    @Override
    public CRUDFindResponse find(CRUDOperationContext crudOperationContext,
                                 QueryExpression queryExpression,
                                 Projection projection,
                                 Sort sort,
                                 Long from,
                                 Long to) {
        if (queryExpression == null) {
            throw new IllegalArgumentException("No query informed");
        }
        if (projection == null) {
            throw new IllegalArgumentException("No projection informed");
        }
        LOGGER.debug("find start: q:{} p:{} sort:{} from:{} to:{}", queryExpression, projection, sort, from, to);
        Error.push("find call");
        CRUDFindResponse response = new CRUDFindResponse();

        try {
            EntityMetadata md = crudOperationContext.getEntityMetadata(crudOperationContext.getEntityName());
            if (md.getAccess().getFind().hasAccess(crudOperationContext.getCallerRoles())) {
                FieldAccessRoleEvaluator roleEval = new FieldAccessRoleEvaluator(md, crudOperationContext.getCallerRoles());
                RDBMSContext rdbmsContext = new RDBMSContext();             
                RDBMS rdbms = (RDBMS)md.getEntitySchema().getProperties().get("rdbms");
                if(rdbms == null){
                    throw new IllegalStateException("Configured to use RDBMS but no RDBMS definition was found for the entity");
                }
                rdbmsContext.setRdbms(rdbms);
                RDBMSDataStore d = (RDBMSDataStore) md.getDataStore();
                DataSource ds = rds.get(d);
                rdbmsContext.setDataSource(ds);
                rdbmsContext.setRowMapper(new VariableUpdateRowMapper(rdbmsContext));
                rdbmsContext.setEntityMetadata(md);
                RDBMSProcessor.process(crudOperationContext, rdbmsContext, "find");
                
                crudOperationContext.getHookManager().queueHooks(crudOperationContext);
            } else {
                crudOperationContext.addError(Error.get("No access", "find:" + crudOperationContext.getEntityName()));
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("find end: query: {} results: {}", response.getSize());
        return response;
    }

    @Override
    public void updateEntityInfo(Metadata md, EntityInfo ei) {

    }

    @Override
    public void newSchema(Metadata md, EntityMetadata emd) {

    }
}
