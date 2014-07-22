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
import com.mongodb.WriteResult;

/**
 *
 * @author nmalik
 */
public class SaveCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject data;

    public SaveCommand(DBCollection collection, DBObject data) {
        super(SaveCommand.class.getSimpleName(), collection);
        this.data = data;
    }

    @Override
    protected WriteResult runMongoCommand() {
        return getDBCollection().save(data);
    }
}
