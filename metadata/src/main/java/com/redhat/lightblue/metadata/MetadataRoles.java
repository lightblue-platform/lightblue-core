/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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

public enum MetadataRoles {
    FIND_DEPENDENCIES("metadata.find.dependencies"),
    FIND_ROLES("metadata.find.roles"),
    FIND_ENTITY_NAMES("metadata.find.entityNames"),
    FIND_ENTITY_VERSIONS("metadata.find.entityVersions"),
    FIND_ENTITY_METADATA("metadata.find.entityMetadata"),
    INSERT("metadata.insert"),
    INSERT_SCHEMA("metadata.insert.schema"),
    UPDATE_ENTITYINFO("metadata.update.entityInfo"),
    UPDATE_ENTITY_SCHEMASTATUS("metadata.update.schemaStatus"),
    UPDATE_DEFAULTVERSION("metadata.update.defaultVersion"),
    DELETE_ENTITY("metadata.delete.entity");

    private final String jsonRepresentation;

    MetadataRoles(String jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    @Override
    public String toString() {
        return this.jsonRepresentation;
    }
}
