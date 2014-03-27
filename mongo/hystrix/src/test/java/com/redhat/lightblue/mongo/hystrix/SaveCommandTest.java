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
import com.mongodb.WriteResult;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class SaveCommandTest extends AbstractMongoTest {
    @Test
    public void executeWithId() {
        DBObject obj = coll.findOne(new BasicDBObject(key1, "obj2"));
        Object id = obj.get("_id");

        obj.put("newKey", "key value");

        WriteResult result = new SaveCommand(null, coll, obj).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        DBObject updated = coll.findOne(new BasicDBObject("_id", id));

        Assert.assertEquals("key value", updated.get("newKey"));
    }

    @Test
    public void executeWithoutId() {
        DBObject obj = coll.find(new BasicDBObject(key1, "obj2")).sort(new BasicDBObject("_id", -1)).next();
        Object id = obj.get("_id");

        DBObject save = new BasicDBObject(key1, obj.get(key1));
        save.put("newKey", "key value");

        WriteResult result = new SaveCommand(null, coll, save).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        DBObject updated = coll.findOne(new BasicDBObject("newKey", "key value"));

        // verify wrote new object
        Assert.assertNotEquals(id, updated.get("_id"));
        Assert.assertEquals("key value", updated.get("newKey"));
    }
}
