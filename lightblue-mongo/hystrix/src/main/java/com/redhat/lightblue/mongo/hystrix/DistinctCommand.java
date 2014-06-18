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
import java.util.List;
import com.mongodb.DBObject;

/**
 *
 * @author nmalik
 */
public class DistinctCommand extends AbstractMongoCommand<List> {
    private final String key;
    private final DBObject query;

    public DistinctCommand(String clientKey, DBCollection collection, String key) {
        this(clientKey, collection, key, null);
    }

    public DistinctCommand(String clientKey, DBCollection collection, String key, DBObject query) {
        super(DistinctCommand.class.getSimpleName(),clientKey, collection);
        this.key = key;
        this.query = query;
    }

    @Override
    protected List runMongoCommand() {
        if (query == null) {
            return getDBCollection().distinct(key);
        } else {
            return getDBCollection().distinct(key, query);
        }
    }
}
