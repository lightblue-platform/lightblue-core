/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Hystrix command for executing findOne on a MongoDB collection.
 *
 * @author nmalik
 */
public class FindCommand extends AbstractMongoCommand<DBCursor> {
    private final DBObject query;
    private final DBObject projection;

    /**
     *
     * @param clientKey used to set thread pool key
     * @param query
     */
    public FindCommand(String clientKey, DBCollection collection, DBObject query, DBObject projection) {
        super(FindCommand.class.getSimpleName(), FindCommand.class.getSimpleName(), clientKey, collection);
        this.query = query;
        this.projection = projection;
    }

    @Override
    protected DBCursor run() throws Exception {
        return getDBCollection().find(query, projection);
    }
}
