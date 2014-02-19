/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.metadata.mongo;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.bson.BSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.StatusChange;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.metadata.types.DefaultTypes;
import com.redhat.lightblue.mongo.MongoConfiguration;
import com.redhat.lightblue.util.Error;

public class MongoMetadata implements Metadata {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_METADATA_COLLECTION = "metadata";

    private static final String LITERAL_ID = "_id";
    private static final String LITERAL_ENTITY_NAME = "entityName";
    private static final String LITERAL_VERSION = "version";
    private static final String LITERAL_NAME = "name";

    private final transient DBCollection collection;

    private final transient BSONParser mdParser;

    public static MongoMetadata create(MongoConfiguration configuration) throws UnknownHostException {
        DB db = configuration.getDB();

        Extensions<BSONObject> parserExtensions = new Extensions<>();
        parserExtensions.addDefaultExtensions();
        parserExtensions.registerDataStoreParser("mongo", new MongoDataStoreParser<BSONObject>());
        DefaultTypes typeResolver = new DefaultTypes();
        return new MongoMetadata(db, parserExtensions, typeResolver);
    }

    public MongoMetadata(DB db,
                         String metadataCollection,
                         Extensions<BSONObject> parserExtensions,
                         TypeResolver typeResolver) {
        this.collection = db.getCollection(metadataCollection);
        this.mdParser = new BSONParser(parserExtensions, typeResolver);
    }

    public MongoMetadata(DB db,
                         Extensions<BSONObject> parserExtensions,
                         TypeResolver typeResolver) {
        this(db, DEFAULT_METADATA_COLLECTION, parserExtensions, typeResolver);
    }

    @Override
    public EntityMetadata getEntityMetadata(String entityName,
                                            String version) {
        if (entityName == null || entityName.length() == 0) {
            throw new IllegalArgumentException(LITERAL_ENTITY_NAME);
        }
        if (version == null || version.length() == 0) {
            throw new IllegalArgumentException(LITERAL_VERSION);
        }

        Error.push("getEntityMetadata(" + entityName + ":" + version + ")");
        try {
            EntityInfo info = getEntityInfo(entityName);
            EntitySchema schema;

            BasicDBObject query = new BasicDBObject(LITERAL_ID, entityName + BSONParser.DELIMITER_ID + version);
            DBObject es = collection.findOne(query);
            if (es != null) {
                schema = mdParser.parseEntitySchema(es);
            } else {
                schema = null;
            }

            return new EntityMetadata(info, schema);
        } finally {
            Error.pop();
        }
    }

