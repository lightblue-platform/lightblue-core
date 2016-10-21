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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.crud.MetadataResolver;
import com.redhat.lightblue.crud.CrudConstants;

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
 * This implementation combines the implementations for GetMetadata interface
 * used by CompositeMetadata, and MetadataResolver interface used by the CRUD
 * subsystem to access metadata by name.
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

        public Gmd(Projection projection,
                   QueryExpression query, String requestVersion) {
            super(projection, query);
            this.requestVersion = requestVersion;
        }

        @Override
        protected EntityMetadata retrieveMetadata(Path injectionPath,
                                                  String entityName,
                                                  String entityVersion) {
            EntityMetadata emd = metadataMap.get(entityName);
            if (emd != null) {
                String defaultVersion = emd.getEntityInfo().getDefaultVersion();
                if (!isValidReference(entityVersion, requestVersion, defaultVersion)) {
                    throw Error.get(CrudConstants.ERR_METADATA_APPEARS_TWICE, entityName + ":" + entityVersion + " and " + requestVersion);
                }
            }
            return metadataMap.get(entityName);
        }
    }

    /**
     * Constructs the metadata resolver with the given metadata implementation
     */
    public DefaultMetadataResolver(Metadata metadata) {
        this.md = metadata;
    }

    /**
     * This method builds the composite metadata for the given top level entity
     * name and entity version, for the given query and projections
     */
    public void initialize(String entityName,
                           String entityVersion,
                           final QueryExpression query,
                           final Projection projection) {
        if (cmd != null) {
            throw new IllegalStateException("Metadata resolver was already initialized");
        }

        LOGGER.debug("Initializing with {}:{}", entityName, entityVersion);
        // first call to getMetadata will preload metadataMap
        EntityMetadata emd = md.getEntityMetadata(entityName, entityVersion);
        initMetadataMap(emd, entityName, entityVersion);
        cmd = CompositeMetadata.buildCompositeMetadata(emd, new Gmd(projection, query, entityVersion));
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
     * Returns the version of the entity metadata relevant in the current
     * operation
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

    private void initMetadataMap(EntityMetadata emd, String entityName, String rootRequestVersion) {
        if (emd == null || emd.getEntitySchema() == null) {
            throw Error.get(CrudConstants.ERR_UNKNOWN_ENTITY, entityName + ":" + rootRequestVersion);
        }
        else if (emd.getEntitySchema().getStatus() == MetadataStatus.DISABLED) {
            throw Error.get(CrudConstants.ERR_DISABLED_METADATA, entityName + ":" + rootRequestVersion);
        }

        // store root entity metadata
        metadataMap.put(entityName, emd);
        Set<String> deferred = new HashSet<>();
        FieldCursor cursor = emd.getFieldCursor();
        do {
            FieldTreeNode node = cursor.getCurrentNode();
            if (node instanceof ReferenceField) {
                ReferenceField ref = (ReferenceField) node;
                String name = ref.getEntityName();
                String ver = ref.getVersionValue();
                EntityMetadata refMd = md.getEntityMetadata(name, ver);
                if (refMd == null || refMd.getEntitySchema() == null) {
                    throw Error.get(CrudConstants.ERR_UNKNOWN_ENTITY, name + ":" + ver);
                }
                else if (refMd.getEntitySchema().getStatus() == MetadataStatus.DISABLED) {
                    throw Error.get(CrudConstants.ERR_DISABLED_METADATA, name + ":" + ver);
                }
                String defaultVer = refMd.getEntityInfo().getDefaultVersion();
                EntityMetadata found = metadataMap.get(name);

                if(found != null){
                    String foundVer = found.getVersion().getValue();
                    if(isValidReference(ver, foundVer, defaultVer)){
                        if (ver != defaultVer) {
                            metadataMap.put(name, refMd);
                        } else {
                            deferred.add(name);
                        }
                    } else {
                        throw Error.get(CrudConstants.ERR_METADATA_APPEARS_TWICE, name + ":" + ver + " and " + foundVer);
                    }
                } else {
                    if (ver != defaultVer) {
                        metadataMap.put(name, refMd);
                    } else {
                        deferred.add(name);
                    }
                }
            }
        } while (cursor.next());

        // we didn't find specific versions for these entities, so use default
        deferred.forEach(n -> {
            String def = md.getEntityInfo(n).getDefaultVersion();
            metadataMap.put(n, md.getEntityMetadata(n, def));
        });
    }

    private boolean isValidReference(String version, String requestVersion, String defaultVersion) {
        // TODO: if defaultVersion is requested, then requestVersion will be null, if the entity doesn't have a default version, 'defaultVersion' will be null
        // if req version of metadata is the default version, and there is a cycle back using a version that is not default, fail
        // or
        // if req version of metadata is not the default version, and cyclic version is not default version, but req version and cyclic version don't match, fail
        return !((requestVersion.equals(defaultVersion) && !requestVersion.equals(version))
                || (!requestVersion.equals(defaultVersion) && !version.equals(defaultVersion) && !requestVersion.equals(version)));
    }
}
