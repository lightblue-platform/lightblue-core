/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Hystrix command for executing findOne on a MongoDB collection.
 *
 * @author nmalik
 */
public class FindOneCommand extends AbstractMongoCommand<DBObject> {
    private final DBObject query;

    /**
     *
     * @param clientKey used to set thread pool key
     * @param query
     */
    public FindOneCommand(String clientKey, DBCollection collection, DBObject query) {
        super(FindOneCommand.class.getSimpleName(), FindOneCommand.class.getSimpleName(), clientKey, collection);
        this.query = query;
    }

    @Override
    protected DBObject run() throws Exception {
        return getDBCollection().findOne(query);
    }
}
