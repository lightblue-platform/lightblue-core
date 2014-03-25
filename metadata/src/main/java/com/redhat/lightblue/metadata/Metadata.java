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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Metadata manager interface
 */
public interface Metadata extends Serializable {

    /**
     * Get all dependencies using default versions. If an entity does not have a default version defined it will be
     * skipped.
     */
    List<Dependency> getDependencies();

    /**
     * Get all dependencies for the given entity using default versions. If an entity does not have a default version
     * defined it will be skipped.
     */
    List<Dependency> getDependencies(String entityName);

    /**
     * Get all dependencies for the given version of the entity. If an entity does not have a default version defined it
     * will be skipped.
     */
    List<Dependency> getDependnecies(String entityName, String version);

    /**
     * Get all roles (access) defined.
     *
     * @return
     */
    Map<String, Access> getAccess();

    String[] getAccess(String entityName);

    String[] getAccess(String entityName, String version);

    /**
     * Returns a particular version of the entity metadata if forceVersion is true. Otherwise, it returns the default
     * version if the specific version is disabled
     */
    EntityMetadata getEntityMetadata(String entityName, String version, boolean forceVersion);

    /**
     * Returns the names of all entities
     */
    String[] getEntityNames();

    /**
     * Returns all versions of an entity
     */
    Version[] getEntityVersions(String entityName);

    /**
     * Creates a new entity metadata
     */
    void createNewMetadata(EntityMetadata md);

    /**
     * Sets the status of a particular version of an entity
     */
    void setMetadataStatus(String entityName,
                           String version,
                           MetadataStatus newStatus,
                           String comment);

}
