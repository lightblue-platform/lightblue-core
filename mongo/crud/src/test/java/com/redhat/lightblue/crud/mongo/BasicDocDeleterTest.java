/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
