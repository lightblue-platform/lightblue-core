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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DocRequest;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.metadata.EntityAccess;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.FieldCursor;
import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.metadata.ReferenceField;
import com.redhat.lightblue.util.JsonDoc;

public final class OperationContext extends CRUDOperationContext {

    private final Request request;
    private final Metadata metadata;
    private final Map<String, EntityMetadata> entityMetadata = new HashMap<>();
    private OperationStatus status = OperationStatus.COMPLETE;

    /**
     * Construct operation context
     *
     * @param request The top-level request
     * @param metadata Metadata manager
     * @param factory The factory to get validators and controllers
     * @param roles Roles of the current caller
     * @param docs The documents in the call. Can be null
     * @param operation The operation in progress
     */
    private OperationContext(Request request,
                             Metadata metadata,
                             Factory factory,
                             JsonNodeFactory nodeFactory,
                             Set<String> roles,
                             List<JsonDoc> docs,
                             Operation operation) {
        super(operation,request.getEntityVersion().getEntity(), factory, nodeFactory, roles, docs);
        this.request = request;
        this.metadata = metadata;
        initMetadata(request.getEntityVersion().getEntity(), request.getEntityVersion().getVersion());
    }

    /**
     * Constructs an operation context
     *
     * @param req The request
     * @param md Metadata manager
     * @param factory The factory to get validators and controllers
     * @param op The operation in progress
     */
    public static OperationContext getInstance(Request req, Metadata md, Factory factory, JsonNodeFactory nodeFactory, Operation op) {

        // get roles from MD
        Set<String> metadataRoles = new HashSet<>();

        EntityAccess ea = md.getEntityMetadata(req.getEntityVersion().getEntity(), req.getEntityVersion().getVersion()).getAccess();
        metadataRoles.addAll(ea.getFind().getRoles());
        metadataRoles.addAll(ea.getUpdate().getRoles());
        metadataRoles.addAll(ea.getInsert().getRoles());
        metadataRoles.addAll(ea.getDelete().getRoles());

        Set<String> callerRoles = new HashSet<>();
        if (!metadataRoles.isEmpty() && req.getClientId() != null) {
            for (String metadataRole : metadataRoles) {
                if (req.getClientId().isUserInRole(metadataRole)) {
                    callerRoles.add(metadataRole);
                }
            }
        }
        List<JsonDoc> docs = req instanceof DocRequest ? JsonDoc.docList(((DocRequest) req).getEntityData()) : null;
        return new OperationContext(req, md, factory, nodeFactory, callerRoles, docs, op);
    }

    /**
     * Returns the top level entity name
     */
    public String getTopLevelEntityName() {
        return request.getEntityVersion().getEntity();
    }

    /**
     * Returns the top level entity version
     */
    public String getTopLevelEntityVersion() {
        return request.getEntityVersion().getVersion();
    }

    /**
     * Returns the top level entity metadata
     */
    public EntityMetadata getTopLevelEntityMetadata() {
        return getEntityMetadata(getTopLevelEntityName());
    }

    /**
     * Returns the metadata manager
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the top level request
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the entity metadata with the version used in this call
     */
    @Override
    public EntityMetadata getEntityMetadata(String entityName) {
        return entityMetadata.get(entityName);
    }

    /**
     * The operation status
     */
    public OperationStatus getStatus() {
        return status;
    }

    /**
     * The operation status
     */
    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    private void initMetadata(String name, String version) {
        EntityMetadata x = entityMetadata.get(name);
        if (x != null) {
            if (!x.getVersion().getValue().equals(version)) {
                throw new IllegalArgumentException(CrudConstants.ERR_METADATA_APPEARS_TWICE + name + " " + version + " and " + x.getVersion().getValue());
            }
        } else {
            x = metadata.getEntityMetadata(name, version);
            if (x == null || x.getEntitySchema() == null) {
                throw new IllegalArgumentException("Unknown entity:" + name + ":" + version);
            }
            if (x.getEntitySchema().getStatus() == MetadataStatus.DISABLED) {
                throw new IllegalArgumentException(CrudConstants.ERR_DISABLED_METADATA + " " + name + " " + x.getEntitySchema().getVersion().getValue());
            }
            entityMetadata.put(x.getName(), x);
            FieldCursor c = x.getFieldCursor();
            while (c.next()) {
                FieldTreeNode node = c.getCurrentNode();
                if (node instanceof ReferenceField) {
                    String refName = ((ReferenceField) node).getEntityName();
                    String refVersion = ((ReferenceField) node).getVersionValue();
                    initMetadata(refName, refVersion);
                }
            }
        }
    }
}
