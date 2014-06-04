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
package com.redhat.lightblue.rest.metadata;

public final class RestMetadataConstants {
    public static final String ERR_REST_ERROR = "rest-metadata:RestError";
    public static final String ERR_NO_ENTITY_NAME = "rest-metadata:NoEntityName";
    public static final String ERR_NO_ENTITY_VERSION = "rest-metadata:NoEntityVersion";
    public static final String ERR_NO_ENTITY_STATUS = "rest-metadata:NoEntityStatus";
    public static final String ERR_NO_NAME_MATCH = "rest-metadata:NoNameMatch";
    public static final String ERR_NO_VERSION_MATCH = "rest-metadata:NoVersionMatch";
    
    public static final String ERR_CANT_GET_METADATA = "rest-metadata:CantGetMetadata";
    public static final String ERR_CANT_GET_PARSER = "rest-metadata:CantGetParser";

    private RestMetadataConstants() {

    }
}
