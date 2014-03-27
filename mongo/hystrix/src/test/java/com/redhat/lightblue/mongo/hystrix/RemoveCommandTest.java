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
