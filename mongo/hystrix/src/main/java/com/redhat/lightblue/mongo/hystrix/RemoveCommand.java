/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 *
 * @author nmalik
 */
public class RemoveCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject data;

    /**
     *
     * @param clientKey used to set thread pool key
     * @param query
     */
    public RemoveCommand(String clientKey, DBCollection collection, DBObject data) {
        super(RemoveCommand.class.getSimpleName(), RemoveCommand.class.getSimpleName(), clientKey, collection);
        this.data = data;
    }

    @Override
    protected WriteResult run() throws Exception {
        return getDBCollection().remove(data);
    }
}
