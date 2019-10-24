package io.github.wanggit.antrpc.commons.metrics;

import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.utils.MonitorUtil;
import com.codahale.metrics.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AntrpcMonitorReporter extends ScheduledReporter {
    /**
     * Returns a new {@link AntrpcMonitorReporter.Builder} for {@link AntrpcMonitorReporter}.
     *
     * @param registry the registry to report
     * @return a {@link AntrpcMonitorReporter.Builder} instance for a {@link AntrpcMonitorReporter}
     */
    static AntrpcMonitorReporter.Builder forRegistry(
            MetricRegistry registry, IConfiguration configuration, IMetricsSender metricsSender) {
        return new AntrpcMonitorReporter.Builder(registry, configuration, metricsSender);
    }

    /**
     * A builder for {@link AntrpcMonitorReporter} instances. Defaults to using the default locale
     * and time zone, writing to {@code System.out}, converting rates to events/second, converting
     * durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private final IConfiguration configuration;
        private final IMetricsSender metricsSender;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;

        private Builder(
                MetricRegistry registry,
                IConfiguration configuration,
                IMetricsSender metricsSender) {
            this.registry = registry;
            this.configuration = configuration;
            this.metricsSender = metricsSender;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            disabledMetricAttributes = Collections.emptySet();
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be stopped with same
         * time with reporter. Default value is true. Setting this parameter to false, has the sense
         * in combining with providing external managed executor via {@link
         * #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with
         *     this reporter
         * @return {@code this}
         */
        public AntrpcMonitorReporter.Builder shutdownExecutorOnStop(
                boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics. Default value is
         * null. Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        AntrpcMonitorReporter.Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        AntrpcMonitorReporter.Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        AntrpcMonitorReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public AntrpcMonitorReporter.Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or
         * "m15"). See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a {@link MetricFilter}
         * @return {@code this}
         */
        public AntrpcMonitorReporter.Builder disabledMetricAttributes(
                Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a {@link AntrpcMonitorReporter} with the given properties.
         *
         * @return a {@link AntrpcMonitorReporter}
         */
        public AntrpcMonitorReporter build() {
            return new AntrpcMonitorReporter(
                    registry,
                    configuration,
                    metricsSender,
                    rateUnit,
                    durationUnit,
                    filter,
                    executor,
                    shutdownExecutorOnStop,
                    disabledMetricAttributes);
        }
    }

    private final MonitorUtil monitorUtil;
    private final IMetricsSender metricsSender;

    private AntrpcMonitorReporter(
            MetricRegistry registry,
            IConfiguration configuration,
            IMetricsSender metricsSender,
            TimeUnit rateUnit,
            TimeUnit durationUnit,
            MetricFilter filter,
            ScheduledExecutorService executor,
            boolean shutdownExecutorOnStop,
            Set<MetricAttribute> disabledMetricAttributes) {
        super(
                registry,
                "antrpc-monitor-reporter",
                filter,
                rateUnit,
                durationUnit,
                executor,
                shutdownExecutorOnStop,
                disabledMetricAttributes);
        this.monitorUtil = new MonitorUtil(configuration);
        this.metricsSender = metricsSender;
        this.metricsSender.setMonitorUtil(this.monitorUtil);
    }

    @Override
    public void report(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        if (!monitorUtil.hasMonitor()) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Antrpc Monitor is not enabled, and can be configure antrpc.monitor-host to enable it.");
            }
            return;
        }
        metricsSender.send(gauges, counters, histograms, meters, timers);
    }
}
