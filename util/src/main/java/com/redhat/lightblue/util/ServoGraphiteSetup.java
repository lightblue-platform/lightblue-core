/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.redhat.lightblue.util;

import com.netflix.hystrix.contrib.servopublisher.HystrixServoMetricsPublisher;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.JvmMetricPoller;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for publishing hystrix and jvm stats from servo to graphite. If the app is running in OpenShift it will
 * setup an appropriate metric prefix, else defaults to the hostname of where the app is running.
 *
 * @author nmalik
 */
public class ServoGraphiteSetup {
    private static boolean initialized = false;

    public static final void initialize() {
        if (!initialized) {
            doInitialize();
        }
    }

    private static synchronized void doInitialize() {
        if (initialized) {
            return;
        }

        // register hystrix servo metrics publisher
        HystrixPlugins.getInstance().registerMetricsPublisher(HystrixServoMetricsPublisher.getInstance());

        // try to get name from openshift.  assume it's scaleable app.
        // format: <app name>.  <namespace>.<gear dns>
        String prefix = System.getenv("HOSTNAME"); // default

        if (System.getenv("OPENSHIFT_APP_NAME") != null) {
            prefix = String.format(
                    "%s.%s.%s",
                    System.getenv("OPENSHIFT_APP_NAME"),
                    System.getenv("OPENSHIFT_NAMESPACE"),
                    System.getenv("OPENSHIFT_GEAR_DNS")
            );
        }

        String host = System.getenv("GRAPHITE_HOSTNAME");
        String port = System.getenv("GRAPHITE_PORT");

        String addr = host + ":" + port;
        MetricObserver observer = new GraphiteMetricObserver(prefix, addr);

        // start poll scheduler  
        PollScheduler.getInstance().start();

        // create and register registery poller  
        PollRunnable registeryTask = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL, observer);
        PollScheduler.getInstance().addPoller(registeryTask, 5, TimeUnit.SECONDS);

        // create and register jvm poller  
        PollRunnable jvmTask = new PollRunnable(new JvmMetricPoller(), BasicMetricFilter.MATCH_ALL, observer);
        PollScheduler.getInstance().addPoller(jvmTask, 5, TimeUnit.SECONDS);

        initialized = true;
    }
}
