/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        WriteResult result = new UpdateCommand(null, coll,
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
        WriteResult result = new UpdateCommand(null, coll,
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
