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
package com.redhat.lightblue;

import com.fasterxml.jackson.databind.JsonNode;

import com.redhat.lightblue.util.JsonObject;

/**
 * This is the base class for implementation specific client identification.
 * Implementations of this class must contain data that identifies the caller of
 * an API
 */
public abstract class ClientIdentification extends JsonObject {
    private static final long serialVersionUID = 1L;
    
    /**
     * Get the principal identifying the client.
     */
    public abstract String getPrincipal();
    
    /**
     * Return the roles the caller is in.
     */
    public abstract boolean isUserInRole(String role);

    /**
     * Default implementation of toJson() returns an empty object node
     */
    @Override
    public JsonNode toJson() {
        return getFactory().objectNode();
    }
}
