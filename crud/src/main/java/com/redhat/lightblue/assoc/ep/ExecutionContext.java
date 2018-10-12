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
package com.redhat.lightblue.assoc.ep;

import java.util.concurrent.ExecutorService;

import com.redhat.lightblue.Request;
import com.redhat.lightblue.crud.CrudConstants;
import com.redhat.lightblue.crud.Factory;
import com.redhat.lightblue.mediator.OperationContext;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.JsonUtils;
import com.redhat.lightblue.util.MemoryMonitor;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps state information for the execution of composite find.
 */
public class ExecutionContext {
    private final OperationContext opctx;
    private final ExecutorService executor;
    private int matchCount;
    private final MemoryMonitor<JsonNode> memoryMonitor = new MemoryMonitor<>(JsonUtils::size);

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);

    public ExecutionContext(OperationContext ctx, ExecutorService executor) {
        this.opctx = ctx;
        this.executor = executor;

        Factory factory = ctx.getFactory();

        registerMemoryMonitors(
                factory.getMaxExecutionContextSizeForCompositeFindB(),
                factory.getWarnResultSetSizeB(),
                ctx.getRequest());
    }

    public OperationContext getOperationContext() {
        return opctx;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public boolean hasErrors() {
        return opctx.hasErrors();
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int c) {
        matchCount = c;
    }

    public void incMatchCount() {
        matchCount++;
    }

    public void close() {
        executor.shutdown();
    }

    /**
     * @see #monitorMemory(JsonNode)
     */
    public void monitorMemory(JsonDoc doc) {
        monitorMemory(doc.getRoot());
    }

    /**
     * @see #monitorMemory(JsonNode)
     */
    public void monitorMemory(ResultDocument document) {
        monitorMemory(document.getDoc());
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
     * @throws Error {@link CrudConstants#ERR_EXECUTION_CONTEXT_TOO_LARGE} If max result size
     * threshold is set and triggered.
     * @see #registerMemoryMonitors(int, int, Request)
     */
    public void monitorMemory(JsonNode json) {
        memoryMonitor.apply(json);
    }

    /**
     * Experimental! Use carefully.
     */
    public void deductMemoryIfCountable(Object object) {
        if (object instanceof JsonNode) {
            deductMemory((JsonNode) object);
        } else if (object instanceof JsonDoc) {
            deductMemory((JsonDoc) object);
        } else if (object instanceof ResultDocument) {
            deductMemory((ResultDocument) object);
        }
    }

    /**
     * Experimental! Use carefully.
     */
    public void deductMemory(ResultDocument document) {
        deductMemory(document.getDoc());
    }

    /**
     * Experimental! Use carefully.
     */
    public void deductMemory(JsonDoc doc) {
        deductMemory(doc.getRoot());
    }

    /**
     * Experimental! Use carefully.
     */
    public void deductMemory(JsonNode json) {
        memoryMonitor.deduct(json);
    }

    /**
     * Result set size threshold is expressed in bytes. This is just an approximation, see @{link {@link JsonUtils#size(JsonNode)} for details.
     *
     * @param maxResultSetSizeB error when this threshold is breached
     * @param warnResultSetSizeB log a warning when this threshold is breached
     * @param forRequest request which resulted in this response, for logging purposes
     */
    private void registerMemoryMonitors(int maxResultSetSizeB, int warnResultSetSizeB, final Request forRequest) {
        // Order is significant – warn first for request in log output.
        memoryMonitor.registerMonitor(new MemoryMonitor.ThresholdMonitor<>(warnResultSetSizeB, (current, threshold, doc) ->
                LOGGER.warn("crud:ExecutionContextIsLarge: request={}, executionDataSizeB={} threshold={}", forRequest, current, threshold)));

        memoryMonitor.registerMonitor(new MemoryMonitor.ThresholdMonitor<>(maxResultSetSizeB, (current, threshold, doc) -> {
            throw Error.get(
                    CrudConstants.ERR_EXECUTION_CONTEXT_TOO_LARGE,
                    current + "B > " + threshold + "B");
        }));
    }

    public int memoryUsedB() {
        return memoryMonitor.getDataSizeB();
    }
}
