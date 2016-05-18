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
package com.redhat.lightblue.hystrix;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.JvmMetricPoller;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import com.redhat.lightblue.hystrix.statsd.StatsdMetricObserver;

/**
 * Utility class for publishing hystrix and jvm stats from servo to graphite
 * statsd. If the app is running in OpenShift it will setup an appropriate
 * metric prefix, else defaults to the hostname of where the app is running.
 *
 * @author nmalik
 */
public final class ServoGraphiteSetup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServoGraphiteSetup.class);

    public static final String ENV_GRAPHITE_PREFIX = "GRAPHITE_PREFIX";
    public static final String ENV_GRAPHITE_HOSTNAME = "GRAPHITE_HOSTNAME";
    public static final String ENV_GRAPHITE_PORT = "GRAPHITE_PORT";
    public static final String ENV_STATSD_PREFIX = "STATSD_PREFIX";
    public static final String ENV_STATSD_HOSTNAME = "STATSD_HOSTNAME";
    public static final String ENV_STATSD_PORT = "STATSD_PORT";

    private static boolean initialized = false;

    private ServoGraphiteSetup() {
    }

    public static void stop() {
        LOGGER.debug("stop() method called, initialized=" + initialized);
        if (initialized) {
            doStop();
        }
    }

    private static void doStop() {
        if (!initialized) {
            return;
        }
        PollScheduler.getInstance().stop();
        LOGGER.debug("PollScheduler.getInstance().stop() completed");
        initialized = false;
        LOGGER.debug("doStop() complete, initialized = " + initialized);
    }

    /**
     * For backwards compatibility. Defaults to {@link Type#STATSD}.
     */
    public static void initialize() {
        initialize(Type.STATSD);
    }

    public static void initialize(final Type... types) {
        if (!initialized) {
            doInitialize(types);
        }
    }

    /**
     * If there is sufficient configuration, register a Graphite observer to
     * publish metrics. Requires at a minimum a host. Optionally can set prefix
     * as well as port. The prefix defaults to the host and port defaults to
     * '2004'.
     *
     * @param observers the list of observers to add any new observer to
     * @param prefix the graphite prefix
     * @param host the graphite host
     * @param port the graphite port
     */
    protected static void registerGraphiteMetricObserver(List<MetricObserver> observers, String prefix, String host, String port) {
        // verify at least hostname is set, else cannot configure this observer
        if (null == host || host.trim().isEmpty()) {
            LOGGER.info("GraphiteMetricObserver not configured, missing environment variable: {}", ENV_GRAPHITE_HOSTNAME);
            return;
        }

        LOGGER.debug("{} environment variable is: {}", ENV_GRAPHITE_PREFIX, prefix);
        LOGGER.debug("{} environment variable is: {}", ENV_GRAPHITE_HOSTNAME, host);
        LOGGER.debug("{} environment variable is: {}", ENV_GRAPHITE_PORT, port);

        if (prefix == null) {
            if (System.getenv("OPENSHIFT_APP_NAME") != null) {
                // try to get name from openshift.  assume it's scaleable app.
                // format: <app name>.  <namespace>.<gear dns>
                prefix = String.format(
                        "%s.%s.%s",
                        System.getenv("OPENSHIFT_APP_NAME"),
                        System.getenv("OPENSHIFT_NAMESPACE"),
                        System.getenv("OPENSHIFT_GEAR_DNS")
                );
            } else {
                //default
                prefix = System.getenv("HOSTNAME");
                LOGGER.debug("using HOSTNAME as default prefix" + prefix);
            }
        }

        int iport = -1;
        if (port != null && !port.isEmpty()) {
            try {
                iport = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                iport = -1;
                LOGGER.warn("Configured port is not an integer.  Falling back to default");
            }
        }

        if (iport < 0) {
            iport = 2004; //default graphite port
            LOGGER.debug("Using default port: " + iport);
        }

        String addr = host + ":" + iport;

        LOGGER.debug("GraphiteMetricObserver prefix: " + prefix);
        LOGGER.debug("GraphiteMetricObserver address: " + addr);

        observers.add(new GraphiteMetricObserver(prefix, addr));
    }

    /**
     * If there is sufficient configuration, register a StatsD metric observer
     * to publish metrics. Requires at a minimum a host. Optionally can set
     * prefix as well as port. The prefix defaults to an empty string and port
     * defaults to '8125'.
     */
    protected static void registerStatsdMetricObserver(List<MetricObserver> observers, String prefix, String host, String port) {
        // verify at least hostname is set, else cannot configure this observer
        if (null == host || host.trim().isEmpty()) {
            LOGGER.info("StatdsMetricObserver not configured, missing environment variable: {}", ENV_STATSD_HOSTNAME);
            return;
        }

        LOGGER.debug("{} environment variable is: {}", ENV_STATSD_PREFIX, prefix);
        LOGGER.debug("{} environment variable is: {}", ENV_STATSD_HOSTNAME, host);
        LOGGER.debug("{} environment variable is: {}", ENV_STATSD_PORT, port);

        int iport = -1;
        if (port != null && !port.isEmpty()) {
            try {
                iport = Integer.valueOf(port);
            } catch (NumberFormatException e) {
                iport = -1;
                LOGGER.warn("Configured port is not an integer.  Falling back to default");
            }
        }

        if (iport < 0) {
            iport = 8125; //default statsd port
            LOGGER.debug("Using default port: " + port);
        }

        LOGGER.debug("StatsdMetricObserver prefix: " + prefix);
        LOGGER.debug("StatsdMetricObserver host: " + host);
        LOGGER.debug("StatsdMetricObserver port: " + iport);

        observers.add(new StatsdMetricObserver(prefix, host, iport));
    }

    private static synchronized void doInitialize(final Type... types) {
        if (initialized) {
            return;
        }

        // Fix Java 7 problem that initialized Hystrix/Servo before the it has been even called
        Hystrix.reset();
        HystrixPlugins.reset();

        // register hystrix servo metrics publisher, required for collecting hystrix metrics
        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixServoMetricsPublisher.getInstance());
        // if IllegalStateException is thrown it means there is a hytrix command being used prior to this setup.
        // SEE: https://github.com/lightblue-platform/lightblue-rest/issues/58
        //      https://github.com/Netflix/Hystrix/issues/150

        List<MetricObserver> observers = new ArrayList<>();

        for (Type type : types) {
            switch (type) {
                case GRAPHITE:
                    registerGraphiteMetricObserver(observers, findVariable(ENV_GRAPHITE_PREFIX),
                            findVariable(ENV_GRAPHITE_HOSTNAME), findVariable(ENV_GRAPHITE_PORT));
                    break;
                case STATSD:
                    registerStatsdMetricObserver(observers, findVariable(ENV_STATSD_PREFIX),
                            findVariable(ENV_STATSD_HOSTNAME), findVariable(ENV_STATSD_PORT));
                    break;
                default:
                    throw new RuntimeException("Unsupported Type: " + type);
            }
        }

        // start poll scheduler
        PollScheduler.getInstance().start();

        // create and register registery poller
        PollRunnable registeryTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL, observers);
        PollScheduler.getInstance().addPoller(registeryTask, 5, TimeUnit.SECONDS);

        // create and register jvm poller
        PollRunnable jvmTask = new PollRunnable(new JvmMetricPoller(), BasicMetricFilter.MATCH_ALL, observers);
        PollScheduler.getInstance().addPoller(jvmTask, 5, TimeUnit.SECONDS);

        initialized = true;
        LOGGER.debug("doInitialize() completed, initialized = " + initialized);
    }

    /**
     * Looks for the value of the key as a key firstly as a JVM argument, and if
     * not found, to an environment variable. If still not found, then null is
     * returned.
     *
     * @param key
     * @return
     */
    private static String findVariable(String key) {
        String value = System.getProperty(key);
        if (value == null) {
            return System.getenv(key);
        }
        return value;
    }

    public enum Type {
        GRAPHITE,
        STATSD
    }
}
