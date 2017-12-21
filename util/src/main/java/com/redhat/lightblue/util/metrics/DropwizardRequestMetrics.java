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

import com.codahale.metrics.Counter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.redhat.lightblue.util.Error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class DropwizardRequestMetrics implements RequestMetrics {

    private final Logger LOGGER = LoggerFactory.getLogger(DropwizardRequestMetrics.class);

    private final MetricRegistry metricRegistry;
    private final MetricNamer metricNamer;

    public interface MetricNamer {
        RequestMetric crud(String operation, String entity, String version);
        RequestMetric streamingCrud(String operation, String entity, String version);
        RequestMetric lock(String domain, String lockCommand);
        RequestMetric savedSearch(String entity, String searchName, String version);
        RequestMetric diagnostics();
        RequestMetric health();
        RequestMetric bulk();
        RequestMetric generate(String entity, String version, String field);
    }

    public static DropwizardRequestMetrics withDefaultMBeans(MetricRegistry registry) {
        DefaultMetricNamer metricNamer = new DefaultMetricNamer();

        final JmxReporter jmxReporter = JmxReporter.forRegistry(registry)
                .filter(metricNamer)
                .createsObjectNamesWith(metricNamer)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        jmxReporter.start();

        return new DropwizardRequestMetrics(registry, metricNamer);
    }

    public DropwizardRequestMetrics(MetricRegistry metricRegistry) {
        this(metricRegistry, new DefaultMetricNamer());
    }

    public DropwizardRequestMetrics(MetricRegistry metricRegistry, MetricNamer metricNamer) {
        this.metricRegistry = metricRegistry;
        this.metricNamer = metricNamer;
    }

    @Override
    public Context startCrudRequest(String operation, String entity, String version) {
        return new DropwizardContext(metricNamer.crud(operation, entity, version));
    }

    @Override
    public Context startStreamingCrudRequest(String operation, String entity, String version) {
        return new DropwizardContext(metricNamer.streamingCrud(operation, entity, version));
    }

    @Override
    public Context startLockRequest(String lockOperation, String domain) {
        return new DropwizardContext(metricNamer.lock(domain, lockOperation));
    }

    @Override
    public Context startHealthRequest() {
        return new DropwizardContext(metricNamer.health());
    }

    @Override
    public Context startDiagnosticsRequest() {
        return new DropwizardContext(metricNamer.diagnostics());
    }

    @Override
    public Context startSavedSearchRequest(String searchName, String entity, String version) {
        return new DropwizardContext(metricNamer.savedSearch(entity, searchName, version));
    }

    @Override
    public Context startBulkRequest() {
        return new DropwizardContext(metricNamer.bulk());
    }

    @Override
    public Context startGenerateRequest(String entity, String version, String field) {
        return new DropwizardContext(metricNamer.generate(entity, version, field));
    }

    private class DropwizardContext implements Context {
        private final RequestMetric metric;
        private final Timer.Context context;
        private final Counter activeRequests;
        private boolean ended = false;

        DropwizardContext(RequestMetric metric) {
            this.metric = metric;
            this.context = metric.requestTimer(metricRegistry).time();
            this.activeRequests = metric.activeRequestCounter(metricRegistry);

            activeRequests.inc();
        }

        @Override
        public void endRequestMonitoring() {
            if (!ended) {
                ended = true;
                activeRequests.dec();
                context.stop();
            } else {
                LOGGER.warn("Request already ended for: {}", metric);
            }
        }

        @Override
        public void markRequestException(Error e) {
            metric.errorMeter(metricRegistry, e.getErrorCode()).mark();
        }

        @Override
        public void markRequestException(Exception e) {
            metric.errorMeter(metricRegistry, unravelReflectionExceptions(e).getName()).mark();
        }

        @Override
        public void markAllErrorsAndEndRequestMonitoring(List<? extends Error> errors) {
            for (Error e : errors) {
                markRequestException(e);
            }
            endRequestMonitoring();
        }
    }

    /**
     * Get to the cause we actually care about in case the bubbled up exception is a
     * higher level framework exception that encapsulates the stuff we really care
     * about.
     */
    private static Class<? extends Throwable> unravelReflectionExceptions(Throwable e) {
        while (e.getCause() != null &&
                (e instanceof UndeclaredThrowableException ||
                        e instanceof InvocationTargetException)) {
            e = e.getCause();
        }
        return e.getClass();
    }
}

