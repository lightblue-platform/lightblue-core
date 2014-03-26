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
public class UpdateCommand extends AbstractMongoCommand<WriteResult> {
    private final DBObject query;
    private final DBObject update;
    private final boolean upsert;
    private final boolean multi;
    private final WriteConcern concern;

    public UpdateCommand(String clientKey, DBCollection collection, DBObject query, DBObject update, boolean upsert, boolean multi) {
        this(clientKey, collection, query, update, upsert, multi, null);
    }

    public UpdateCommand(String clientKey, DBCollection collection, DBObject query, DBObject update, boolean upsert, boolean multi, WriteConcern concern) {
        super(UpdateCommand.class.getSimpleName(), UpdateCommand.class.getSimpleName(), clientKey, collection);
        this.query = query;
        this.update = update;
        this.upsert = upsert;
        this.multi = multi;
        this.concern = concern;
    }

    @Override
    protected WriteResult run() throws Exception {
        if (concern != null) {
            return getDBCollection().update(query, update, upsert, multi, concern);
        } else {
            return getDBCollection().update(query, update, upsert, multi);
        }
    }
}
