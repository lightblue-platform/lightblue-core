package com.redhat.lightblue.util.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public interface RequestMetric {
    Timer requestTimer(MetricRegistry registry);

    Counter activeRequestCounter(MetricRegistry registry);

    Meter errorMeter(MetricRegistry registry, String errorTypeOrCode);
}
