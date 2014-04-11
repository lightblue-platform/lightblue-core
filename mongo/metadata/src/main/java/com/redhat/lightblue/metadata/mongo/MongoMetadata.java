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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.redhat.lightblue.DataError;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.metadata.DataStore;
import com.redhat.lightblue.metadata.EntityAccess;
import com.redhat.lightblue.metadata.EntityInfo;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.EntitySchema;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.FieldAccess;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Index;
import com.redhat.lightblue.metadata.Indexes;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.PredefinedFields;
import com.redhat.lightblue.metadata.StatusChange;
import com.redhat.lightblue.metadata.TypeResolver;
import com.redhat.lightblue.metadata.Version;
import com.redhat.lightblue.metadata.parser.Extensions;
import com.redhat.lightblue.mongo.hystrix.DistinctCommand;
import com.redhat.lightblue.mongo.hystrix.FindCommand;
import com.redhat.lightblue.mongo.hystrix.FindOneCommand;
import com.redhat.lightblue.mongo.hystrix.InsertCommand;
import com.redhat.lightblue.mongo.hystrix.RemoveCommand;
import com.redhat.lightblue.mongo.hystrix.UpdateCommand;
import com.redhat.lightblue.query.SortKey;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

public class MongoMetadata implements Metadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoMetadata.class);

    public static final String DEFAULT_METADATA_COLLECTION = "metadata";

    private static final String LITERAL_ID = "_id";
    private static final String LITERAL_ENTITY_NAME = "entityName";
    private static final String LITERAL_VERSION = "version";
    private static final String LITERAL_NAME = "name";

    private final transient DBCollection collection;

    private final transient BSONParser mdParser;

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

        Error.push("getEntityMetadata(" + entityName + ":" + version + ")");
        try {
            EntityInfo info = getEntityInfo(entityName);
            if (version == null || version.length() == 0) {
                if (info.getDefaultVersion() == null || info.getDefaultVersion().length() == 0) {
                    throw new IllegalArgumentException(LITERAL_VERSION);
                } else {
                    version = info.getDefaultVersion();
                }
            }

            EntitySchema schema;

            BasicDBObject query = new BasicDBObject(LITERAL_ID, entityName + BSONParser.DELIMITER_ID + version);
            DBObject es = new FindOneCommand(null, collection, query).execute();
            if (es != null) {
                schema = mdParser.parseEntitySchema(es);
            } else {
                throw Error.get(MongoMetadataConstants.ERR_UNKNOWN_VERSION, entityName + ":" + version);
            }
            return new EntityMetadata(info, schema);
        } finally {
            Error.pop();
        }
    }
    
    @Override
    public EntityInfo getEntityInfo(String entityName) {
        if (entityName == null || entityName.length() == 0) {
            throw new IllegalArgumentException(LITERAL_ENTITY_NAME);
        }

        Error.push("getEntityInfo(" + entityName + ")");
        try {
            BasicDBObject query = new BasicDBObject(LITERAL_ID, entityName + BSONParser.DELIMITER_ID);
            DBObject ei = new FindOneCommand(null, collection, query).execute();
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
            List l = new DistinctCommand(null, collection, LITERAL_NAME).execute();
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
            DBCursor cursor = new FindCommand(null, collection, query, project).execute();
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
        LOGGER.debug("createNewMetadata: begin");
        checkMetadataHasName(md);
        checkMetadataHasFields(md);
        checkDataStoreIsValid(md);
        Version ver = checkVersionIsValid(md);
        LOGGER.debug("createNewMetadata: version {}",ver);

        Error.push("createNewMetadata(" + md.getName() + ")");

        // write info and schema as separate docs!
        try {

            if (md.getEntityInfo().getDefaultVersion() != null) {
                if(!md.getEntityInfo().getDefaultVersion().equals(ver.getValue())) {
                    validateDefaultVersion(md.getEntityInfo());
                }
                if (md.getStatus() == MetadataStatus.DISABLED) {
                    throw Error.get(MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION, md.getName() + ":" + md.getEntityInfo().getDefaultVersion());
                }
            }
            LOGGER.debug("createNewMetadata: Default version validated");
            PredefinedFields.ensurePredefinedFields(md);
            DBObject infoObj = (DBObject) mdParser.convert(md.getEntityInfo());
            DBObject schemaObj = (DBObject) mdParser.convert(md.getEntitySchema());

            Error.push("writeEntity");
            try {
                WriteResult result = new InsertCommand(null, collection, new DBObject[]{infoObj, schemaObj}, WriteConcern.SAFE).execute();
                LOGGER.debug("createNewMetadata: insertion complete");
                String error = result.getError();
                if (error != null) {
                    cleanup(infoObj.get(LITERAL_ID), schemaObj.get(LITERAL_ID));
                    LOGGER.error("createNewMetadata: error {}"+error);
                    throw Error.get(MongoMetadataConstants.ERR_DB_ERROR, error);
                }
                createUpdateEntityInfoIndexes(md.getEntityInfo());
            } catch (MongoException.DuplicateKey dke) {
                LOGGER.error("createNewMetadata: duplicateKey {}",dke);
                cleanup(infoObj.get(LITERAL_ID), schemaObj.get(LITERAL_ID));
                throw Error.get(MongoMetadataConstants.ERR_DUPLICATE_METADATA, ver.getValue());
            } finally {
                Error.pop();
            }
        } finally {
            Error.pop();
        }
        LOGGER.debug("createNewMetadata: end");
    }

    private void createUpdateEntityInfoIndexes(EntityInfo ei) {
        LOGGER.debug("createUpdateEntityInfoIndexes: begin");
        
        MongoDataStore ds = (MongoDataStore) ei.getDataStore();
        Indexes indexes = ei.getIndexes();
        DBCollection entityCollection = collection.getDB().getCollection(ds.getCollectionName());  
        // TODO: This is broken. It assumes entity collection is in the same DB as metadata. We have to:
        //  1) Move DBResolver from crud to a more common place, possibly metadata
        //  2) Pass an implementation of DBResolver to MongoMetadata to find the DB
        
        Error.push("createUpdateIndex");
        try {
            for (Index index: indexes.getIndexes()) {
                DBObject newIndex = new BasicDBObject();
                for(SortKey p : index.getFields()) {
                    newIndex.put(p.toString(), p.isDesc() ? -1 : 1);    
                }
                
                for(DBObject existingIndex: entityCollection.getIndexInfo()) {
                    if(indexFieldsMatch(index, existingIndex) && !indexOptionsMatch(index, existingIndex)) {
                        entityCollection.dropIndex(existingIndex.get("name").toString());
                        break;
                    }
                }
                
                entityCollection.ensureIndex(newIndex, index.getName(), index.isUnique());
            }            
        } catch(MongoException me) {
            LOGGER.error("createUpdateEntityInfoIndexes: {}", ei);
            throw Error.get(MongoMetadataConstants.ERR_ENTITY_INDEX_NOT_CREATED);
        } finally {
            Error.pop();
        }
        
        LOGGER.debug("createUpdateEntityInfoIndexes: end");
    }

    private boolean compareSortKeys(SortKey sortKey,String fieldName,Object dir) {
        if(sortKey.getField().toString().equals(fieldName)) {
            int direction=((Number)dir).intValue();
            return sortKey.isDesc()==(direction<0);
        }
        return false;
    }
    
    private boolean indexFieldsMatch(Index index, DBObject existingIndex) {
        BasicDBObject keys=(BasicDBObject)existingIndex.get("key");
        if(keys!=null) {
            List<SortKey> fields=index.getFields();
            if(keys.size()==fields.size()) {
                Iterator<SortKey> sortKeyItr=fields.iterator();
                for(Map.Entry<String,Object> entry:keys.entrySet()) {
                    SortKey sortKey=sortKeyItr.next();
                    if(!compareSortKeys(sortKey,entry.getKey(),entry.getValue())) {
                        return false;
                    }
                }
                return true;
            }
        }
        return true;
    }

    private boolean indexOptionsMatch(Index index, DBObject existingIndex) {
        if(existingIndex.get("unique").equals(index.isUnique())) {
            return true;
        }
        return false;
    }

    private void cleanup(Object... ids) {
        for (Object id : ids) {
            if (id != null) {
                try {
                    new RemoveCommand(null, collection, new BasicDBObject(LITERAL_ID, id)).execute();
                } catch (Exception e) {
                }
            }
        }
    }

    private void validateDefaultVersion(EntityInfo ei) {
        if (ei.getDefaultVersion() != null) {
            BasicDBObject query = new BasicDBObject(LITERAL_ID, ei.getName() + BSONParser.DELIMITER_ID + ei.getDefaultVersion());
            DBObject es = collection.findOne(query);
            if (es == null) {
                throw Error.get(MongoMetadataConstants.ERR_INVALID_DEFAULT_VERSION, ei.getName() + ":" + ei.getDefaultVersion());
            }
        }
    }

    @Override
    public void updateEntityInfo(EntityInfo ei) {
        checkMetadataHasName(ei);
        checkDataStoreIsValid(ei);
        Error.push("updateEntityInfo("+ei.getName()+")");

        // Verify entity info exists
        EntityInfo old=getEntityInfo(ei.getName());
        if (null == old) {
            throw Error.get(MongoMetadataConstants.ERR_MISSING_ENTITY_INFO, ei.getName());
        }
        if( (old.getDefaultVersion()==null&&ei.getDefaultVersion()!=null) ||
            (old.getDefaultVersion()!=null&&ei.getDefaultVersion()!=null&&
             !old.getDefaultVersion().equals(ei.getDefaultVersion())) )
            validateDefaultVersion(ei);

        try {
            collection.update(new BasicDBObject(LITERAL_ID, ei.getName() + BSONParser.DELIMITER_ID),
                              (DBObject)mdParser.convert(ei));
            createUpdateEntityInfoIndexes(ei);
        } catch (Exception e) {
            throw Error.get(MongoMetadataConstants.ERR_DB_ERROR,e.toString());
        }
    }

    /**
     * Creates a new schema (versioned data) for an existing metadata.
     *
     * @param md
     */
    @Override
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

            WriteResult result = new InsertCommand(null, collection, schemaObj, WriteConcern.SAFE).execute();
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
        return checkVersionIsValid(md.getEntitySchema());
    }

    private Version checkVersionIsValid(EntitySchema md) {
        Version ver = md.getVersion();
        if (ver == null || ver.getValue() == null || ver.getValue().length() == 0) {
            throw new IllegalArgumentException(MongoMetadataConstants.ERR_INVALID_VERSION);
        }
        if (ver.getValue().indexOf(' ') != -1) {
            throw new IllegalArgumentException(MongoMetadataConstants.ERR_INVALID_VERSION_NUMBER);
        }
        return ver;
    }

    private void checkDataStoreIsValid(EntityMetadata md) {
        checkDataStoreIsValid(md.getEntityInfo());
    }

    private void checkDataStoreIsValid(EntityInfo md) {
        DataStore store = md.getDataStore();
        if (!(store instanceof MongoDataStore)) {
            throw new IllegalArgumentException(MongoMetadataConstants.ERR_INVALID_DATASTORE);
        }
    }

    private void checkMetadataHasName(EntityMetadata md) {
        checkMetadataHasName(md.getEntityInfo());
    }
    
    private void checkMetadataHasName(EntityInfo md) {
        if (md.getName() == null || md.getName().length() == 0) {
            throw new IllegalArgumentException(MongoMetadataConstants.ERR_EMPTY_METADATA_NAME);
        }
    }

    private void checkMetadataHasFields(EntityMetadata md) {
        checkMetadataHasFields(md.getEntitySchema());
    }

    private void checkMetadataHasFields(EntitySchema md) {
        if (md.getFields().getNumChildren() <= 0) {
            throw new IllegalArgumentException(MongoMetadataConstants.ERR_METADATA_WITH_NO_FIELDS);
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
            throw new IllegalArgumentException(MongoMetadataConstants.ERR_NEW_STATUS_IS_NULL);
        }
        BasicDBObject query = new BasicDBObject(LITERAL_ID, entityName + BSONParser.DELIMITER_ID + version);
        Error.push("setMetadataStatus(" + entityName + ":" + version + ")");
        try {
            DBObject md = new FindOneCommand(null, collection, query).execute();
            if (md == null) {
                throw Error.get(MongoMetadataConstants.ERR_UNKNOWN_VERSION, entityName + ":" + version);
            }

            EntityInfo info = getEntityInfo(entityName);
            if (info.getDefaultVersion() != null && info.getDefaultVersion().contentEquals(version) && newStatus == MetadataStatus.DISABLED) {
                throw Error.get(MongoMetadataConstants.ERR_DISABLED_DEFAULT_VERSION, entityName + ":" + version);
            }

            EntitySchema schema = mdParser.parseEntitySchema(md);
            StatusChange newLog = new StatusChange();
            newLog.setDate(new Date());
            newLog.setStatus(schema.getStatus());
            newLog.setComment(comment);
            schema.getStatusChangeLog().add(newLog);
            schema.setStatus(newStatus);

            query = new BasicDBObject(LITERAL_ID, md.get(LITERAL_ID));
            WriteResult result = new UpdateCommand(null, collection, query, (DBObject) mdParser.convert(schema), false, false).execute();
            String error = result.getError();
            if (error != null) {
                throw Error.get(MongoMetadataConstants.ERR_DB_ERROR, error);
            }
        } finally {
            Error.pop();
        }
    }

    @Override
    public Response getDependencies(String entityName, String version) {
        // NOTE do not implement until entity references are moved from fields to entity info! (TS3)
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Response getAccess(String entityName, String version) {
        List<String> entityNames = new ArrayList<>();
        // accessMap: <role, <operation, List<path>>>
        Map<String, Map<String, List<String>>> accessMap = new HashMap<>();

        if (null != entityName && !entityName.isEmpty()) {
            entityNames.add(entityName);
        } else {
            // force version to be null
            version = null;
            entityNames.addAll(Arrays.asList(getEntityNames()));
        }

        // initialize response, assume will be completely successful
        Response response = new Response();
        response.setStatus(OperationStatus.COMPLETE);

        // for each name get metadata
        for (String name : entityNames) {
            EntityMetadata metadata;
            try {
                metadata = getEntityMetadata(name, version);
            } catch (Exception e) {
                response.setStatus(OperationStatus.PARTIAL);
                // construct error data
                ObjectNode obj = new ObjectNode(JsonNodeFactory.instance);
                obj.put("name", name);
                if (null != version) {
                    obj.put("version", version);
                }
                List<Error> errors = new ArrayList<>();
                errors.add(Error.get("ERR_NO_METADATA", "Could not get metadata for given input. Error message: " + e.getMessage()));
                DataError error = new DataError(obj, errors);
                response.getDataErrors().add(error);
                // skip to next entity name
                continue;
            }

            EntityAccess ea = metadata.getAccess();
            Map<FieldAccess, Path> fa = new HashMap<>();
            FieldCursor fc = metadata.getFieldCursor();
            // collect field access
            while (fc.next()) {
                FieldTreeNode ftn = fc.getCurrentNode();
                if (ftn instanceof Field) {
                    // add access if there is anything to extract later
                    Field f = (Field) ftn;
                    if (!f.getAccess().getFind().isEmpty()
                            || !f.getAccess().getInsert().isEmpty()
                            || !f.getAccess().getUpdate().isEmpty()) {
                        fa.put(f.getAccess(), f.getFullPath());
                    }
                }
            }

            // key is role, value is all associated paths.
            // accessMap: <role, <operation, List<path>>>
            // collect entity access
            helperAddRoles(ea.getDelete().getRoles(), "delete", name, accessMap);
            helperAddRoles(ea.getFind().getRoles(), "find", name, accessMap);
            helperAddRoles(ea.getInsert().getRoles(), "insert", name, accessMap);
            helperAddRoles(ea.getUpdate().getRoles(), "update", name, accessMap);

            // collect field access
            for (Map.Entry<FieldAccess,Path> entry:fa.entrySet()) {
                FieldAccess access=entry.getKey();
                String pathString = name + "." + entry.getValue().toString();
                helperAddRoles(access.getFind().getRoles(), "find", pathString, accessMap);
                helperAddRoles(access.getInsert().getRoles(), "insert", pathString, accessMap);
                helperAddRoles(access.getUpdate().getRoles(), "update", pathString, accessMap);
            }
        }

        // finally, populate response with valid output
        if (!accessMap.isEmpty()) {
            ArrayNode root = new ArrayNode(JsonNodeFactory.instance);
            response.setEntityData(root);
            for (Map.Entry<String,Map<String,List<String> > > entry:accessMap.entrySet()) {
                String role=entry.getKey();
                Map<String, List<String>> opPathMap = entry.getValue();

                ObjectNode roleJson = new ObjectNode(JsonNodeFactory.instance);
                root.add(roleJson);

                roleJson.put("role", role);

                for (Map.Entry<String, List<String>> operationMap : opPathMap.entrySet()) {
                    String operation = operationMap.getKey();
                    List<String> paths = opPathMap.get(operation);
                    ArrayNode pathNode = new ArrayNode(JsonNodeFactory.instance);
                    for (String path : paths) {
                        pathNode.add(path);
                    }
                    roleJson.put(operation, pathNode);
                }
            }
        } else {
            // nothing successful! set status to error
            response.setStatus(OperationStatus.ERROR);
        }

        return response;
    }

    /**
     * Add roles and paths to accessMap where accessMap = <role, <operation, List<path>>>
     *
     * @param roles
     * @param operation
     * @param path
     * @param accessMap
     */
    private void helperAddRoles(Collection<String> roles, String operation, String path, Map<String, Map<String, List<String>>> accessMap) {
        for (String role : roles) {
            if (!accessMap.containsKey(role)) {
                accessMap.put(role, new HashMap<String, List<String>>());
            }
            if (!accessMap.get(role).containsKey(operation)) {
                accessMap.get(role).put(operation, new ArrayList<String>());
            }
            accessMap.get(role).get(operation).add(path);
        }
    }
}
