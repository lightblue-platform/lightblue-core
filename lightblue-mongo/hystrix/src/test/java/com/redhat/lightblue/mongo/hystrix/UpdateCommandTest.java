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
public class UpdateCommandTest extends AbstractMongoTest {
    @Test
    public void executeWithWriteConcern() {
        DBObject query = new BasicDBObject(key1, "obj1");
        String newValue = "new value";

        DBObject obj = coll.findOne(query);

        Assert.assertNotNull(obj);
        Assert.assertNotEquals(newValue, obj.get(key2));

        DBObject set = new BasicDBObject();
        set.put("$set", new BasicDBObject(key2, newValue));
        //public UpdateCommand(String clientKey, DBCollection collection, DBObject query, DBObject update, boolean upsert, boolean multi, WriteConcern concern) {
        WriteResult result = new UpdateCommand(coll,
                query,
                set,
                false,
                false,
                WriteConcern.SAFE).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        // verify data change
        DBObject updated = coll.findOne(query);

        Assert.assertNotNull(updated);
        Assert.assertEquals(newValue, updated.get(key2));
    }

    @Test
    public void executeWithoutWriteConcern() {
        DBObject query = new BasicDBObject(key1, "obj1");
        String newValue = "new value";

        DBObject obj = coll.findOne(query);

        Assert.assertNotNull(obj);
        Assert.assertNotEquals(newValue, obj.get(key2));

        DBObject set = new BasicDBObject();
        set.put("$set", new BasicDBObject(key2, newValue));
        //public UpdateCommand(String clientKey, DBCollection collection, DBObject query, DBObject update, boolean upsert, boolean multi, WriteConcern concern) {
        WriteResult result = new UpdateCommand(coll,
                query,
                set,
                false,
                false).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        // verify data change
        DBObject updated = coll.findOne(query);

        Assert.assertNotNull(updated);
        Assert.assertEquals(newValue, updated.get(key2));
    }
}
