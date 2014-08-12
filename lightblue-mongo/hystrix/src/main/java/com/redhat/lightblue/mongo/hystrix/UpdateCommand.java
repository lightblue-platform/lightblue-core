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
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 *
 * @author nmalik
 */
public class UpdateCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject query;
    private final DBObject update;
    private final boolean upsert;
    private final boolean multi;
    private final WriteConcern concern;

    public UpdateCommand(DBCollection collection, DBObject query, DBObject update, boolean upsert, boolean multi) {
        this(collection, query, update, upsert, multi, null);
    }

    public UpdateCommand(DBCollection collection, DBObject query, DBObject update, boolean upsert, boolean multi, WriteConcern concern) {
        super(UpdateCommand.class.getSimpleName(), collection);
        this.query = query;
        this.update = update;
        this.upsert = upsert;
        this.multi = multi;
        this.concern = concern;
    }

    @Override
    protected WriteResult runMongoCommand() {
        if (concern != null) {
            return getDBCollection().update(query, update, upsert, multi, concern);
        } else {
            return getDBCollection().update(query, update, upsert, multi);
        }
    }
}
