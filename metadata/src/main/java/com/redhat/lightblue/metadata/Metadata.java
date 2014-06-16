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

import com.redhat.lightblue.Response;
import java.io.Serializable;

/**
 * Metadata manager interface
 */
public interface Metadata extends Serializable {

    /**
     * Get all dependencies for the given entity using the given version. If entity name is not specified all entity
     * dependencies are processed. If no entity version is specified default version for the entity is used, lack of
     * default version is treated as a data error.
     *
     * @return Response with an array of Dependency object data.
     */
    Response getDependencies(String entityName, String version);

    /**
     * Get all entity and field access for the given entity using the given version. If entity name is not specified all
     * entities are processed. If no entity version is specified default version for the entity is used, lack of default
     * version is treated as a data error.
     *
     * @return Response with array of Map<String, MetadataAccess> data, where key is the entity name
     */
    Response getAccess(String entityName, String version);

    /**
     * Returns a particular version of the entity metadata if a version is specified. Otherwise, it returns the default
     * version
     */
    EntityMetadata getEntityMetadata(String entityName, String version);

    /**
     * Returns the entity info for the given entity.
     */
    EntityInfo getEntityInfo(String entityName);

    /**
     * Returns the names of all entities
     *
     * @param statuses If empty, all entity names are returned. Otherwise, only those entities that have schema with the
     * given statuses are returned
     */
    String[] getEntityNames(MetadataStatus... statuses);

    /**
     * Returns all versions of an entity
     */
    VersionInfo[] getEntityVersions(String entityName);

    /**
     * Creates a new entity metadata
     */
    void createNewMetadata(EntityMetadata md);

    /**
     * Creates a new schema (versioned data) for an existing metadata.
     *
     * @param md
     */
    void createNewSchema(EntityMetadata md);

    /**
     * Updates entity info
     */
    void updateEntityInfo(EntityInfo ei);

    /**
     * Sets the status of a particular version of an entity
     */
    void setMetadataStatus(String entityName,
                           String version,
                           MetadataStatus newStatus,
                           String comment);

    /**
     * Remove all entity records only if all versions of the entity are disabled
     */
    void removeEntity(String entityName);

}
