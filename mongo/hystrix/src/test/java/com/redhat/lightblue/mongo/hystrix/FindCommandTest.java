/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class FindCommandTest extends AbstractMongoTest {
    private final String key1 = "name";
    private final String key2 = "foo";

    @Before
    public void setup() {
        // setup data
        int count = 0;
        for (int i = 1; i < 5; i++) {
            for (int x = 1; x < i + 1; x++) {
                DBObject obj = new BasicDBObject(key1, "obj" + i);
                obj.put(key2, "bar" + x);
                coll.insert(obj);
                count++;
            }
        }

        Assert.assertEquals(count, coll.find().count());
    }

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
