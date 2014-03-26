/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 *
 * @author nmalik
 */
public class InsertCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject[] data;
    private final WriteConcern concern;

    public InsertCommand(String clientKey, DBCollection collection, DBObject data, WriteConcern concern) {
        this(clientKey, collection, new DBObject[]{data}, concern);
    }

    public InsertCommand(String clientKey, DBCollection collection, DBObject[] data, WriteConcern concern) {
        super(InsertCommand.class.getSimpleName(), InsertCommand.class.getSimpleName(), clientKey, collection);
        this.data = data;
        this.concern = concern;
    }

    @Override
    protected WriteResult run() throws Exception {
        return getDBCollection().insert(data, concern);
    }
}
