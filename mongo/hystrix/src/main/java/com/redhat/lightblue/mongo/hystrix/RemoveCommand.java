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
public class RemoveCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject data;
    private final WriteConcern concern;

    public RemoveCommand(String clientKey, DBCollection collection, DBObject data) {
        this(clientKey, collection, data, null);
    }

    public RemoveCommand(String clientKey, DBCollection collection, DBObject data, WriteConcern concern) {
        super(RemoveCommand.class.getSimpleName(), RemoveCommand.class.getSimpleName(), clientKey, collection);
        this.data = data;
        this.concern = concern;
    }

    @Override
    protected WriteResult run() throws Exception {
        if (concern != null) {
            return getDBCollection().remove(data, concern);
        } else {
            return getDBCollection().remove(data);
        }
    }
}
