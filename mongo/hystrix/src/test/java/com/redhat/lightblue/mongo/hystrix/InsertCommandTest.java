/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
