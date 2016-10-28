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
package com.redhat.lightblue.metadata;

public final class MetadataConstants {

    public static final String ROLE_ANYONE = "anyone";
    public static final String ROLE_NOONE = "noone";

    public static final String ARRAY_ANY_ELEM = "*";

    public static final String ERR_DUPLICATE_FIELD = "metadata:DuplicateField";
    public static final String ERR_DUPLICATE_ENUM = "metadata:DuplicateEnum";
    public static final String ERR_INVALID_ARRAY_REFERENCE = "metadata:InvalidArrayReference";
    public static final String ERR_INVALID_FIELD_REFERENCE = "metadata:InvalidFieldReference";
    public static final String ERR_INVALID_REDIRECTION = "metadata:InvalidRedirection";
    public static final String ERR_INVALID_THIS = "metadata:Invalid$This";
    public static final String ERR_INVALID_PARENT = "metadata:Invalid$Parent";

    public static final String ERR_FIELD_WRONG_TYPE = "metadata:FieldWrongType";

    public static final String ERR_PARSE_MISSING_ELEMENT = "metadata:ParseMissingElement";
    public static final String ERR_PARSE_INVALID_STATUS = "metadata:ParseInvalidStatus";
    public static final String ERR_INVALID_ARRAY_ELEMENT_TYPE = "metadata:InvalidArrayElementType";
    public static final String ERR_ILL_FORMED_METADATA = "metadata:IllFormedMetadata";
    public static final String ERR_INVALID_BACKEND = "metadata:InvalidBackend";
    public static final String ERR_INVALID_INDEX = "metadata:InvalidIndex";
    public static final String ERR_INVALID_ENUM = "metadata:InvalidEnum";
    public static final String ERR_INVALID_HOOK = "metadata:InvalidHook";
    public static final String ERR_UNKNOWN_BACKEND = "metadata:UnknownBackend";
    public static final String ERR_INVALID_CONSTRAINT = "metadata:InvalidConstraint";
    public static final String ERR_INVALID_TYPE = "metadata:InvalidType";

    public static final String ERR_INCOMPATIBLE_VALUE = "metadata:IncompatibleValue";
    public static final String ERR_INCOMPATIBLE_FIELDS = "metadata:IncompatibleFields";

    public static final String ERR_CONFIG_NOT_VALID = "metadata:ConfigurationNotValid";
    public static final String ERR_CONFIG_NOT_FOUND = "metadata:ConfigurationNotFound";

    public static final String ERR_NOT_A_NUMBER_TYPE = "metadata:NotANumberType";

    public static final String ERR_COMPARE_NOT_SUPPORTED = "metadata:CompareNotSupported";
    public static final String ERR_CAST_NOT_SUPPORTED = "metadata:CastNotSupported";
    public static final String ERR_TO_JSON_NOT_SUPPORTED = "metadata:ToJsonNotSupported";
    public static final String ERR_FROM_JSON_NOT_SUPPORTED = "metadata:FromJsonNotSupported";

    public static final String ERR_INVALID_VERSION = "metadata:InvalidVersion";
    public static final String ERR_UNKNOWN_VERSION = "metadata:UnknownVersion";
    public static final String ERR_INVALID_VERSION_NUMBER = "metadata:InvalidVersionNumber";
    public static final String ERR_EMPTY_METADATA_NAME = "metadata:EmptyMetadataName";
    public static final String ERR_METADATA_WITH_NO_FIELDS = "metadata:MetadataWithNoFields";
    public static final String ERR_INVALID_DEFAULT_VERSION = "metadata:InvalidDefaultVersion";

    public static final String ERR_INVALID_CONTEXT = "metadata:InvalidContext";

    public static final String ERR_AUTH_FAILED = "metadata:AuthFailed";

    public static final String ERR_DATASOURCE_TIMEOUT = "metadata:DataSourceTimeout";

    public static final String ERR_DATASOURCE_UNKNOWN = "metadata:DataSourceUnknown";

    private MetadataConstants() {
    }

}
