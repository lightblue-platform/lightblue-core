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
package com.redhat.lightblue.rest.crud;

public final class RestCrudConstants {
    public static final String ERR_REST_ERROR = "rest-crud:RestError";
    public static final String ERR_REST_FIND = "rest-crud:RestFindError";
    public static final String ERR_REST_INSERT = "rest-crud:RestInsertError";
    public static final String ERR_REST_UPDATE = "rest-crud:RestUpdateError";
    public static final String ERR_REST_SAVE = "rest-crud:RestSaveError";
    public static final String ERR_REST_DELETE = "rest-crud:RestDeleteError";

    public static final String ERR_NO_ENTITY_MATCH = "rest-crud:NoEntityMatch";
    public static final String ERR_NO_VERSION_MATCH = "rest-crud:NoVersionMatch";

    public static final String ERR_CANT_GET_MEDIATOR = "rest-crud:CantGetMediator";
    
    private RestCrudConstants() {

    }
}
