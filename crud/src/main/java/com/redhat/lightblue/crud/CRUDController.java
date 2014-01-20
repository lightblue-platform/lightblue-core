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

import java.util.List;

import com.redhat.lightblue.util.JsonDoc;

import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Sort;
import com.redhat.lightblue.query.UpdateExpression;

public interface CRUDController {

    /**
     * Performs insertion of documents to the back end
     *
     * @param resolver The metadata resolver
     * @param documents The documents to insert
     * @param projection If non-null, the inserted documents are projected using this projection and returned in the
     * response
     *
     * The resolver must provide access to the correct versions of metadata used to insert all the documents. There is
     * no limitation on the list of documents other than that they all have to belong to this particular back-end.
     * Documents can belong to different data stores of the same back-end. If projection is non-null, the data must be
     * projected and returned, otherwise, no data is returned.
     */
    CRUDInsertionResponse insert(MetadataResolver resolver,
                                 List<JsonDoc> documents,
                                 Projection projection);

    /**
     * Performs update or insertion of documents to the back end
     *
     * @param resolver The metadata resolver
     * @param documents The documents to insert or update
     * @param upsert If true, and if a document does not exist, it is inserted
     * @param projection If non-null, the inserted/updated documents are projected using this projection and returned in
     * the response
     *
     * The resolver must provide access to the correct versions of metadata used to insert/update all the documents.
     * There is no limitation on the list of documents other than that they all have to belong to this particular
     * back-end. Documents can belong to different data stores of the same back-end. If a document has nonnull _id
     * field, the document is updated in the db. Otherwise, if upsert is true, the document is updated. If projection is
     * non-null, the data must be projected and returned, otherwise, no data is returned.
     */
    CRUDSaveResponse save(MetadataResolver resolver,
                          List<JsonDoc> documents,
                          boolean upsert,
                          Projection projection);

    /**
     * Updates documents matching the search criteria
     *
     * @param resolver The metadata resolver
     * @param entity The entity to work on
     * @param query The query specifying the documents to update
     * @param update The update expression specifying the operations to be performed on matching documents
     * @param projection The fields to be returned from the updated documents
     *
     * If projection is non-null, the updated documents are projected and returned.
     */
    CRUDUpdateResponse update(MetadataResolver resolver,
                              String entity,
                              QueryExpression query,
                              UpdateExpression update,
                              Projection projection);

    /**
     * Searches for documents
     *
     * @param resolver The metadata resolver
     * @param entity The entity to search for
     * @param query The query. Cannot be null
     * @param projection What fields to return. Cannot be null
     * @param sort Sort keys. Can be null
     * @param from starting index in the result set. Can be null. Meaninguful only if a sort is given. Starts from 0.
     * @param to end index in the result set. Starts from 0, and inclusive. Can be null.
     */
    CRUDFindResponse find(MetadataResolver resolver,
                          String entity,
                          QueryExpression query,
                          Projection projection,
                          Sort sort,
                          Long from,
                          Long to);

}
