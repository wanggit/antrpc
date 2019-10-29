package io.github.wanggit.antrpc.commons.metrics;

import com.codahale.metrics.*;

import java.util.SortedMap;

public interface IMetricsSender {

    void send(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers);
}
