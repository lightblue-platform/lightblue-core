/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.crud.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.redhat.lightblue.crud.CRUDDeleteResponse;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.Operation;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class BasicDocDeleterTest extends AbstractMongoTest {
    @Test
    public void delete() {
        // setup data to delete
        String id = "deleteTest1";
        DBObject obj = new BasicDBObject();
        obj.put("_id", id);
        WriteResult wr = coll.insert(obj);

        // check that insert happened
        Assert.assertTrue(wr.getError() == null);

        Assert.assertEquals("count on collection", 1, coll.find(null).count());

        // execute delete
        BasicDocDeleter deleter = new BasicDocDeleter();
        CRUDOperationContext ctx = new OCtx(Operation.DELETE);
        DBObject mongoQuery = new BasicDBObject();
        mongoQuery.put("_id", id);
        CRUDDeleteResponse response = new CRUDDeleteResponse();
        deleter.delete(ctx, coll, mongoQuery, response);

        Assert.assertTrue(response.getNumDeleted() == 1);

        // verify nothing left in collection
        Assert.assertEquals("count on collection", 0, coll.find(null).count());
    }
}
