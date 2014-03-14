/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.mongo.config.metadata;

import com.mongodb.DB;
import com.redhat.lightblue.metadata.mongo.MongoDataStoreParser;
import com.redhat.lightblue.metadata.mongo.MongoMetadata;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import java.net.UnknownHostException;
import org.bson.BSONObject;

/**
 *
 * @author lcestari
 */
public final class MongoMetadataFactory {
    public static MongoMetadata create(MongoConfiguration configuration) throws UnknownHostException {
        DB db = configuration.getDB();
        Extensions<BSONObject> parserExtensions = new Extensions<>();
        parserExtensions.addDefaultExtensions();
        parserExtensions.registerDataStoreParser("mongo", new MongoDataStoreParser<BSONObject>());
        DefaultTypes typeResolver = new DefaultTypes();
        return new MongoMetadata(db, parserExtensions, typeResolver);
    }

}
