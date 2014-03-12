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
package com.redhat.lightblue.crud.mongo;

public final class MongoCrudConstants {

    public static final String ERR_INVALID_OBJECT = "mongo-crud:InvalidObject";
    public static final String ERR_DUPLICATE = "mongo-crud:Duplicate";
    public static final String ERR_INSERTION_ERROR = "mongo-crud:InsertionError";
    public static final String ERR_SAVE_ERROR = "mongo-crud:SaveError";
    public static final String ERR_UPDATE_ERROR = "mongo-crud:UpdateError";
    public static final String ERR_NO_ACCESS = "mongo-crud:NoAccess";
    public static final String ERR_CONNECTION_ERROR = "mongo-crud:ConnectionError";

    public static final String ERR_EMPTY_DOCUMENTS = "mongo-crud:EmptyDocuments";
    public static final String ERR_EMPTY_VALUE_LIST = "mongo-crud:EmptyValueList";

    public static final String ERR_NULL_QUERY = "mongo-crud:NullQuery";
    public static final String ERR_NULL_PROJECTION = "mongo-crud:NullProjection";

    private MongoCrudConstants() {

    }
}
