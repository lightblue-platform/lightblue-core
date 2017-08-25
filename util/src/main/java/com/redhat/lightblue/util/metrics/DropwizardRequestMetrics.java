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

import static com.codahale.metrics.MetricRegistry.name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import com.redhat.lightblue.util.Error;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.MetricRegistry;

public class DropwizardRequestMetrics implements RequestMetrics {
    
    private final Logger LOGGER = LoggerFactory.getLogger(DropwizardRequestMetrics.class);

    private static final String API = "api";

    private final MetricRegistry metricsRegistry;
    
    private final String REGEX = "[-@#!$%^&*()_+|~={}:;'<>?,\"\\s]";

    public DropwizardRequestMetrics(MetricRegistry metricRegistry) {
        metricsRegistry = metricRegistry;
    }

    /**
     * Create exception namespace for metric reporting based on exception name
     * 
     */
    private static String errorNamespace(String metricNamespace, Throwable exception, String errorMessage) {
        Class<? extends Throwable> actualExceptionClass = unravelReflectionExceptions(exception);
        return name(metricNamespace, "requests", "exception", actualExceptionClass.getSimpleName(), errorMessage);
    }

    /**
     * Get to the cause we actually care about in case the bubbled up exception is a
     * higher level framework exception that encapsulates the stuff we really care
     * about.
     * 
     */
    private static Class<? extends Throwable> unravelReflectionExceptions(Throwable e) {
        while (e.getCause() != null
                && (e instanceof UndeclaredThrowableException || e instanceof InvocationTargetException)) {
            e = e.getCause();
        }
        return e.getClass();
    }

    @Override
    public Context startEntityRequest(String operation, String entity, String version) {
        return new DropwizardContext(name(API, operation, entity, version));
    }

    @Override
    public Context startStreamingEntityRequest(String operation, String entity, String version) {
        return new DropwizardContext(name(API, "stream", operation, entity, version));
    }
    
    @Override
    public Context startLockRequest(String lockOperation, String domain) {
        return new DropwizardContext(name(API, "lock", domain, lockOperation));
    }

    @Override
    public Context startBulkRequest() {
        return new DropwizardContext(name(API, "bulk"));
    }

    public class DropwizardContext implements Context {
        
        private final String metricNamespace;
        private final Timer.Context context;
        private final Counter activeRequests;
        private boolean ended = false;

        public DropwizardContext(String metricNamespace) {
            this.metricNamespace = Objects.requireNonNull(metricNamespace, "metricNamespace");
            this.context = metricsRegistry.timer(name(metricNamespace, "requests", "latency")).time();
            this.activeRequests = metricsRegistry.counter(name(metricNamespace, "requests", "active"));

            activeRequests.inc();
        }

        @Override
        public void endRequestMonitoring() {
            if (!ended) {
                ended = true;
                activeRequests.dec();
                context.stop();
            } else {
                LOGGER.warn("Request already ended for :: " + metricNamespace);
            }
        }

        @Override
        public void markRequestException(Error e) {
            String errorCode = e.getErrorCode().replaceAll(REGEX, ".");
            metricsRegistry.meter(errorNamespace(metricNamespace, e, errorCode)).mark();            
        }
        
        @Override
        public void markRequestException(Exception e) {
        	String errorMessage = e.getMessage().replaceAll(REGEX, ".");
        	metricsRegistry.meter(errorNamespace(metricNamespace, e, errorMessage)).mark();
        }

        @Override
        public void markAllErrorsAndEndRequestMonitoring(List<? extends Error> errors) {
            for (Error e : errors) {
               markRequestException(e);
             }
            endRequestMonitoring();
        }
    }
}
