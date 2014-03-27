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
public class FindAndModifyCommandTest extends AbstractMongoTest {
    @Test
    public void execute() {
        String newValue = "new value";
        DBObject update = new BasicDBObject(key2, newValue);
        DBObject modifiedDoc = new FindAndModifyCommand(null, coll,
                new BasicDBObject(key1, "obj1"),
                null,
                null,
                false,
                update,
                true,
                false).execute();

        Assert.assertNotNull(modifiedDoc);
        Assert.assertEquals(newValue, modifiedDoc.get(key2));
    }
}
