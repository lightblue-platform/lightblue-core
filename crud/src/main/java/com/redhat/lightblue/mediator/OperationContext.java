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

import com.redhat.lightblue.ClientIdentification;
import com.redhat.lightblue.EntityVersion;
import com.redhat.lightblue.OperationStatus;
import com.redhat.lightblue.Request;
import com.redhat.lightblue.Response;
import com.redhat.lightblue.crud.CRUDFindRequest;
import com.redhat.lightblue.crud.CRUDOperation;
import com.redhat.lightblue.crud.CRUDOperationContext;
import com.redhat.lightblue.crud.DocCtx;
import com.redhat.lightblue.crud.DocRequest;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.crud.FindRequest;
import com.redhat.lightblue.crud.WithProjection;
import com.redhat.lightblue.crud.WithQuery;
import com.redhat.lightblue.hooks.HookManager;
import com.redhat.lightblue.metadata.CompositeMetadata;
import com.redhat.lightblue.metadata.EntityMetadata;
import com.redhat.lightblue.metadata.Metadata;
import com.redhat.lightblue.query.Projection;
import com.redhat.lightblue.query.QueryExpression;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.MemoryMonitor;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OperationContext extends CRUDOperationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationContext.class);

    private final Request request;
    private final Metadata metadata;
    private OperationStatus status = OperationStatus.COMPLETE;
    private final DefaultMetadataResolver resolver;
    private final MemoryMonitor<JsonNode> memoryMonitor = new MemoryMonitor<>(JsonUtils::size);

    /**
     * Construct operation context
     *
     * @param request The top-level request
     * @param metadata Metadata manager
     * @param factory The factory to get validators and controllers
     * @param CRUDOperation The operation in progress
     */
    public OperationContext(Request request,
                            Metadata metadata,
                            Factory factory,
                            CRUDOperation CRUDOperation) {
        super(CRUDOperation,
                request.getEntityVersion().getEntity(),
                factory,
                request instanceof DocRequest ? JsonDoc.docList(JsonDoc.filterNulls(((DocRequest) request).getEntityData())) : null,
                request.getExecution());
        this.request = request;
        this.metadata = metadata;
        this.resolver = new DefaultMetadataResolver(metadata);

        QueryExpression query;
        Projection projection;
        if (request instanceof WithQuery) {
            query = ((WithQuery) request).getQuery();
        } else {
            query = null;
        }
        if (request instanceof WithProjection) {
            projection = ((WithProjection) request).getProjection();
        } else {
            projection = null;
        }
        resolver.initialize(request.getEntityVersion().getEntity(),
                request.getEntityVersion().getVersion(),
                query, projection);
        addCallerRoles(getCallerRoles(resolver.getMetadataRoles(), request.getClientId()));
        LOGGER.debug("Caller roles:{}", getCallerRoles());

        registerMemoryMonitors(factory.getMaxResultSetSizeForReadsB(), factory.getWarnResultSetSizeB(), request);
    }

    /**
     * Construct operation context based on an existing one, with a different
     * request and operation
     */
    public OperationContext(Request request,
                            CRUDOperation op,
                            OperationContext ctx) {
        super(op,
                request.getEntityVersion().getEntity(),
                ctx.getFactory(),
                ctx.getCallerRoles(),
                ctx.getHookManager(),
                request instanceof DocRequest ? JsonDoc.docList(((DocRequest) request).getEntityData()) : null,
                request.getExecution());
        this.request = request;
        this.metadata = ctx.metadata;
        this.resolver = ctx.resolver;

        Factory factory = ctx.getFactory();

        registerMemoryMonitors(factory.getMaxResultSetSizeForReadsB(), factory.getWarnResultSetSizeB(), request);
    }

    /**
     * Construct an operation context drived from another operation context
     *
     * @param request The top-level request
     * @param metadata Metadata manager
     * @param factory The factory to get validators and controllers
     * @param CRUDOperation The operation in progress
     * @param resolver the resolver instance to use
     * @param docs The documents in the call. Can be null
     * @param callerRoles Roles of the current caller
     * @param hookManager the hook manager
     */
    public OperationContext(Request request,
                            Metadata metadata,
                            Factory factory,
                            CRUDOperation CRUDOperation,
                            DefaultMetadataResolver resolver,
                            List<DocCtx> docs,
                            Set<String> callerRoles,
                            HookManager hookManager) {
        super(CRUDOperation,
                request.getEntityVersion().getEntity(),
                factory,
                docs,
                callerRoles,
                hookManager,
                request.getExecution());

        this.request = request;
        this.metadata = metadata;
        this.resolver = resolver;

        // TODO: adjust for reads vs writes vs composite reads
        registerMemoryMonitors(factory.getMaxResultSetSizeForReadsB(), factory.getWarnResultSetSizeB(), request);
    }

    /**
     * Result set size threshold is expressed in bytes. This is just an approximation, see @{link {@link JsonUtils#size(JsonNode)} for details.
     *
     * @param maxResultSetSizeB error when this threshold is breached
     * @param warnResultSetSizeB log a warning when this threshold is breached
     * @param forRequest request which resulted in this response, for logging purposes
     */
    private void registerMemoryMonitors(int maxResultSetSizeB, int warnResultSetSizeB, final Request forRequest) {
        memoryMonitor.registerMonitor(new MemoryMonitor.ThresholdMonitor<>(maxResultSetSizeB, (current, threshold, doc) -> {
            throw Error.get(Response.ERR_RESULT_SIZE_TOO_LARGE, "request=" + forRequest + " responseDataSizeB=" + current + " errorThreshold=" + threshold);
        }));

        memoryMonitor.registerMonitor(new MemoryMonitor.ThresholdMonitor<>(warnResultSetSizeB, (current, threshold, doc) ->
                LOGGER.warn("crud:ResultSizeIsLarge: request={}, responseDataSizeB={} warnThreshold={}", forRequest, current, threshold)));
    }

    /**
     * Monitor the memory usage for JSON objects part of a response that are might not be garbage
     * collected at all or garbage collected timely enough before they are streamed to clients. Too
     * much memory may warn or cause this method to throw an {@link Error} based on configured
     * thresholds.
     *
     * <p>Objects which are known to be short lived should not be monitored, such as objects
     * passing through a {@link java.util.stream.Stream} or {@link java.util.Iterator} that are not
     * aggregated in a {@link java.util.Collection} or array, etc.
     *
     * <p>You <em>may</em> want to monitor objects that are aggregated, depending on how large the
     * aggregation may be and for how long the objects may be referenced.
     *
     * @throws Error {@link com.redhat.lightblue.Response#ERR_RESULT_SIZE_TOO_LARGE} If max result
     * size threshold is set and triggered.
     * @see #registerMemoryMonitors(int, int, Request)
     */
    public void monitorMemory(JsonNode json) {
        memoryMonitor.apply(json);
    }

    public OperationContext getDerivedOperationContext(String entityName, CRUDFindRequest req) {
        // Create a new request with same header information, but different query information
        FindRequest newReq = new FindRequest();
        newReq.shallowCopyFrom((Request) request, req);
        newReq.setEntityVersion(new EntityVersion(entityName, resolver.getEntityMetadata(entityName).getVersion().getValue()));
        // At this point, newReq has header information from the
        // original request, but query information from the argument
        // 'req'

        return new OperationContext(newReq,
                metadata,
                getFactory(),
                CRUDOperation.FIND,
                resolver,
                new ArrayList<DocCtx>(),
                getCallerRoles(),
                getHookManager());
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
    public CompositeMetadata getTopLevelEntityMetadata() {
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
     * Returns true if the operation context is for a simple entity, an entity
     * with no associations
     */
    public boolean isSimple() {
        return resolver.getCompositeMetadata().isSimple();
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

    public long memoryUsed() {
        return memoryMonitor.getDataSizeB();
    }
}
