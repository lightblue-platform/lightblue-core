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

/**
 * This is primarily for CRUDController implementations that would
 * like to intercept certain metadata operations. The metadata
 * implementation calls these methods on the CRUDController for that
 * entity before and after operations that modify entity metadata.
 */
public interface MetadataListener {

    /**
     * Called before creating a new schema
     *
     * @param m The metadata implementation
     * @param md The entity metadata
     */
    void beforeCreateNewSchema(Metadata m,EntityMetadata md);

    /**
     * Called after creating a new schema
     *
     * @param m The metadata implementation
     * @param md The entity metadata
     */
    void afterCreateNewSchema(Metadata m,EntityMetadata md);
    
    /**
     * Called before entity info is created or updated
     *
     * @param m The metadata implementation
     * @param md The entity info
     * @param newEntity If <code>true</code>, this is a call to create
     * a new entity. Otherwise, this is a call to modify an existing
     * entity.
     */
    void beforeUpdateEntityInfo(Metadata m,EntityInfo ei,boolean newEntity);

    /**
     * Called after entity info is created or updated
     *
     * @param m The metadata implementation
     * @param md The entity info
     * @param newEntity If <code>true</code>, this is a call to create
     * a new entity. Otherwise, this is a call to modify an existing
     * entity.
     */
    void afterUpdateEntityInfo(Metadata m,EntityInfo ei,boolean newEntity);

}
