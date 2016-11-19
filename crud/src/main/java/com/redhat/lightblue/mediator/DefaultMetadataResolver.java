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
package com.redhat.lightblue.mediator;

import java.io.Serializable;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Deque;
import java.util.ArrayDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.crud.MetadataResolver;
import com.redhat.lightblue.crud.CrudConstants;

import com.redhat.lightblue.EntityVersion;

import com.redhat.lightblue.metadata.AbstractGetMetadata;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Field;
import com.redhat.lightblue.metadata.ResolvedReferenceField;
import com.redhat.lightblue.metadata.EntityAccess;
import com.redhat.lightblue.metadata.FieldAccess;
import com.redhat.lightblue.metadata.FieldCursor;

import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.query.Projection;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;

/**
 * This implementation combines the implementations for GetMetadata
 * interface used by CompositeMetadata, and MetadataResolver interface
 * used by the CRUD subsystem to access metadata by name.
 *
 * The metadata tree may contain entities accessed using default
 * versions. If the same entity appears with a definite version as
 * well, that definite version will be used for default versions.
 * 
 * Algorithm:
 *   Gmd.retrieveMetadata is called whenever new metadata is needed to evaluate 
 * the query/projection. 
 *   -If Gmd.retrieveMetadata is called with a definite version
 *      If the same version is already loaded, return that
 *      If another version is already loaded, throw exception
 *      If it is not loaded, load it, cache it
 *   -If Gmd.retrieveMetadata is called with a default version
 *      If the same entity is already loaded with a definite version, return it
 *      If the entity is not loaded, load it using its default version
 *
 *
 */
public class DefaultMetadataResolver implements MetadataResolver, Serializable {

    private static final long serialVersionUID = 1l;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultMetadataResolver.class);

    private final Map<String, EntityMetadata> metadataMap = new HashMap<>();
    private final Metadata md;

    private CompositeMetadata cmd;
    private Set<String> roles;

    private final class Gmd extends AbstractGetMetadata {
        // the metadata version of the initial request
        private final String requestVersion;
        private final String requestEntityName;

        public Gmd(Projection projection, QueryExpression query, String requestEntityName, String requestVersion) {
            super(projection, query);
            this.requestVersion = requestVersion;
            this.requestEntityName = requestEntityName;
        }

        @Override
        protected EntityMetadata retrieveMetadata(Path injectionPath, String entityName, String entityVersion) {
            EntityMetadata metadata=metadataMap.get(entityName);
            if(metadata!=null) {
                if(entityVersion!=null&&!entityVersion.equals(metadata.getVersion().getValue())) {
                    throw Error.get(CrudConstants.ERR_METADATA_APPEARS_TWICE, entityName + ":" + entityVersion + " and " + metadata.getVersion().getValue());
                } 
            } else if(entityVersion!=null) {
                metadata = md.getEntityMetadata(entityName, entityVersion);
                if (metadata == null) {
                    throw Error.get(CrudConstants.ERR_UNKNOWN_ENTITY, entityName + ":"+ entityVersion);
                }
                metadataMap.put(entityName,metadata);
            } else {
                // Metadata is requested with default version, and we haven't loaded it yet
                metadata=md.getEntityMetadata(entityName,null);
                if (metadata == null) {
                    throw Error.get(CrudConstants.ERR_UNKNOWN_ENTITY, entityName + ":"+ entityVersion);
                }
                metadataMap.put(entityName,metadata);
            }
            return metadata;
        }
    }

    /**
     * Constructs the metadata resolver with the given metadata implementation
     */
    public DefaultMetadataResolver(Metadata metadata) {
        this.md = metadata;
    }

    /**
     * This method builds the composite metadata for the given top level entity name and entity version, for the given query and projections
     */
    public void initialize(String entityName, String entityVersion, final QueryExpression query, final Projection projection) {
        if (cmd != null) {
            throw new IllegalStateException("Metadata resolver was already initialized");
        }

        LOGGER.debug("Initializing with {}:{}", entityName, entityVersion);
        // first call to getMetadata will preload metadataMap
        EntityMetadata emd = md.getEntityMetadata(entityName, entityVersion);
        if (emd == null || emd.getEntitySchema() == null) {
            throw Error.get(CrudConstants.ERR_UNKNOWN_ENTITY, entityName + ":" + entityVersion);
        } else if (emd.getEntitySchema().getStatus() == MetadataStatus.DISABLED) {
            throw Error.get(CrudConstants.ERR_DISABLED_METADATA, entityName + ":" + entityVersion);
        }
        metadataMap.put(entityName,emd);
        cmd = CompositeMetadata.buildCompositeMetadata(emd, new Gmd(projection, query, entityName, entityVersion));
        LOGGER.debug("Composite metadata:{}", cmd);

        LOGGER.debug("Collecting metadata roles");
        roles = new HashSet<>();
        addMetadataRoles(roles, cmd);
        FieldCursor c = cmd.getFieldCursor();
        while (c.next()) {
            FieldTreeNode node = c.getCurrentNode();
            addFieldRoles(roles, node);
            if (node instanceof ResolvedReferenceField) {
                addMetadataRoles(roles, ((ResolvedReferenceField) node).getReferencedMetadata());
            }
        }
        LOGGER.debug("Metadata roles:{}", roles);
    }

    /**
     * Returns the top level entity name
     */
    public String getTopLevelEntityName() {
        return cmd.getName();
    }

    /**
     * Returns the top level entity version
     */
    public String getTopLevelEntityVersion() {
        return cmd.getVersion().getValue();
    }

    /**
     * Returns the top level entity metadata
     */
    public CompositeMetadata getTopLevelEntityMetadata() {
        return cmd;
    }

    /**
     * Returns the version of the entity metadata relevant in the current operation
     */
    @Override
    public EntityMetadata getEntityMetadata(String entityName) {
        return metadataMap.get(entityName);
    }

    /**
     * Returns the composite metadata for this operation
     */
    public CompositeMetadata getCompositeMetadata() {
        return cmd;
    }

    /**
     * Return all roles referenced in metadata
     */
    public Set<String> getMetadataRoles() {
        return roles;
    }

    private void addMetadataRoles(Set<String> roles, EntityMetadata em) {
        EntityAccess a = em.getAccess();
        a.getFind().addRolesTo(roles);
        a.getUpdate().addRolesTo(roles);
        a.getInsert().addRolesTo(roles);
        a.getDelete().addRolesTo(roles);
    }

    private void addFieldRoles(Set<String> roles, FieldTreeNode node) {
        if (node instanceof Field) {
            Field field = (Field) node;
            FieldAccess a = field.getAccess();
            a.getFind().addRolesTo(roles);
            a.getInsert().addRolesTo(roles);
            a.getUpdate().addRolesTo(roles);
        }
    }
}
