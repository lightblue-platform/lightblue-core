/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.BasicDBObject;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class DistinctCommandTest extends AbstractMongoTest {
    @Test
    public void execute() {
        String key = "name";
        
        // setup data
        coll.insert(new BasicDBObject(key, "obj1"));
        coll.insert(new BasicDBObject(key, "obj2"));
        coll.insert(new BasicDBObject(key, "obj2"));
        coll.insert(new BasicDBObject(key, "obj3"));
        coll.insert(new BasicDBObject(key, "obj4"));
        coll.insert(new BasicDBObject(key, "obj4"));
        coll.insert(new BasicDBObject(key, "obj4"));
        
        Assert.assertEquals(7, coll.find().count());
        
        List values = new DistinctCommand(null, coll, key).execute();
        
        Assert.assertEquals(4, values.size());
        Assert.assertTrue(values.contains("obj1"));
        Assert.assertTrue(values.contains("obj2"));
        Assert.assertTrue(values.contains("obj3"));
        Assert.assertTrue(values.contains("obj4"));
    }
}
