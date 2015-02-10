package com.redhat.lightblue.util.statsd;

import java.util.List;

import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.publish.BaseMetricObserver;
import com.netflix.servo.publish.graphite.BasicGraphiteNamingConvention;
import com.netflix.servo.publish.graphite.GraphiteNamingConvention;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics observer that sends hystrix data to statsd with the intent of then
 * then sending it to graphite.
 *
 * @author nmalik, dcrissman
 */
public class StatsdMetricObserver extends BaseMetricObserver {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatsdMetricObserver.class);

    private final String prefix;
    private final String host;
    private final int port;
    private final GraphiteNamingConvention namingConvention;

    public StatsdMetricObserver(String prefix, String host, int port) {
        this(prefix, host, port, new BasicGraphiteNamingConvention());
    }

    public StatsdMetricObserver(String prefix, String host, int port, GraphiteNamingConvention namingConvention) {
        super("StatsdMetricObserver." + prefix);
        this.prefix = prefix;
        this.host = host;
        this.port = port;
        this.namingConvention = namingConvention;
    }

    @Override
    public void updateImpl(List<Metric> metrics) {
        // The statsd client doesn't do any checks on the underlying socket's state
        // and the socket connects only once, so we cannot trust the socket to stay
        // open over a period of time.  If this is changed/fixed we could reuse the
        // client but until then it cannot be safely reused.
        StatsDClient statsd = new NonBlockingStatsDClient(prefix, host, port, errorHandlerInstance);
        LOGGER.debug("sending data");

        for (Metric metric : metrics) {
            String aspect = namingConvention.getName(metric);

            if (metric.getConfig().getTags().getTag(DataSourceType.COUNTER.getValue()) != null) {
                statsd.count(aspect, metric.getNumberValue().longValue());
            } else  if (metric.hasNumberValue()) {
                statsd.gauge(aspect, metric.getNumberValue().longValue());
            } else {
                statsd.set(aspect, metric.getValue().toString());
            }

            statsd.time(aspect, metric.getTimestamp() / 1000);
        }

        statsd.stop();
    }

    private static final ErrorHandler errorHandlerInstance = new ErrorHandler();

    private static class ErrorHandler implements StatsDClientErrorHandler {
        @Override
        public void handle(Exception exception) {
            LOGGER.error("Error publishing metrics to statsd", exception);
        }
    }
}
