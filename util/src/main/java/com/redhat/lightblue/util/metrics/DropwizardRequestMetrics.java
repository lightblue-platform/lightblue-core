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
import com.codahale.metrics.DefaultObjectNameFactory;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ObjectNameFactory;
import com.codahale.metrics.Timer;
import com.redhat.lightblue.util.Error;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;


public class DropwizardRequestMetrics implements RequestMetrics {

    private final Logger LOGGER = LoggerFactory.getLogger(DropwizardRequestMetrics.class);

    private static final String API = "api";

    private final MetricRegistry metricsRegistry;

    public static DropwizardRequestMetrics withMBeans(MetricRegistry registry) {
        final JmxReporter jmxReporter = JmxReporter.forRegistry(registry)
                .filter((name, metric) -> name.startsWith(API + "."))
                .createsObjectNamesWith(new MetricsObjectNameFactory())
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        jmxReporter.start();

        return new DropwizardRequestMetrics(registry);
    }

    public DropwizardRequestMetrics(MetricRegistry metricRegistry) {
        metricsRegistry = metricRegistry;
    }

    // metrics:type=timers,entity=user,operation=find-streaming,version=default,error=
    // app.api.${operation}.${entity}.versions.{$version}.errors.${error}
    //
    @Override
    public Context startCrudRequest(String operation, String entity, String version) {
        return new DropwizardContext(new EntityMetric(operation, entity, version));
    }

    @Override
    public Context startStreamingCrudRequest(String operation, String entity, String version) {
        // TODO own metric type?
        return new DropwizardContext(new EntityMetric(operation + "-streaming", entity, version));
    }

    // metrics:type=timers,lockCommand=acquire,domain=foo
    // app.api.locks.${domain}.${lockCommand}.errors.${error}
    @Override
    public Context startLockRequest(String lockOperation, String domain) {
        return new DropwizardContext(new LockMetric(domain, lockOperation));
    }

    // metrics:type=timers,operation=health,error=*
    @Override
    public Context startHealthRequest() {
        return new DropwizardContext(new GenericOperationMetric("health"));
    }

    @Override
    public Context startDiagnosticsRequest() {
        return new DropwizardContext(new GenericOperationMetric("diagnostics"));
    }

    // metrics:type=timers,savedSearch=byId,entity=foo,version=default,error=*
    // app.api.saved-search.foo.byId.default
    // app.api.saved-search.${entity}.${savedSearch}.versions.${version}.errors.${error}
    @Override
    public Context startSavedSearchRequest(String searchName, String entity, String version) {
        return new DropwizardContext(new SavedSearchMetric(entity, searchName, version));
    }

    // metrics:type=timers,operation=bulk,error=*
    // app.api.bulk
    @Override
    public Context startBulkRequest() {
        return new DropwizardContext(new GenericOperationMetric("bulk"));
    }

    @Override
    public Context startGenerateRequest(String entity, String version, String field) {
        return new DropwizardContext(new GenerateMetric(entity, version, field));
    }

    private class DropwizardContext implements Context {
        private final Metric metric;
        private final Timer.Context context;
        private final Counter activeRequests;
        private boolean ended = false;

        DropwizardContext(Metric metric) {
            this.metric = metric;
            this.context = metric.requestTimer(metricsRegistry).time();
            this.activeRequests = metric.activeRequestCounter(metricsRegistry);

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
            metric.errorMeter(metricsRegistry, e.getErrorCode()).mark();
        }

        @Override
        public void markRequestException(Exception e) {
            metric.errorMeter(metricsRegistry, unravelReflectionExceptions(e).getName()).mark();
        }

        @Override
        public void markAllErrorsAndEndRequestMonitoring(List<? extends Error> errors) {
            for (Error e : errors) {
                markRequestException(e);
            }
            endRequestMonitoring();
        }
    }

    private static class MetricsObjectNameFactory implements ObjectNameFactory {
        static ObjectNameFactory _default = new DefaultObjectNameFactory();

        @Override
        public ObjectName createName(String type, String domain, String name) {
            StringTokenizer metricTokens = new StringTokenizer(name, ".");

            if (!metricTokens.hasMoreTokens() || !metricTokens.nextToken().equals(API)) {
                // ??? Use default
                return _default.createName(type, domain, name);
            }

            Map<String, String> metricProperties = parseMetricProperties(metricTokens);
            Hashtable<String, String> properties = new Hashtable<>(metricProperties.size() + 1);
            properties.putAll(metricProperties);
            properties.put("type", type);

            try {
                return new ObjectName(domain, properties);
            } catch (MalformedObjectNameException e) {
                return _default.createName(type, domain, name);
            }
        }

        private Map<String, String> parseMetricProperties(StringTokenizer metricTokens) {
            String namespace = metricTokens.nextToken();

            switch (namespace) {
                case LockMetric.NAMESPACE:
                    return LockMetric.parseProperties(metricTokens);
                case SavedSearchMetric.NAMESPACE:
                    return SavedSearchMetric.parseProperties(metricTokens);
                case EntityMetric.NAMESPACE:
                    return EntityMetric.parseProperties(metricTokens);
                case GenerateMetric.NAMESPACE:
                    return GenerateMetric.parseProperties(metricTokens);
                default:
                    return GenericOperationMetric.parseProperties(namespace, metricTokens);
            }
        }
    }

