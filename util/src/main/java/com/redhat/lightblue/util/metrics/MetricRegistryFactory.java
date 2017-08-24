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

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

/**
 * Registers JVM metrics and initializes JMX reporter to report all metrics, 
 * also provides a singleton instance of metricregistry to register rest endpoint metrics
 */
public class MetricRegistryFactory {

    private static MetricRegistry METRIC_REGISTRY = null;
    
    private MetricRegistryFactory(){}
    
    private static void initializeJVMMetrics() {
        METRIC_REGISTRY.register("garbage-collector", new GarbageCollectorMetricSet());
        METRIC_REGISTRY.register("buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        METRIC_REGISTRY.register("memory", new MemoryUsageGaugeSet());
        METRIC_REGISTRY.register("threads", new ThreadStatesGaugeSet());
    }
    
    private static void initializeJMXReporting() {    
        final JmxReporter jmxReporter = JmxReporter.forRegistry(METRIC_REGISTRY)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).build();
        jmxReporter.start();
    }
    
    public static synchronized MetricRegistry getJmxMetricRegistry() {
        if (METRIC_REGISTRY == null) {
            METRIC_REGISTRY = new MetricRegistry();
            initializeJVMMetrics();
            initializeJMXReporting();
        }
        return METRIC_REGISTRY;
    }
}
