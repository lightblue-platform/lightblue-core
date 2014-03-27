/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
