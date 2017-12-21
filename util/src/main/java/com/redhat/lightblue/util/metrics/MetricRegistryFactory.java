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

import com.codahale.metrics.MetricRegistry;

/**
 * Registers JVM metrics and initializes JMX reporter to report all metrics, 
 * also provides a singleton instance of metricregistry to register rest endpoint metrics
 */
public class MetricRegistryFactory {

    private static MetricRegistry METRIC_REGISTRY = null;
    
    private MetricRegistryFactory() {}

    public static synchronized MetricRegistry getJmxMetricRegistry() {
        if (METRIC_REGISTRY == null) {
            METRIC_REGISTRY = new MetricRegistry();
        }
        return METRIC_REGISTRY;
    }
}
