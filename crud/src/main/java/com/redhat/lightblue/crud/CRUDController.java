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

import com.redhat.lightblue.mediator.MetadataResolver;

public interface CRUDController {

    /**
     * Performs insertion of documents to the back end
     *
     * @param resolver The metadata resolver
     * @param documents The documents to insert
     * @param projection If non-null, the inserted documents are
     * projected using this projection and returned in the response
     *
     * The resolver must provide access to the correct versions of
     * metadata used to insert all the documents. There is no
     * limitation on the list of documents other than that they all
     * have to belong to this particular back-end. Documents can
     * belong to different data stores of the same back-end. If
     * projection is non-null, the data must be projected and
     * returned, otherwise, no data is returned.
     */
    CRUDInsertionResponse insert(MetadataResolver resolver,
                                 List<JsonDoc> documents,
                                 Projection projection);

    CRUDFindResponse find(MetadataResolver resolver,
                          String entity,
                          QueryExpression query,
                          Projection projection,
                          Sort sort,
                          Long from,
                          Long to);

}

