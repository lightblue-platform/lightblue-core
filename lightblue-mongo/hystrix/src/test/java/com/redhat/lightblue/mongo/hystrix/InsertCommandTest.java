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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class InsertCommandTest extends AbstractMongoTest {
    @Before
    @Override
    public void setup() {
        // override default behavior from abstract test setup
        // don't want what is setup for the rest of tests extending from the abstract test class
    }

    @Test
    public void executeOne() {
        // doesn't support projection because there is no need at this time.
        DBObject query = new BasicDBObject("name", "obj");
        WriteResult result = new InsertCommand(null, coll, query, WriteConcern.SAFE).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        DBCursor cur = coll.find(query, null);

        Assert.assertNotNull(cur);
        Assert.assertEquals(1, cur.size());

        DBObject obj = cur.next();

        Assert.assertNotNull(obj);
        Assert.assertEquals("obj", obj.get("name"));
    }

    @Test
    public void executeMany() {
        // doesn't support projection because there is no need at this time.
        DBObject[] data = new BasicDBObject[]{
            new BasicDBObject("name", "obj1"),
            new BasicDBObject("name", "obj2"),
            new BasicDBObject("name", "obj3")
        };
        WriteResult result = new InsertCommand(null, coll, data, WriteConcern.SAFE).execute();

        Assert.assertNotNull(result);
        Assert.assertNull(result.getError());

        DBCursor cur = coll.find(null, null);

        Assert.assertNotNull(cur);
        Assert.assertEquals(3, cur.size());

        // check first object
        DBObject obj = cur.next();

        Assert.assertNotNull(obj);
        Assert.assertEquals("obj1", obj.get("name"));
    }
}
