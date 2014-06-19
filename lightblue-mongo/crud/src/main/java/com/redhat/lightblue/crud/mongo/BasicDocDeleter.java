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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.mongo.hystrix.RemoveCommand;

/**
 * Basic document deletion with no transaction support
 */
public class BasicDocDeleter implements DocDeleter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicDocDeleter.class);

    @Override
    public void delete(CRUDOperationContext ctx,
                       DBCollection collection,
                       DBObject mongoQuery,
                       CRUDDeleteResponse response) {
        LOGGER.debug("Removing docs with {}", mongoQuery);
        WriteResult result = new RemoveCommand(collection, mongoQuery).execute();
        LOGGER.debug("Removal complete, write result={}", result);
        response.setNumDeleted(result.getN());
    }
}
