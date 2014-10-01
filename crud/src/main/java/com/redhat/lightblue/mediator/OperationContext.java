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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.ClientIdentification;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.DocRequest;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.Operation;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.metadata.MetadataStatus;
import com.redhat.lightblue.util.JsonDoc;

public final class OperationContext extends CRUDOperationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationContext.class);

    private final Request request;
    private final Metadata metadata;
    private OperationStatus status = OperationStatus.COMPLETE;
    private final DefaultMetadataResolver resolver;

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
    public OperationContext(Request request,
                            Metadata metadata,
                            Factory factory,
                            Operation operation) {
        super(operation, 
              request.getEntityVersion().getEntity(), 
              factory, 
              request instanceof DocRequest ? JsonDoc.docList( ((DocRequest)request).getEntityData()):null );
        this.request = request;
        this.metadata = metadata;
        this.resolver = new DefaultMetadataResolver(metadata);

        if(request instanceof FindRequest) {
            // Setup composite metadata for find requests
            resolver.initialize(request.getEntityVersion().getEntity(),
                                request.getEntityVersion().getVersion(),
                                ((FindRequest)request).getQuery(),
                                ((FindRequest)request).getProjection());
        } else {
            resolver.initialize(request.getEntityVersion().getEntity(),
                                request.getEntityVersion().getVersion(),
                                null,null);
        }
        setCallerRoles(getCallerRoles(resolver.getMetadataRoles(),request.getClientId()));
        LOGGER.debug("Caller roles:{}", getCallerRoles());
    }


    /**
     * Returns the top level entity name
     */
    public String getTopLevelEntityName() {
        return resolver.getTopLevelEntityName();
    }

    /**
     * Returns the top level entity version
     */
    public String getTopLevelEntityVersion() {
        return resolver.getTopLevelEntityVersion();
    }

    /**
     * Returns the top level entity metadata
     */
    public EntityMetadata getTopLevelEntityMetadata() {
        return resolver.getTopLevelEntityMetadata();
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
        return resolver.getEntityMetadata(entityName);
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


    private Set<String> getCallerRoles(Set<String> metadataRoles, ClientIdentification id) {
        Set<String> callerRoles = new HashSet<>();
        if (!metadataRoles.isEmpty() && id != null) {
            for (String metadataRole : metadataRoles) {
                if (id.isUserInRole(metadataRole)) {
                    callerRoles.add(metadataRole);
                }
            }
        }
        return callerRoles;
    }

}
