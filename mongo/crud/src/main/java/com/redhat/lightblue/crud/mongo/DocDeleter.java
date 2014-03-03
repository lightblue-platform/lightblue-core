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

import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;

/**
 * Deletes documents from a MongoDB collection
 */
public interface DocDeleter {

    /**
     * Delete documents
     *
     * @param ctx Operation context
     * @param collection The collection from which the documents will be deleted
     * @param mongoQuery The query whose result set will be deleted
     * @param response The deletion response
     *
     * The implementation should delete the documents, and update the
     * response with the number of docs deleted. No exception handling
     * is expected from the implementation, so all unhandled
     * exceptions should be rethrown.
     */
    void delete(CRUDOperationContext ctx,
                DBCollection collection,
                DBObject mongoQuery,
                CRUDDeleteResponse response);
}
