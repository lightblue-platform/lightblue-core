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
package com.redhat.lightblue.util;

import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.servo.publish.*;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import com.redhat.lightblue.util.statsd.StatsdMetricObserver;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for publishing hystrix and jvm stats from servo to graphite
 * statsd. If the app is running in OpenShift it will setup an appropriate
 * metric prefix, else defaults to the hostname of where the app is running.
 *
 * @author nmalik
 */
public final class ServoGraphiteSetup {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServoGraphiteSetup.class);

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

    public static void initialize() {
        // Without GRAPHITE_HOSTNAME variable, graphite will not start
        String env = System.getenv("GRAPHITE_HOSTNAME");
        LOGGER.debug("GRAPHITE_HOSTNAME environment variable in initialize() is " + env);
        if (!initialized && env != null && !env.trim().isEmpty()) {
            doInitialize();
        }
    }

    /**
     * If there is sufficient configuration, register a Graphite observer to
     * publish metrics. Requires at a minimum environment variable
     * GRAPHITE_HOSTNAME. Optionally can set GRAPHITE_PREFIX as well as
     * GRAPHITE_PORT. GRAPHITE_PREFIX defaults to the hostname and GRAPHITE_PORT
     * defaults to '2004'.
     */
    private static void registerGraphiteMetricObserver(List<MetricObserver> observers) {
        String prefix = System.getenv("GRAPHITE_PREFIX");
        String host = System.getenv("GRAPHITE_HOSTNAME");
        String port = System.getenv("GRAPHITE_PORT");

        // verify at least hostname is set, else cannot configure this observer
        if (null == host || host.trim().isEmpty()) {
            LOGGER.info("GraphiteMetricObserver not configured, missing environment variable: GRAPHITE_HOSTNAME");
            return;
        }

        LOGGER.debug("GRAPHITE_PREFIX environment variable is: " + prefix);
        LOGGER.debug("GRAPHITE_HOSTNAME environment variable is: " + host);
        LOGGER.debug("GRAPHITE_PORT environment variable is: " + port);

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

        if (port == null) {
            port = "2004"; //default graphite port
            LOGGER.debug("Using default port: " + port);
        }

        String addr = host + ":" + port;

        LOGGER.debug("GraphiteMetricObserver prefix: " + prefix);
        LOGGER.debug("GraphiteMetricObserver address: " + addr);

        observers.add(new GraphiteMetricObserver(prefix, addr));
    }

    /**
     * If there is sufficient configuration, register a StatsD metric observer
     * to publish metrics. Requires at a minimum environment variable
     * STATSD_HOSTNAME. Optionally can set STATSD_PREFIX as well as STATDS_PORT.
     * STATSD_PREFIX defaults to an empty string. STATSD_PORT defaults to
     * '8125'.
     */
    private static void registerStatsdMetricObserver(List<MetricObserver> observers) {
        String prefix = System.getenv("STATSD_PREFIX");
        String host = System.getenv("STATSD_HOSTNAME");
        String port = System.getenv("STATSD_PORT");

        // verify at least hostname is set, else cannot configure this observer
        if (null == host || host.trim().isEmpty()) {
            LOGGER.info("StatdsMetricObserver not configured, missing environment variable: STATSD_HOSTNAME");
            return;
        }

        LOGGER.debug("STATSD_PREFIX environment variable is: " + prefix);
        LOGGER.debug("STATSD_HOSTNAME environment variable is: " + host);
        LOGGER.debug("STATSD_PORT environment variable is: " + port);

        int iport = -1;

        try {
            iport = Integer.valueOf(port);
        } catch (NumberFormatException e) {
            iport = -1;
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

    private static synchronized void doInitialize() {
        if (initialized) {
            return;
        }

        // register hystrix servo metrics publisher, required for collecting hystrix metrics
        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixServoMetricsPublisher.getInstance());
        // if IllegalStateException is thrown it means there is a hytrix command being used prior to this setup.
        // SEE: https://github.com/lightblue-platform/lightblue-rest/issues/58
        //      https://github.com/Netflix/Hystrix/issues/150

        List<MetricObserver> observers = new ArrayList<>();

        registerGraphiteMetricObserver(observers);
        registerStatsdMetricObserver(observers);

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
}
