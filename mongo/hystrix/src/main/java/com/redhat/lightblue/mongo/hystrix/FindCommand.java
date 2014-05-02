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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Hystrix command for executing findOne on a MongoDB collection.
 *
 * @author nmalik
 */
public class FindCommand extends AbstractMongoCommand<DBCursor> {
    private final DBObject query;
    private final DBObject projection;

    /**
     *
     * @param clientKey used to set thread pool key
     * @param query
     */
    public FindCommand(String clientKey, DBCollection collection, DBObject query, DBObject projection) {
        super(FindCommand.class.getSimpleName(), FindCommand.class.getSimpleName(), clientKey, collection);
        this.query = query;
        this.projection = projection;
    }

    @Override
    protected DBCursor run() {
        return getDBCollection().find(query, projection);
    }
}
