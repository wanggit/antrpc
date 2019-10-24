package io.github.wanggit.antrpc.commons.metrics;

import com.codahale.metrics.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractCircuitBreakerMetricsSender implements IMetricsSender {

    private final EventCountCircuitBreaker breaker;

    private AbstractCircuitBreakerMetricsSender(
            int threshold, long checkInterval, TimeUnit timeUnit) {
        this.breaker = new EventCountCircuitBreaker(threshold, checkInterval, timeUnit);
    }

    public AbstractCircuitBreakerMetricsSender() {
        this(5, 1, TimeUnit.MINUTES);
    }

    @Override
    public void send(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        if (breaker.checkState()) {
            try {
                internalSend(gauges, counters, histograms, meters, timers);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(
                            "An exception occurred when metrics information was sent to Monitor.",
                            e);
                }
                breaker.incrementAndCheckState();
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error(
                        "An exception occurred when metrics information was sent to Monitor. The Circuit Breaker is open.");
            }
        }
    }

    protected abstract void internalSend(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers);
}
