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
package com.redhat.lightblue.crud;

import com.redhat.lightblue.metadata.MetadataListener;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;
import com.redhat.lightblue.util.JsonDoc;

public interface CRUDController {

    /**
     * Performs insertion of documents to the back end
     *
     * @param ctx Operation context
     * @param projection If non-null, the inserted documents are projected using
     * this projection and returned in the response
     *
     * The ctx must provide access to the correct versions of metadata used to
     * insert all the documents. All documents must be of the same entity type.
     * If projection is non-null, the data must be projected and returned,
     * otherwise, no data is returned.
     */
    CRUDInsertionResponse insert(CRUDOperationContext ctx,
                                 Projection projection);

    /**
     * Performs update or insertion of documents to the back end
     *
     * @param ctx Operation context
     * @param upsert If true, and if a document does not exist, it is inserted
     * @param projection If non-null, the inserted/updated documents are
     * projected using this projection and returned in the response
     *
     * The ctx must provide access to the correct versions of metadata used to
     * insert/update all the documents. All documents must be of the same entity
     * type. If a document has nonnull _id field, the document is updated in the
     * db. Otherwise, if upsert is true, the document is updated. If projection
     * is non-null, the data must be projected and returned, otherwise, no data
     * is returned.
     */
    CRUDSaveResponse save(CRUDOperationContext ctx,
                          boolean upsert,
                          Projection projection);

    /**
     * Updates documents matching the search criteria
     *
     * @param ctx Operation context
     * @param query The query specifying the documents to update
     * @param update The update expression specifying the operations to be
     * performed on matching documents
     * @param projection The fields to be returned from the updated documents
     *
     * If projection is non-null, the updated documents are projected and
     * returned.
     */
    CRUDUpdateResponse update(CRUDOperationContext ctx,
                              QueryExpression query,
                              UpdateExpression update,
                              Projection projection);

    /**
     * Deletes documents matching the search criteria
     *
     * @param ctx Operation context
     * @param query The query specifying the documents to delete
     *
     */
    CRUDDeleteResponse delete(CRUDOperationContext ctx,
                              QueryExpression query);

    /**
     * Searches for documents
     *
     * @param ctx Operation context
     * @param entity The entity to search for
     * @param query The query. Cannot be null
     * @param projection What fields to return. Cannot be null
     * @param sort Sort keys. Can be null
     * @param from starting index in the result set. Can be null. Meaninguful
     * only if a sort is given. Starts from 0.
     * @param to end index in the result set. Starts from 0, and inclusive. Can
     * be null.
     */
    CRUDFindResponse find(CRUDOperationContext ctx,
                          QueryExpression query,
                          Projection projection,
                          Sort sort,
                          Long from,
                          Long to);


    /**
     * Return an implementation of MetadataListener interface to receive
     * notifications about metadata operations. Returns null if this
     * implementation is not interested in receiving metadata notifications
     */
    MetadataListener getMetadataListener();

    /**
     * The back end should update the predefined fields of the document
     */
    void updatePredefinedFields(CRUDOperationContext ctx, JsonDoc doc);
    
    /**
     * Checks the CRUD controller health. The respective implementations should
     * provide the health of lightblue CRUD layer which is further exposed by
     * dropwizard metrics HealthCheckServlet REST endpoint
     */
    CRUDHealth checkHealth();
}
