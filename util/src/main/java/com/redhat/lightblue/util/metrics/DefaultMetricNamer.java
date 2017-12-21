package com.redhat.lightblue.util.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Counter;
import com.codahale.metrics.DefaultObjectNameFactory;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ObjectNameFactory;
import com.codahale.metrics.Timer;
import org.apache.commons.lang3.StringUtils;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class DefaultMetricNamer implements DropwizardRequestMetrics.MetricNamer, ObjectNameFactory, MetricFilter {
    private static final ObjectNameFactory DEFAULT_ONF = new DefaultObjectNameFactory();
    private static final String PREFIX_TOKEN = "request";
    private static final String ERROR_TOKEN = "errors";

    /** @see #escape(String) */
    private final static Pattern ILLEGAL =
            Pattern.compile("[\\.@#!$%^&*()+|~={}:;'<>?,\"\\\\////\\s]");

    @Override
    public ObjectName createName(String type, String domain, String name) {
        StringTokenizer metricTokens = new StringTokenizer(name, ".");

        if (!metricTokens.hasMoreTokens() || !metricTokens.nextToken().equals(PREFIX_TOKEN)) {
            return DEFAULT_ONF.createName(type, domain, name);
        }

        String namespace = metricTokens.nextToken();
        // Instantiate table per namespace to keep unused capacity low. The metrics handling needs
        // to be lightweight. These are instantiated once per combination (not once per request),
        // but there can still be a large amount of combinations in typical cases.
        Hashtable<String, String> properties;

        switch (namespace) {
            // Each case uses count(properties) + one for "type" and one for "error".
            // There may not be an error, but we allocate capacity just in case.
            case "crud":
                properties = new Hashtable<>(5);
                properties.put("operation", metricTokens.nextToken());
                properties.put("entity", metricTokens.nextToken());
                properties.put("version", metricTokens.nextToken());
                break;
            case "lock":
                properties = new Hashtable<>(4);
                properties.put("domain", metricTokens.nextToken());
                properties.put("lockCommand", metricTokens.nextToken());
                break;
            case "saved-search":
                properties = new Hashtable<>(5);
                properties.put("entity", metricTokens.nextToken());
                properties.put("savedSearch", metricTokens.nextToken());
                properties.put("version", metricTokens.nextToken());
                break;
            case "generate":
                properties = new Hashtable<>(5);
                properties.put("entity", metricTokens.nextToken());
                properties.put("version", metricTokens.nextToken());
                properties.put("field", metricTokens.nextToken());
            default:
                properties = new Hashtable<>(3);
                properties.put("operation", namespace);
        }

        if (metricTokens.hasMoreTokens() &&
                ERROR_TOKEN.equals(metricTokens.nextToken()) &&
                metricTokens.hasMoreTokens()) {
            properties.put("error", metricTokens.nextToken());
        }

        properties.put("type", type);

        try {
            return new ObjectName(domain, properties);
        } catch (MalformedObjectNameException e) {
            return DEFAULT_ONF.createName(type, domain, name);
        }
    }

    @Override
    public boolean matches(String name, com.codahale.metrics.Metric metric) {
        return name.startsWith(PREFIX_TOKEN + ".");
    }

    @Override
    public RequestMetric crud(String operation, String entity, String version) {
        return new ParseableMetric(PREFIX_TOKEN, "crud", escape(operation), escape(entity),
                escapeVersion(version));
    }

    @Override
    public RequestMetric streamingCrud(String operation, String entity, String version) {
        return crud(operation + "-streaming", entity, version);
    }

    // metrics:type=timers,lockCommand=acquire,domain=foo
    // app.api.locks.${domain}.${lockCommand}.errors.${error}
    @Override
    public RequestMetric lock(String domain, String lockCommand) {
        return new ParseableMetric(PREFIX_TOKEN, "lock", escape(domain), escape(lockCommand));
    }

    // metrics:type=timers,savedSearch=byId,entity=foo,version=default,error=*
    // app.api.saved-search.foo.byId.default
    // app.api.saved-search.${entity}.${savedSearch}.versions.${version}.errors.${error}
    @Override
    public RequestMetric savedSearch(String entity, String searchName, String version) {
        return new ParseableMetric(PREFIX_TOKEN, "saved-search", escape(entity), escape(searchName),
                escapeVersion(version));
    }

    @Override
    public RequestMetric diagnostics() {
        return new ParseableMetric(PREFIX_TOKEN, "diagnostics");
    }

    // metrics:type=timers,operation=health,error=*
    @Override
    public RequestMetric health() {
        return new ParseableMetric(PREFIX_TOKEN, "health");
    }

    // metrics:type=timers,operation=bulk,error=*
    // app.api.bulk
    @Override
    public RequestMetric bulk() {
        return new ParseableMetric(PREFIX_TOKEN, "bulk");
    }

    @Override
    public RequestMetric generate(String entity, String version, String field) {
        return new ParseableMetric(PREFIX_TOKEN, "generate", escape(entity), escapeVersion(version),
                escape(field));
    }

    private static class ParseableMetric implements RequestMetric {
        private final String base;

        private ParseableMetric(String part, String... parts) {
            base = name(part, parts);
        }

        @Override
        public String toString() {
            return base;
        }

        @Override
        public final Timer requestTimer(MetricRegistry registry) {
            return registry.timer(base);
        }

        @Override
        public final Counter activeRequestCounter(MetricRegistry registry) {
            return registry.counter(name(base, "active"));
        }

        @Override
        public final Meter errorMeter(MetricRegistry registry, String errorTypeOrCode) {
            return registry.meter(name(base, ERROR_TOKEN, escapeErrorTypeOrCode(errorTypeOrCode)));
        }

        private static String escapeErrorTypeOrCode(String errorTypeOrCode) {
            return escape(errorTypeOrCode);
        }
    }

    /**
     * Replace all {@link #ILLEGAL} with _ as Dropwizard conventions (and metric systems like
     * Graphite) treat . as a path component separator, in addition to JMX ObjectNames having their
     * own set of restrictions.
     */
    private static String escape(String unescaped) {
        if (unescaped == null) return null;
        return ILLEGAL.matcher(unescaped).replaceAll("_");
    }

    /**
     * If version is null, replace with default. Then calls {@link #escape(String)}.
     */
    private static String escapeVersion(String version) {
        return StringUtils.isEmpty(version) ? "default" : escape(version);
    }
}