    public EntityInfo getEntityInfo(String entityName) {
        if (entityName == null || entityName.length() == 0) {
            throw new IllegalArgumentException(LITERAL_ENTITY_NAME);
        }

        Error.push("getEntityInfo(" + entityName + ")");
        try {
            BasicDBObject query = new BasicDBObject(LITERAL_ID, entityName + BSONParser.DELIMITER_ID);
            DBObject ei = collection.findOne(query);
            if (ei != null) {
                return mdParser.parseEntityInfo(ei);
            } else {
                return null;
            }
        } finally {
            Error.pop();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String[] getEntityNames() {
        Error.push("getEntityNames");
        try {
            List l = collection.distinct(LITERAL_NAME);
            String[] arr = new String[l.size()];
            int i = 0;
            for (Object x : l) {
                arr[i++] = x.toString();
            }
            return arr;
        } finally {
            Error.pop();
        }
    }

    @Override
    public Version[] getEntityVersions(String entityName) {
        if (entityName == null || entityName.length() == 0) {
            throw new IllegalArgumentException(LITERAL_ENTITY_NAME);
        }
        Error.push("getEntityVersions(" + entityName + ")");
        try {
            // query by name but only return documents that have a version
            BasicDBObject query = new BasicDBObject(LITERAL_NAME, entityName)
                    .append(LITERAL_VERSION, new BasicDBObject("$exists", 1));
            BasicDBObject project = new BasicDBObject(LITERAL_VERSION, 1);
            project.append(LITERAL_ID, 0);
            DBCursor cursor = collection.find(query, project);
            int n = cursor.count();
            Version[] ret = new Version[n];
            int i = 0;
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                ret[i++] = mdParser.parseVersion((BSONObject) object.get(LITERAL_VERSION));
            }
            return ret;
        } finally {
            Error.pop();
        }
    }

    @Override
    public void createNewMetadata(EntityMetadata md) {

        checkMetadataHasName(md);
        checkMetadataHasFields(md);
        checkDataStoreIsValid(md);
        Version ver = checkVersionIsValid(md);

        Error.push("createNewMetadata(" + md.getName() + ")");

        // write info and schema as separate docs!
        try {
            PredefinedFields.ensurePredefinedFields(md);
            DBObject infoObj = (DBObject) mdParser.convert(md.getEntityInfo());
            DBObject schemaObj = (DBObject) mdParser.convert(md.getEntitySchema());

            Error.push("writeEntityInfo");
            try {
                WriteResult result = collection.insert(infoObj, WriteConcern.SAFE);
                String error = result.getError();
                if (error != null) {
                    throw Error.get(MongoMetadataConstants.ERR_DB_ERROR, error);
                }
            } catch (MongoException.DuplicateKey dke) {
                throw Error.get(MongoMetadataConstants.ERR_DUPLICATE_METADATA, ver.getValue());
            } finally {
                Error.pop();
            }

            Error.push("writeEntitySchema");
            try {
                WriteResult result = collection.insert(schemaObj, WriteConcern.SAFE);
                String error = result.getError();
                if (error != null) {
                    throw Error.get(MongoMetadataConstants.ERR_DB_ERROR, error);
                }
            } catch (MongoException.DuplicateKey dke) {
                throw Error.get(MongoMetadataConstants.ERR_DUPLICATE_METADATA, ver.getValue());
            } finally {
                Error.pop();
            }

        } finally {
            Error.pop();
        }
    }

    /**
     * Creates a new schema (versioned data) for an existing metadata.
     *
     * @param md
     */
    public void createNewSchema(EntityMetadata md) {

        checkMetadataHasName(md);
        checkMetadataHasFields(md);
        checkDataStoreIsValid(md);
        Version ver = checkVersionIsValid(md);

        Error.push("createNewSchema(" + md.getName() + ")");

        try {
            // verify entity info exists
            EntityInfo info = getEntityInfo(md.getName());

            if (null == info) {
                throw Error.get(MongoMetadataConstants.ERR_MISSING_ENTITY_INFO, md.getName());
            }

            PredefinedFields.ensurePredefinedFields(md);
            DBObject schemaObj = (DBObject) mdParser.convert(md.getEntitySchema());

            WriteResult result = collection.insert(schemaObj, WriteConcern.SAFE);
            String error = result.getError();
            if (error != null) {
                throw Error.get(MongoMetadataConstants.ERR_DB_ERROR, error);
            }

        } catch (MongoException.DuplicateKey dke) {
            throw Error.get(MongoMetadataConstants.ERR_DUPLICATE_METADATA, ver.getValue());
        } finally {
            Error.pop();
        }
    }

    private Version checkVersionIsValid(EntityMetadata md) {
        Version ver = md.getVersion();
        if (ver == null || ver.getValue() == null || ver.getValue().length() == 0) {
            throw new IllegalArgumentException("Invalid version");
        }
        if (ver.getValue().indexOf(' ') != -1) {
            throw new IllegalArgumentException("Invalid version number");
        }
        return ver;
    }

    private void checkDataStoreIsValid(EntityMetadata md) {
        DataStore store = md.getDataStore();
        if (!(store instanceof MongoDataStore)) {
            throw new IllegalArgumentException("Invalid datastore");
        }
    }

    private void checkMetadataHasName(EntityMetadata md) {
        if (md.getName() == null || md.getName().length() == 0) {
            throw new IllegalArgumentException("Empty metadata name");
        }
    }

    private void checkMetadataHasFields(EntityMetadata md) {
        if (md.getFields().getNumChildren() <= 0) {
            throw new IllegalArgumentException("Metadata without any fields");
        }
    }

    @Override
    public void setMetadataStatus(String entityName,
                                  String version,
                                  MetadataStatus newStatus,
                                  String comment) {

        if (entityName == null || entityName.length() == 0) {
            throw new IllegalArgumentException(LITERAL_ENTITY_NAME);
        }
        if (version == null || version.length() == 0) {
            throw new IllegalArgumentException(LITERAL_VERSION);
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("status");
        }
        BasicDBObject query = new BasicDBObject(LITERAL_ID, entityName + BSONParser.DELIMITER_ID + version);
        Error.push("setMetadataStatus(" + entityName + ":" + version + ")");
        try {
            DBObject md = collection.findOne(query);
            if (md == null) {
                throw Error.get(MongoMetadataConstants.ERR_UNKNOWN_VERSION, entityName + ":" + version);
            }
            EntitySchema schema = mdParser.parseEntitySchema(md);

            StatusChange newLog = new StatusChange();
            newLog.setDate(new Date());
            newLog.setStatus(schema.getStatus());
            newLog.setComment(comment);
            schema.getStatusChangeLog().add(newLog);
            schema.setStatus(newStatus);

            query = new BasicDBObject(LITERAL_ID, md.get(LITERAL_ID));
            WriteResult result = collection.
                    update(query, (DBObject) mdParser.convert(schema), false, false);
            String error = result.getError();
            if (error != null) {
                throw Error.get(MongoMetadataConstants.ERR_DB_ERROR, error);
            }
        } finally {
            Error.pop();
        }
    }
}
