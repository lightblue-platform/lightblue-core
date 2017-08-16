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

import java.util.Objects;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Timer;
import com.codahale.metrics.MetricRegistry;

public class DropwizardRequestMetrics implements RequestMetrics {
    
    private final Logger LOGGER = LoggerFactory.getLogger(DropwizardRequestMetrics.class);

    private static final String API = "api";

    private final MetricRegistry metricsRegistry;
    
    private boolean isBulkRequest = false;

    public boolean isBulkRequest() {
        return isBulkRequest;
    }

    public void setBulkRequest(boolean isBulkRequest) {
        this.isBulkRequest = isBulkRequest;
    }

    public DropwizardRequestMetrics(MetricRegistry metricRegistry) {
        metricsRegistry = metricRegistry;
    }

    /**
     * Create exception namespace for metric reporting based on exception name
     * 
     */
    private static String errorNamespace(String metricNamespace, Throwable exception) {
        Class<? extends Throwable> actualExceptionClass = unravelReflectionExceptions(exception);
        return name(metricNamespace, "requests", "exception", actualExceptionClass.getSimpleName());
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
    public Context startLockRequest(String lockOperation, String domain) {
        return new DropwizardContext(name(API, "lock", domain, lockOperation));
    }

    @Override
    public Context startBulkRequest(String bulkOperation, String entity, String version) {
        return new DropwizardContext(name(API, "bulk", bulkOperation, entity, version));
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
                LOGGER.warn("Request already ended");
            }
        }

        @Override
        public void markRequestException(Exception e) {
            metricsRegistry.meter(errorNamespace(metricNamespace, e)).mark();
        }

        @Override
        public void endRequestMonitoringWithException(Exception e) {
            endRequestMonitoring();
            markRequestException(e);
        }
    }
}
