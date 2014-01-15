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

import java.util.Set;

public interface ServiceMetrics {

    /**
     * Increment counter by name. If the counter doesn't exist, a new counter is
     * created with initial value of 1 and is registered.
     */
    public Number incrementCounter(String name);

    /**
     * Get names of all registered counters.
     * 
     * @return
     */
    public Set<String> getCounterNames();

    /**
     * Get value of counter by name. If counter doesn't exist returns -1.
     * 
     * @param name
     * @return
     */
    public Number getCounterValue(String name);

    /**
     * Increment gauge. Creates gauge with zero (0) value if it doesn't exist.
     * 
     * @param name
     *            - the name of the gauge
     */
    public Number incrementGauge(String name);

    /**
     * Decrement gauge. Creates gauge with zero (0) value if it doesn't exist.
     * 
     * @param name
     *            - the name of the gauge
     */
    public Number decrementGauge(String name);

    /**
     * Sets the value of a gauge. Creates gauge with zero (0) value if it
     * doesn't exist.
     * 
     * @param name
     *            - the name of the gauge
     * @param value
     *            - the new value of the gauge
     */
    public Number setGauge(String name, int value);

    public Set<String> getGaugeNames();

    public Number getGaugeValue(String name);
}