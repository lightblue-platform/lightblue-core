package com.redhat.lightblue.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Utility class for tracking memory usage for given type (assuming it's an immutable value).
 * You can set thresholds and have it execute callbacks when they are exceeded. See {@link MemoryMonitorTest} for usage.
 *
 * @author mpatercz
 *
 * @param <T>
 */
public class MemoryMonitor<T> {

    @FunctionalInterface
    public static interface SizeCalculator<T> {
        public int size(T obj);
    }

    @FunctionalInterface
    public static interface ThresholdExceededCallback<T> {
        public void fire(int currentSizeB, int thresholdB, T obj) throws RuntimeException;
    }

    public static class ThresholdMonitor<T> {

        final int thresholdB;

        boolean fired = false;

        final ThresholdExceededCallback<T> callback;

        public ThresholdMonitor(int thresholdB, ThresholdExceededCallback<T> callback) {
            super();
            this.thresholdB = thresholdB;
            this.callback = callback;
        }

    }

    private int dataSizeB = 0;

    private SizeCalculator<T> sizeCalculator;

    private List<ThresholdMonitor<T>> monitors = new ArrayList<>();
    private Set<T> counted = Collections.newSetFromMap(new WeakHashMap<>());

    /**
     * Adds a monitor with a callback which fires when specified threshold is exceeded. The callback will fire only once.
     *
     * Monitors/callbacks are processed in order they were registered. If a callback throws a RuntimeException, processing stops.
     *
     * @param m
     */
    public void registerMonitor(ThresholdMonitor<T> m) {
        if (m.thresholdB > 0) {
            this.monitors.add(m);
        }
    }

    private void checkThresholdMonitors(T obj) throws RuntimeException {
        for (ThresholdMonitor<T> m: monitors) {
            if (dataSizeB > m.thresholdB && !m.fired) {
                m.fired = true;
                m.callback.fire(dataSizeB, m.thresholdB, obj);
            }
        }
    }

    public MemoryMonitor(SizeCalculator<T> sizeCalculator, int intialDataSizeB) {
        super();
        this.sizeCalculator = sizeCalculator;
        this.dataSizeB = intialDataSizeB;
    }

    public MemoryMonitor(SizeCalculator<T> sizeCalculator) {
        this(sizeCalculator, 0);
    }

    /**
     * Add this value's size to the total, unless this object (by reference) has already been
     * counted.
     *
     * @param value
     * @return
     */
    public T apply(final T value) {
        if (counted.add(value)) {
            dataSizeB += sizeCalculator.size(value);

            checkThresholdMonitors(value);
        }

        return value;
    }

    public int getDataSizeB() {
        return dataSizeB;
    }
}
