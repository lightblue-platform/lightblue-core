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
package com.redhat.lightblue.crud.mongo;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDUpdateResponse;

import com.redhat.lightblue.metadata.EntityMetadata;

/**
 * Updates docs based on a query
 */
public interface DocUpdater {

    /**
     * Updates the documents in the resultset of a query
     *
     * @param ctx Operation context
     * @param collection The collection in which the documents will be updated
     * @param md Entity metadata
     * @param response Update response
     * @param query The query to search
     *
     * It is expected that how the documents will be updated is to be passed in the constructor. The implementation
     * should run the query, update the documents, compute the projections of the updated documents, and update the
     * operation context accordingly.1
     */
    void update(CRUDOperationContext ctx,
                DBCollection collection,
                EntityMetadata md,
                CRUDUpdateResponse response,
                DBObject query);
}
