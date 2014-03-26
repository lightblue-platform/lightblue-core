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
public class SaveCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject data;

    public SaveCommand(String clientKey, DBCollection collection, DBObject data) {
        super(SaveCommand.class.getSimpleName(), SaveCommand.class.getSimpleName(), clientKey, collection);
        this.data = data;
    }

    @Override
    protected WriteResult run() throws Exception {
        return getDBCollection().save(data);
    }
}
