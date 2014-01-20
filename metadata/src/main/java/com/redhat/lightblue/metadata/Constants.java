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

public final class Constants {

    public static final String ROLE_ANYONE = "anyone";
    public static final String ROLE_NOONE = "noone";

    public static final String ARRAY_ANY_ELEM = "*";

    public static final String ERR_DUPLICATE_FIELD = "DUPLICATE_FIELD";
    public static final String ERR_INVALID_ARRAY_REFERENCE = "INVALID_ARRAY_REFERENCE";
    public static final String ERR_INVALID_FIELD_REFERENCE = "INVALID_FIELD_REFERENCE";
    public static final String ERR_INVALID_REDIRECTION = "INVALID_REDIRECTION";
    public static final String ERR_INVALID_THIS = "INVALID_$THIS";
    public static final String ERR_INVALID_PARENT = "INVALID_$PARENT";

    private Constants() {
    }
}
