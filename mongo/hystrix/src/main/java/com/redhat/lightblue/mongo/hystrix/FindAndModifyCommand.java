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

/**
 *
 * @author nmalik
 */
public class FindAndModifyCommand extends AbstractMongoCommand<DBObject> {
    private final DBObject query;
    private final DBObject fields;
    private final DBObject sort;
    private final boolean remove;
    private final DBObject update;
    private final boolean returnNew;
    private final boolean upsert;

    //public DBObject findAndModify(DBObject query, DBObject fields, DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert){
    public FindAndModifyCommand(String clientKey, DBCollection collection, DBObject query, DBObject fields,
                                DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert) {
        super(FindAndModifyCommand.class.getSimpleName(), FindAndModifyCommand.class.getSimpleName(), clientKey, collection);
        this.query = query;
        this.fields = fields;
        this.sort = sort;
        this.remove = remove;
        this.update = update;
        this.returnNew = returnNew;
        this.upsert = upsert;
    }

    @Override
    protected DBObject run() {
        return getDBCollection().findAndModify(query, fields, sort, remove, update, returnNew, upsert);
    }
}
