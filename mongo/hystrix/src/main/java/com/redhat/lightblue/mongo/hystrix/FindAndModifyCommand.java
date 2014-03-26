/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.hystrix;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 *
 * @author nmalik
 */
public class FindAndModifyCommand extends AbstractMongoCommand<DBObject> {
    private final DBObject query;
    private final DBObject fields;
    private final DBObject sort;
    private final boolean remove;
    private final DBObject update;
    private final boolean returnNew;
    private final boolean upsert;

    //public DBObject findAndModify(DBObject query, DBObject fields, DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert){
    public FindAndModifyCommand(String clientKey, DBCollection collection, DBObject query, DBObject fields,
                                DBObject sort, boolean remove, DBObject update, boolean returnNew, boolean upsert) {
        super(FindAndModifyCommand.class.getSimpleName(), FindAndModifyCommand.class.getSimpleName(), clientKey, collection);
        this.query = query;
        this.fields = fields;
        this.sort = sort;
        this.remove = remove;
        this.update = update;
        this.returnNew = returnNew;
        this.upsert = upsert;
    }

    @Override
    protected DBObject run() throws Exception {
        return getDBCollection().findAndModify(query, fields, sort, remove, update, returnNew, upsert);
    }
}
