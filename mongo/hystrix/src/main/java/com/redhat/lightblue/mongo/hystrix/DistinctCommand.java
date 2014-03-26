/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import java.util.List;

/**
 *
 * @author nmalik
 */
public class DistinctCommand extends AbstractMongoCommand<List> {
    private final String key;

    public DistinctCommand(String clientKey, DBCollection collection, String key) {
        super(DistinctCommand.class.getSimpleName(), DistinctCommand.class.getSimpleName(), clientKey, collection);
        this.key = key;
    }

    @Override
    protected List run() throws Exception {
        return getDBCollection().distinct(key);
    }
}
