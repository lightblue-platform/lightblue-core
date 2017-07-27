package com.redhat.lightblue.util;

import java.util.ArrayList;
import java.util.List;

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

        final ThresholdExceededCallback<T> event;

        public ThresholdMonitor(int thresholdB, ThresholdExceededCallback<T> event) {
            super();
            this.thresholdB = thresholdB;
            this.event = event;
        }

    }

    private int dataSizeB = 0;

    private SizeCalculator<T> sizeCalculator;

    private List<ThresholdMonitor<T>> monitors = new ArrayList<>();

    public void registerMonitor(ThresholdMonitor<T> m) {
        if (m.thresholdB > 0) {
            this.monitors.add(m);
        }
    }

    private void checkThresholdMonitors(T obj) throws RuntimeException {
        for (ThresholdMonitor<T> m: monitors) {
            if (dataSizeB > m.thresholdB && !m.fired) {
                m.fired = true;
                m.event.fire(dataSizeB, m.thresholdB, obj);
            }
        }
    }

    public MemoryMonitor(SizeCalculator<T> sizeCalculator) {
        super();
        this.sizeCalculator = sizeCalculator;
    }

    /**
     * Add this value's size to the total.
     *
     * @param value
     * @return
     */
    public T apply(final T value) {
        dataSizeB += sizeCalculator.size(value);

        checkThresholdMonitors(value);

        return value;
    }

    public int getDataSizeB() {
        return dataSizeB;
    }
}
