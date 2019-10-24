package io.github.wanggit.antrpc.commons.metrics;

import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.config.MetricsConfig;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jmx.JmxReporter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.concurrent.TimeUnit;

@Slf4j
abstract class AbstractMetrics {

    MetricsConfig metricsConfig;
    MetricRegistry metricRegistry;
    private IConfiguration configuration;
    private IMetricsSender metricsSender;
    private IRpcClients rpcClients;

    AbstractMetrics(
            MetricsConfig metricsConfig,
            MetricRegistry metricRegistry,
            IConfiguration configuration,
            IMetricsSender metricsSender,
            IRpcClients rpcClients) {
        this.metricsConfig = null == metricsConfig ? new MetricsConfig() : metricsConfig;
        this.metricRegistry = metricRegistry;
        this.configuration = configuration;
        this.metricsSender = metricsSender;
        this.rpcClients = rpcClients;
    }

    public void init() {
        internalInit();
        initReporter();
    }

    abstract void internalInit();

    abstract long getReportIntervalSeconds();

    private void initReporter() {
        if (metricsConfig.isEnableLoggerReporter()) {
            if (log.isInfoEnabled()) {
                log.info("Initializing Metrics Log reporting");
            }
            Marker metricsMarker = MarkerFactory.getMarker("metrics");
            Slf4jReporter reporter =
                    Slf4jReporter.forRegistry(metricRegistry)
                            .outputTo(LoggerFactory.getLogger("metrics"))
                            .markWith(metricsMarker)
                            .convertRatesTo(TimeUnit.SECONDS)
                            .convertDurationsTo(TimeUnit.MILLISECONDS)
                            .build();
            reporter.start(getReportIntervalSeconds(), TimeUnit.SECONDS);
        }
        if (metricsConfig.isEnableConsoleReporter()) {
            if (log.isInfoEnabled()) {
                log.info("Initializing Metrics Console reporting");
            }
            ConsoleReporter reporter =
                    ConsoleReporter.forRegistry(metricRegistry)
                            .convertRatesTo(TimeUnit.SECONDS)
                            .convertDurationsTo(TimeUnit.SECONDS)
                            .build();
            reporter.start(getReportIntervalSeconds(), TimeUnit.SECONDS);
        }
        if (metricsConfig.isEnableJmxReporter()) {
            if (log.isInfoEnabled()) {
                log.info("Initializing Metrics Jmx reporting");
            }
            JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
            jmxReporter.start();
        }
        if (metricsConfig.isEnableAntrpcMonitorReporter()) {
            if (log.isInfoEnabled()) {
                log.info("Initializing Metrics Antrpc Monitor reporting");
            }
            AntrpcMonitorReporter reporter =
                    AntrpcMonitorReporter.forRegistry(metricRegistry, configuration, metricsSender)
                            .convertRatesTo(TimeUnit.SECONDS)
                            .convertDurationsTo(TimeUnit.SECONDS)
                            .build();
            reporter.start(getReportIntervalSeconds(), TimeUnit.SECONDS);
        }
    }
}
