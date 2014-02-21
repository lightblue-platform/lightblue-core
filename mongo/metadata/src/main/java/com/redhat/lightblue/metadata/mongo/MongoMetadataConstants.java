package com.redhat.lightblue.metadata.mongo;

public final class MongoMetadataConstants {

    public static final String ERR_DUPLICATE_METADATA = "mongo-metadata:DuplicateMetadata ";
    public static final String ERR_UNKNOWN_VERSION = "mongo-metadata:UnknownVersion ";
    public static final String ERR_DB_ERROR = "mongo-metadata:DatabaseError ";
    public static final String ERR_MISSING_ENTITY_INFO = "mongo-metadata:MissingEntityInfo ";

    public static final String ERR_INVALID_VERSION = " mongo-metadata:InvalidVersion ";
    public static final String ERR_INVALID_VERSION_NUMBER = " mongo-metadata:InvalidVersionNumber ";
    public static final String ERR_INVALID_DATASTORE = " mongo-metadata:InvalidDatastore ";
    public static final String ERR_EMPTY_METADATA_NAME = " mongo-metadata:EmptyMetadataName ";
    public static final String ERR_METADATA_WITH_NO_FIELDS = " mongo-metadata:MetadataWithNoFields ";
    
    public static final String ERR_NEW_STATUS_IS_NULL = " mongo-metadata:NewStatusIsNull ";
    
    private MongoMetadataConstants() {
        
    }
    
}