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

/**
 * Metadata manager interface
 */
public interface Metadata extends Serializable {

    /**
     * Returns a particular version of the entity metadata
     */
    EntityMetadata getEntityMetadata(String entityName, String version);

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
