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

import java.util.List;
import com.redhat.lightblue.util.Error;

/**
 * Start timers and counters for each request. Use the returned context to
 * complete the request and optionally mark errors if they occur.
 * 
 * Each request is monitored within a unique namespace, which is created
 * by using the information available in each request. 
 *
 */
public interface RequestMetrics {

    /** 
     * Starts monitoring for a simple request. This will handle all LB  
     * CRUD operations. 
     */
    Context startEntityRequest(String operation, String entity, String version);

    /** 
     * Starts monitoring for a streaming request. This will handle all streaming LB 
     * CRUD operations 
     */
    Context startStreamingEntityRequest(String operation, String entity, String version);

    /** 
     * Starts monitoring for a locking request
     *  
     */
    Context startLockRequest(String lockOperation, String domain);

    /** 
     * Starts monitoring for a bulk request. Individual requests within the bulk requests
     * are tracked using startEntityRequest(...) 
     *  
     */
    Context startBulkRequest();

    /**
     * Context information for a request. Context is created when monitoring starts for any request 
     * and is further monitoring actions on that request are tracked using this context.
     * 
     * The returned context itself is not completely thread safe, it is expected to
     * be used by one and only one thread concurrently.
     * 
     */
    interface Context {
        
        /** 
         * Ends request monitoring for all types of requests.
         *  
         */
        void endRequestMonitoring();

        /** 
         * Mark request as exception with error type and error code in 
         * metric namespace. Use this to track cases where LB Error object is available.
         *  
         */
        void markRequestException(Error e);
        
        /** 
         * Mark request as exception with exception type and exception message in 
         * metric namespace. Use this to track cases where LB Error object is not available
         *  
         */
        void markRequestException(Exception e);
        
        /** 
         * Mark all the individual errors within their own error namespace
         * and end monitoring for the request
         *  
         */
        void markAllErrorsAndEndRequestMonitoring(List<? extends Error> errors);
        
    }
}
