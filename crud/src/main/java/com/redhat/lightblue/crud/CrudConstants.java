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
package com.redhat.lightblue.crud;

public final class CrudConstants {

    public static final String ERR_ARRAY_TOO_SMALL = "crud:ArrayTooSmall";
    public static final String ERR_ARRAY_TOO_LARGE = "crud:ArrayTooLarge";
    public static final String ERR_INVALID_ENUM = "crud:InvalidEnum";
    public static final String ERR_VALUE_TOO_SMALL = "crud:ValueTooSmall";
    public static final String ERR_VALUE_TOO_LARGE = "crud:ValueTooLarge";
    public static final String ERR_REQUIRED = "crud:Required";
    public static final String ERR_TOO_SHORT = "crud:TooShort";
    public static final String ERR_TOO_LONG = "crud:TooLong";
    public static final String ERR_CRUD = "crud";
    public static final String ERR_NO_ACCESS = "crud:NoAccess";
    public static final String ERR_NO_FIELD_INSERT_ACCESS = "crud:insert:NoFieldAccess";
    public static final String ERR_NO_FIELD_UPDATE_ACCESS = "crud:update:NoFieldAccess";

    public static final String ERR_INVALID_ENTITY = "crud:InvalidEntity";
    public static final String ERR_INVALID_DEREFERENCE = "crud:InvalidDeference";
    public static final String ERR_INVALID_ASSIGNMENT = "crud:InvalidAssignment";

    public static final String ERR_INVALID_HOOK = "crud:InvalidHook";

    public static final String ERR_INCOMPATIBLE_DEREFERENCE = "crud:IncompatibleDereference";
    public static final String ERR_INCOMPATIBLE_ASSIGNMENT = "crud:IncompatibleAssignment";

    public static final String ERR_PATTERN_NOT_EXPECTED = "crud:PatternNotExpected";

    public static final String ERR_REQUIRED_INSERTION_INDEX = "crud:InsertionRequiresIndex";
    public static final String ERR_REQUIRED_ARRAY = "crud:ArrayRequired";

    public static final String ERR_EXPECTED_OBJECT_VALUE = "crud:ObjectValueExpected";
    public static final String ERR_EXPECTED_VALUE = "crud:ValueExpected";
    public static final String ERR_EXPECTED_SIMPLE_ARRAY = "crud:SimpleArrayExpected";
    public static final String ERR_EXPECTED_ARRAY_FIELD = "crud:ArrayFieldExpected";
    public static final String ERR_EXPECTED_SIMPLE_FIELD_OR_SIMPLE_ARRAY = "crud:SimpleFieldOrSimpleArrayExpected";
    public static final String ERR_EXPECTED_OBJECT_ARRAY = "crud:ExpectedObjectArray";
    public static final String ERR_EXPECTED_ARRAY = "crud:ExpectedArray";
    public static final String ERR_EXPECTED_ARRAY_ELEMENT = "crud:ExpectedArrayElement";

    public static final String ERR_FIELD_NOT_ARRAY = "crud:FieldNotArray";
    public static final String ERR_FIELD_NOT_THERE = "crud:FieldNotThere";

    public static final String ERR_CANT_ACCESS = "crud:CannotAccess";
    public static final String ERR_ASSIGNMENT = "crud:AssignmentError";

    public static final String ERR_NO_CONSTRAINT = "crud:NoConstraint";
    public static final String ERR_CONFIG_NOT_VALID = "crud:ConfigurationNotValid";

    public static final String ERR_CANNOT_LOAD_METADATA = "crud:CannotLoadMetadata";
    public static final String ERR_DISABLED_METADATA = "crud:DisabledMetadataVersion";
    public static final String ERR_METADATA_APPEARS_TWICE = "crud:MetadataAppearsTwice";
    public static final String ERR_UNKNOWN_ENTITY = "crud:UnknownEntity";

    public static final String ERR_AUTH_FAILED = "crud:AuthFailed";

    private CrudConstants() {

    }
}
