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

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;

/**
 * Inserts or saves a doc
 */
public interface DocSaver {

    public enum Op { insert, save };

    /**
     * Inserts or saves a doc
     *
     * @param ctx Operation context
     * @param op insert or save
     * @param upsert Whether to insert if the document is not in db
     * @param collection The MongoDB collection to which documents will be inserted or saved
     * @param md Entity metadata
     * @param dbObject Document to insert/save
     * @param inputDoc The input document
     *
     * The implementation should insert or save the document to the
     * collection. If operation is insert, the document is inserted,
     * and the _id is returned in the output document of inputDoc. If
     * the operation is save, and the document to be saved has _id,
     * the document is attempted to be updated in the db. If the db
     * does not have the document but upsert=true, document is
     * inserted. Otherwise, update fails.
     */
    void saveDoc(CRUDOperationContext ctx,
                 Op op,
                 boolean upsert,
                 DBCollection collection,
                 EntityMetadata md,
                 DBObject dbObject,
                 DocCtx inputDoc);
}

