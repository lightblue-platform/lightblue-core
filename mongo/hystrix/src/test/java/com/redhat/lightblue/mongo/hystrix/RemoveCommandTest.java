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

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class RemoveCommandTest extends AbstractMongoTest {
    @Test
    public void executeWithWriteConcern() {
        DBObject data = new BasicDBObject(key1, "obj1");

        DBObject before = coll.findOne(data);

        Assert.assertNotNull(before);

        WriteResult result = new RemoveCommand(null, coll, data, WriteConcern.SAFE).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        DBObject after = coll.findOne(data);

        Assert.assertNull(after);
    }

    @Test
    public void executeWithoutWriteConcern() {
        DBObject data = new BasicDBObject(key1, "obj1");

        DBObject before = coll.findOne(data);

        Assert.assertNotNull(before);

        WriteResult result = new RemoveCommand(null, coll, data).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        DBObject after = coll.findOne(data);

        Assert.assertNull(after);
    }
}
