/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class FindOneCommandTest extends AbstractMongoTest {
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
        // doesn't support projection because there is no need at this time.
        DBObject query = new BasicDBObject(key1, "obj4");
        DBObject obj = new FindOneCommand(null, coll, query).execute();

        Assert.assertNotNull(obj);
        Assert.assertNotNull(obj.get(key1));
        Assert.assertNotNull(obj.get(key2));
    }
}
