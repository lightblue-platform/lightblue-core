/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class FindOneCommandTest extends AbstractMongoTest {
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
