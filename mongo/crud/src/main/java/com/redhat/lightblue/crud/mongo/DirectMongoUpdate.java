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

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.WriteConcern;

import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CRUDUpdateResponse;
import com.redhat.lightblue.crud.CrudConstants;

import com.redhat.lightblue.eval.FieldAccessRoleEvaluator;

import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * Atomic updater: uses mongo update to update all docs in one call. No projections.
 */
public class DirectMongoUpdate implements DocUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectMongoUpdate.class);

    private final DBObject mongoUpdate;
    private final FieldAccessRoleEvaluator roleEval;
    private final Set<Path> updatedFields;

    public DirectMongoUpdate(DBObject mongoUpdate,
                             FieldAccessRoleEvaluator roleEval,
                             Set<Path> updatedFields) {
        this.mongoUpdate = mongoUpdate;
        this.roleEval = roleEval;
        this.updatedFields = updatedFields;
    }

    @Override
    public void update(CRUDOperationContext ctx,
                       DBCollection collection,
                       EntityMetadata md,
                       CRUDUpdateResponse response,
                       DBObject query) {
        Set<Path> inaccessibleFields = roleEval.getInaccessibleFields(FieldAccessRoleEvaluator.Operation.update);
        for (Path x : inaccessibleFields) {
            if (updatedFields.contains(x)) {
                ctx.addError(Error.get("update", CrudConstants.ERR_NO_FIELD_UPDATE_ACCESS, x.toString()));
            }
        }
        if (!ctx.hasErrors()) {
            LOGGER.debug("Calling update with q={} and u={}", query, mongoUpdate);
            WriteResult result = collection.update(query, mongoUpdate, false, true, WriteConcern.SAFE);
            LOGGER.debug("Update result={}", result);
            // No projections
            response.setNumUpdated(result.getN());
        }
    }
}
