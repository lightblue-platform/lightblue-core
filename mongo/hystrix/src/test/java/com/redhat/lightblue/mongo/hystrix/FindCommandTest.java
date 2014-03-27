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
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class FindCommandTest extends AbstractMongoTest {
    @Test
    public void executeWithoutProjection() {
        int expectedCount = 4;

        DBObject query = new BasicDBObject(key1, "obj" + expectedCount);
        DBCursor cur = new FindCommand(null, coll, query, null).execute();

        Assert.assertEquals(expectedCount, cur.count());

        int count = 0;
        while (cur.hasNext()) {
            DBObject obj = cur.next();
            Assert.assertNotNull(obj);
            Assert.assertNotNull(obj.get(key1));
            Assert.assertNotNull(obj.get(key2));
            count++;
        }
        Assert.assertEquals(expectedCount, count);
    }

    @Test
    public void executeWithProjection() {
        int expectedCount = 3;

        DBObject query = new BasicDBObject(key1, "obj" + expectedCount);
        DBObject projection = new BasicDBObject(key2, 0);
        DBCursor cur = new FindCommand(null, coll, query, projection).execute();

        Assert.assertEquals(expectedCount, cur.count());

        int count = 0;
        while (cur.hasNext()) {
            DBObject obj = cur.next();
            Assert.assertNotNull(obj);
            Assert.assertNotNull(obj.get(key1));
            Assert.assertNull(obj.get(key2));
            count++;
        }
        Assert.assertEquals(expectedCount, count);
    }
}