    private abstract static class Metric {
        private static final String ERROR_TOKEN = "errors";

        final Timer requestTimer(MetricRegistry registry) {
            return registry.timer(name(API, toString()));
        }

        final Counter activeRequestCounter(MetricRegistry registry) {
            return registry.counter(name(API, toString(), "active"));
        }

        final Meter errorMeter(MetricRegistry registry, String errorTypeOrCode) {
            return registry.meter(name(API, toString(), ERROR_TOKEN, escape(errorTypeOrCode)));
        }

        /** A parseable metric name including a beginning namespace token for the metric type. */
        public abstract String toString();

        /**
         * Parses the error code (if present) from tokens leftover after {@link #toString()} content
         * is parsed and defines a standard "error" property for its contents.
         */
        static void putErrorPropertyIfPresent(
                StringTokenizer leftoverTokens, Map<String, String> properties) {
            if (leftoverTokens.hasMoreTokens() &&
                    ERROR_TOKEN.equals(leftoverTokens.nextToken()) &&
                    leftoverTokens.hasMoreTokens()) {
                properties.put("error", leftoverTokens.nextToken());
            }
        }
    }

    private static class GenericOperationMetric extends Metric {
        private final String operation;

        GenericOperationMetric(String unescapedOperation) {
            // This request metric is special: no specific namespace / catch-all
            operation = escape(unescapedOperation);
        }

        @Override
        public String toString() {
            return operation;
        }

        public static Map<String, String> parseProperties(
                String namespace, StringTokenizer metricTokens) {
            Map<String, String> properties = new HashMap<>(2);
            properties.put("operation", namespace);
            putErrorPropertyIfPresent(metricTokens, properties);

            return properties;
        }
    }

    private static class EntityMetric extends Metric {
        static final String NAMESPACE = "crud";

        private final String encoded;

        private EntityMetric(String unescapedOperation, String unescapedEntity,
                String unescapedVersion) {
            String operation = escape(unescapedOperation);
            String entity = escape(unescapedEntity);
            String version = escapeVersion(unescapedVersion);

            encoded = name(NAMESPACE, operation, entity, version);
        }

        @Override
        public String toString() {
            return encoded;
        }

        public static Map<String, String> parseProperties(StringTokenizer metricTokens) {
            Map<String, String> properties = new HashMap<>(4);

            properties.put("operation", metricTokens.nextToken());
            properties.put("entity", metricTokens.nextToken());
            properties.put("version", metricTokens.nextToken());
            putErrorPropertyIfPresent(metricTokens, properties);

            return properties;
        }
    }

    private static class LockMetric extends Metric {
        static final String NAMESPACE = "locks";

        private final String encoded;

        private LockMetric(String unescapedDomain, String unescapedLockCommand) {
            encoded = name(NAMESPACE, escape(unescapedDomain), escape(unescapedLockCommand));
        }

        public String toString() {
            return encoded;
        }

        public static Map<String, String> parseProperties(StringTokenizer metricTokens) {
            Map<String, String> properties = new HashMap<>(3);

            properties.put("domain", metricTokens.nextToken());
            properties.put("lockCommand", metricTokens.nextToken());
            putErrorPropertyIfPresent(metricTokens, properties);

            return properties;
        }
    }

    private static class SavedSearchMetric extends Metric {
        static final String NAMESPACE = "saved-search";

        private final String encoded;

        public SavedSearchMetric(
                String unescapedEntity, String unescapedSearchName, String unescapedVersion) {
            encoded = name(NAMESPACE, escape(unescapedEntity), escape(unescapedSearchName),
                    escapeVersion(unescapedVersion));
        }

        @Override
        public String toString() {
            return encoded;
        }

        public static Map<String, String> parseProperties(StringTokenizer metricTokens) {
            Map<String, String> properties = new HashMap<>(4);

            properties.put("entity", metricTokens.nextToken());
            properties.put("searchName", metricTokens.nextToken());
            properties.put("version", metricTokens.nextToken());
            putErrorPropertyIfPresent(metricTokens, properties);

            return properties;
        }
    }

    private static class GenerateMetric extends Metric {

        public GenerateMetric(String unescapedEntity, String unescapedVersion,
                String unescapedField) {

        }

        @Override
        public String toString() {
            return null;
        }
    }

    /**
     * Replace all . in version with _ as Dropwizard conventions (and metric systems like Graphite)
     * treat . as a path component separator. In order to have stable meaning of path components, we
     * must escape . in individual components.
     */
    private static String escape(String unescaped) {
        if (unescaped == null) return null;
        return unescaped.replace('.', '_');
    }

    /**
     * If version is null, replace with default. Then calls {@link #escape(String)}.
     */
    private static String escapeVersion(String version) {
        return StringUtils.isEmpty(version) ? "default" : escape(version);
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
