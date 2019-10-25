package io.github.wanggit.antrpc.monitor.web.vm;

import com.codahale.metrics.*;

import java.io.Serializable;
import java.util.SortedMap;

public class MetricsVM implements Serializable {

    private static final long serialVersionUID = 1478458472276103877L;
    private String appName;
    private Long ts;
    private SortedMap<String, GaugeValue> gauges;
    private SortedMap<String, Counter> counters;
    private SortedMap<String, Histogram> histograms;
    private SortedMap<String, Meter> meters;
    private SortedMap<String, Timer> timers;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public SortedMap<String, GaugeValue> getGauges() {
        return gauges;
    }

    public void setGauges(SortedMap<String, GaugeValue> gauges) {
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

    public static class GaugeValue implements Gauge<Serializable>, Serializable {

        private static final long serialVersionUID = 9158580415659212789L;
        private Serializable value;

        @Override
        public Serializable getValue() {
            return this.value;
        }

        public void setValue(Serializable value) {
            this.value = value;
        }
    }
}
