/*
 Copyright 2015 Red Hat, Inc. and/or its affiliates.

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
package com.redhat.lightblue.hystrix.statsd;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.servo.Metric;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.publish.BaseMetricObserver;
import com.netflix.servo.publish.graphite.BasicGraphiteNamingConvention;
import com.netflix.servo.publish.graphite.GraphiteNamingConvention;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;

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

    protected StatsDClient createClient() {
        return new NonBlockingStatsDClient(prefix, host, port, errorHandlerInstance);
    }

    @Override
    public void updateImpl(List<Metric> metrics) {
        // The statsd client doesn't do any checks on the underlying socket's state
        // and the socket connects only once, so we cannot trust the socket to stay
        // open over a period of time.  If this is changed/fixed we could reuse the
        // client but until then it cannot be safely reused.
        StatsDClient statsd = createClient();
        LOGGER.debug("sending data");
        try {
            for (Metric metric : metrics) {
                String aspect = namingConvention.getName(metric);

                if (metric.getConfig().getTags().getTag(DataSourceType.COUNTER.getValue()) != null) {
                    statsd.count(aspect, metric.getNumberValue().longValue());
                } else if (metric.hasNumberValue()) {
                    statsd.gauge(aspect, metric.getNumberValue().longValue());
                } else {
                    statsd.set(aspect, metric.getValue().toString());
                }

                statsd.time(aspect, metric.getTimestamp() / 1000);
            }
        } finally {
            statsd.stop();
        }
    }

    private static final ErrorHandler errorHandlerInstance = new ErrorHandler();

    private static class ErrorHandler implements StatsDClientErrorHandler {
        @Override
        public void handle(Exception exception) {
            LOGGER.error("Error publishing metrics to statsd", exception);
        }
    }
}
