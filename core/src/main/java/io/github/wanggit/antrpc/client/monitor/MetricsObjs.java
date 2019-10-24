package io.github.wanggit.antrpc.client.monitor;

import com.codahale.metrics.*;

import java.io.Serializable;
import java.util.SortedMap;

public class MetricsObjs implements Serializable {

    private static final long serialVersionUID = -7538046637142937833L;
    private String appName;
    private Long ts;
    private SortedMap<String, Gauge> gauges;
    private SortedMap<String, Counter> counters;
    private SortedMap<String, Histogram> histograms;
    private SortedMap<String, Meter> meters;
    private SortedMap<String, Timer> timers;

    MetricsObjs(
            String appName,
            Long ts,
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        this.appName = appName;
        this.ts = ts;
        this.gauges = gauges;
        this.counters = counters;
        this.histograms = histograms;
        this.meters = meters;
        this.timers = timers;
    }

    public MetricsObjs() {}

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public SortedMap<String, Gauge> getGauges() {
        return gauges;
    }

    public void setGauges(SortedMap<String, Gauge> gauges) {
        this.gauges = gauges;
    }

    public SortedMap<String, Counter> getCounters() {
        return counters;
    }

    public void setCounters(SortedMap<String, Counter> counters) {
        this.counters = counters;
    }

    public SortedMap<String, Histogram> getHistograms() {
        return histograms;
    }

    public void setHistograms(SortedMap<String, Histogram> histograms) {
        this.histograms = histograms;
    }

    public SortedMap<String, Meter> getMeters() {
        return meters;
    }

    public void setMeters(SortedMap<String, Meter> meters) {
        this.meters = meters;
    }

    public SortedMap<String, Timer> getTimers() {
        return timers;
    }

    public void setTimers(SortedMap<String, Timer> timers) {
        this.timers = timers;
    }
}
