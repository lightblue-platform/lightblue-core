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

public final class MongoMetadataConstants {

    public static final String ERR_DUPLICATE_METADATA = "mongo-metadata:DuplicateMetadata";
    public static final String ERR_UNKNOWN_VERSION = "mongo-metadata:UnknownVersion";
    public static final String ERR_DB_ERROR = "mongo-metadata:DatabaseError";
    public static final String ERR_MISSING_ENTITY_INFO = "mongo-metadata:MissingEntityInfo";

    public static final String ERR_DISABLED_VERSION = "mongo-metadata:DisabledVersion";
    public static final String ERR_INVALID_VERSION = "mongo-metadata:InvalidVersion";
    public static final String ERR_INVALID_DEFAULT_VERSION = "mongo-metadata:InvalidDefaultVersion";
    public static final String ERR_DISABLED_DEFAULT_VERSION = "mongo-metadata:DisabledDefaultVersion";
    public static final String ERR_INVALID_VERSION_NUMBER = "mongo-metadata:InvalidVersionNumber";
    public static final String ERR_INVALID_DATASTORE = "mongo-metadata:InvalidDatastore";
    public static final String ERR_EMPTY_METADATA_NAME = "mongo-metadata:EmptyMetadataName";
    public static final String ERR_METADATA_WITH_NO_FIELDS = "mongo-metadata:MetadataWithNoFields";

    public static final String ERR_NEW_STATUS_IS_NULL = "mongo-metadata:NewStatusIsNull";

    private MongoMetadataConstants() {

    }

}
