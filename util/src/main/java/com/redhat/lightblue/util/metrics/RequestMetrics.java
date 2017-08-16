/*
 Copyright 2017 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.util.metrics;

public interface RequestMetrics {
    /**
     * Start timers and counters for the request. Use the returned context to
     * complete the request and optionally mark errors if they occur.
     *
     * <p>
     * The returned context itself is not completely thread safe, it is expected to
     * be used by one and only one thread concurrently.
     */
    Context startEntityRequest(String operation, String entity, String version);
    
    Context startStreamingEntityRequest(String operation, String entity, String version);

    Context startLockRequest(String lockOperation, String domain);

    Context startBulkRequest(String bulkOperation, String entity, String version);
    
    void setBulkRequest(boolean bulkRequest);
    
    boolean isBulkRequest();

    interface Context {
        
        void endRequestMonitoring();
        
        void markRequestException(Exception e);

        void endRequestMonitoringWithException(Exception e);

    }
}
