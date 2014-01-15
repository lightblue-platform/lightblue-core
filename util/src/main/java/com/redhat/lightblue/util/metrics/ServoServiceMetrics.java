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
package com.redhat.lightblue.util.metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.netflix.servo.DefaultMonitorRegistry;
import com.netflix.servo.annotations.DataSourceType;
import com.netflix.servo.annotations.Monitor;
import com.netflix.servo.monitor.BasicCounter;
import com.netflix.servo.monitor.BasicGauge;
import com.netflix.servo.monitor.Counter;
import com.netflix.servo.monitor.Gauge;
import com.netflix.servo.monitor.MonitorConfig;
import com.netflix.servo.monitor.Monitors;
import com.netflix.servo.publish.BasicMetricFilter;
import com.netflix.servo.publish.FileMetricObserver;
import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.MonitorRegistryMetricPoller;
import com.netflix.servo.publish.PollRunnable;
import com.netflix.servo.publish.PollScheduler;
import com.netflix.servo.tag.InjectableTag;
import com.netflix.servo.tag.Tag;

/**
 * Provides a single place to capture all metrics for the service.
 * 
 * Right now this uses Servo (https://github.com/Netflix/servo/wiki) to capture
 * metrics. Tags used for each metric are HOSTNAME and IP.
 * 
 * @author nmalik
 */
public class ServoServiceMetrics implements ServiceMetrics {
    private static Logger log = Logger.getLogger(ServoServiceMetrics.class.getName());

    /** Name of the monitor used by Servo. */
    private static String MONITOR_ID = ServoServiceMetrics.class.getSimpleName();

    /**
     * Semaphore used for creating instance and for creating new metric objects.
     */
    private static Semaphore semaphore = new Semaphore(1);

    /** Singleton instance of this class. */
    private static ServiceMetrics server;

    /**
     * Create or return the singleton instance of this class.
     * 
     * @return singleton instance
     */
    public static ServiceMetrics getInstance() {
        if (server == null) {
            try {
                semaphore.acquire();
                if (server == null) {
                    ServiceMetrics tmp = new ServoServiceMetrics();
                    server = tmp;
                }
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Failed to instantiate " + ServoServiceMetrics.class.getSimpleName(), e);
            } finally {
                semaphore.release();
            }
        }

        return server;
    }

    @Monitor(name = "Status", type = DataSourceType.INFORMATIONAL)
    private AtomicReference<String> status = new AtomicReference<String>("UP");

    private Map<String, Counter> counters = new HashMap<String, Counter>();

    private Map<String, Gauge<Number>> gauges = new HashMap<String, Gauge<Number>>();

    private Map<String, AtomicInteger> gaugeData = new HashMap<String, AtomicInteger>();

    private ServoServiceMetrics() {
        // create tag list
        List<Tag> tags = new ArrayList<Tag>(2);
        tags.add(InjectableTag.HOSTNAME);
        tags.add(InjectableTag.IP);

        // register self
        Monitors.registerObject(String.valueOf(MONITOR_ID), this);

        // create poller and file observer
        PollScheduler scheduler = PollScheduler.getInstance();
        scheduler.start();

        MetricObserver fileObserver = new FileMetricObserver("metrics", new File(System.getProperty("OPENSHIFT_JBOSSEAP_LOG_DIR", "/tmp")));
        PollRunnable task = new PollRunnable(new MonitorRegistryMetricPoller(), BasicMetricFilter.MATCH_ALL, fileObserver);
        scheduler.addPoller(task, 1, TimeUnit.MINUTES);
    }

    /**
     * Gets the data for the given gauge name. If the gauge doesn't exist,
     * creates it with tags HOSTNAME and IP.
     * 
     * @param name
     *            - the name of the gauge
     * @return
     */
    private AtomicInteger getGaugeData(String name) {
        if (!gaugeData.containsKey(name)) {
            // gauge doesn't exist, create it and add to collection
            final AtomicInteger data = new AtomicInteger();
            data.set(0);

            Callable<Number> function = new Callable<Number>() {
                @Override
                public Number call() throws Exception {
                    // TODO Auto-generated method stub
                    return data;
                }
            };

            BasicGauge<Number> gauge = new BasicGauge<Number>(MonitorConfig.builder(name).build(), function);
            DefaultMonitorRegistry.getInstance().register(gauge);

            gaugeData.put(name, data);
            gauges.put(name, gauge);
        }

        return gaugeData.get(name);
    }

    @Override
    public Number incrementCounter(String name) {
        if (!counters.containsKey(name)) {
            try {
                semaphore.acquire();
                if (!counters.containsKey(name)) {
                    BasicCounter counter = new BasicCounter(MonitorConfig.builder(name).build());
                    counters.put(name, counter);
                    DefaultMonitorRegistry.getInstance().register(counter);
                }
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Failed to create counter " + name, e);
            } finally {
                semaphore.release();
            }
        }
        counters.get(name).increment();
        return counters.get(name).getValue();
    }

    @Override
    public Set<String> getCounterNames() {
        return counters.keySet();
    }

    @Override
    public Number getCounterValue(String name) {
        if (counters.containsKey(name)) {
            return counters.get(name).getValue();
        } else {
            return -1;
        }
    }

    @Override
    public Number incrementGauge(String name) {
        AtomicInteger data = getGaugeData(name);

        return data.incrementAndGet();
    }

    @Override
    public Number decrementGauge(String name) {
        AtomicInteger data = getGaugeData(name);

        return data.decrementAndGet();
    }

    @Override
    public Number setGauge(String name, int value) {
        AtomicInteger data = getGaugeData(name);

        data.set(value);

        return data.get();
    }

    @Override
    public Set<String> getGaugeNames() {
        return gaugeData.keySet();
    }

    @Override
    public Number getGaugeValue(String name) {
        return getGaugeData(name);
    }
}
