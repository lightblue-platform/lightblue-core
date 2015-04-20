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
package com.redhat.lightblue.hystrix;

import com.netflix.servo.publish.MetricObserver;
import com.netflix.servo.publish.graphite.GraphiteMetricObserver;
import com.redhat.lightblue.hystrix.statsd.StatsdMetricObserver;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author nmalik
 */
public class ServoGraphiteSetupTest {

    @Test
    public void no_observers() {
        List<MetricObserver> observers = new ArrayList<>();

        ServoGraphiteSetup.registerGraphiteMetricObserver(observers, null, null, null);
        ServoGraphiteSetup.registerStatsdMetricObserver(observers, null, null, null);

        Assert.assertTrue("Observer list is not empty", observers.isEmpty());
    }

    @Test
    public void graphite_only_minimal() {
        List<MetricObserver> observers = new ArrayList<>();

        ServoGraphiteSetup.registerGraphiteMetricObserver(observers, null, "localhost", null);
        ServoGraphiteSetup.registerStatsdMetricObserver(observers, null, null, null);

        Assert.assertFalse("Observer list is empty", observers.isEmpty());
        Assert.assertTrue(observers.get(0) instanceof GraphiteMetricObserver);
    }

    @Test
    public void graphite_only_invalid_port() {
        List<MetricObserver> observers = new ArrayList<>();

        ServoGraphiteSetup.registerGraphiteMetricObserver(observers, null, "localhost", "xxxx");
        ServoGraphiteSetup.registerStatsdMetricObserver(observers, null, null, null);

        Assert.assertFalse("Observer list is empty", observers.isEmpty());
        Assert.assertTrue(observers.get(0) instanceof GraphiteMetricObserver);
    }

    @Test
    public void statsd_only_minimal() {
        List<MetricObserver> observers = new ArrayList<>();

        ServoGraphiteSetup.registerGraphiteMetricObserver(observers, null, null, null);
        ServoGraphiteSetup.registerStatsdMetricObserver(observers, null, "localhost", null);

        Assert.assertFalse("Observer list is empty", observers.isEmpty());
        Assert.assertTrue(observers.get(0) instanceof StatsdMetricObserver);
    }

    @Test
    public void statsd_only_invalid_port() {
        List<MetricObserver> observers = new ArrayList<>();

        ServoGraphiteSetup.registerGraphiteMetricObserver(observers, null, null, null);
        ServoGraphiteSetup.registerStatsdMetricObserver(observers, null, "localhost", "xxxx");

        Assert.assertFalse("Observer list is empty", observers.isEmpty());
        Assert.assertTrue(observers.get(0) instanceof StatsdMetricObserver);
    }

    @Test
    public void both_graphite_and_statsd_minimal() {
        List<MetricObserver> observers = new ArrayList<>();

        ServoGraphiteSetup.registerGraphiteMetricObserver(observers, null, "localhost", null);
        ServoGraphiteSetup.registerStatsdMetricObserver(observers, null, "localhost", null);

        Assert.assertFalse("Observer list is empty", observers.isEmpty());
        Assert.assertTrue(observers.get(0) instanceof GraphiteMetricObserver);
        Assert.assertTrue(observers.get(1) instanceof StatsdMetricObserver);
    }
}
