package io.github.wanggit.antrpc.commons.metrics;

import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.config.MetricsConfig;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

@Slf4j
public class JvmMetrics extends AbstractMetrics {

    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";
    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";
    private static final String PROP_METRIC_REG_JVM_ATTRIBUTE_SET = "jvm.attributes";

    public JvmMetrics(
            MetricsConfig metricsConfig,
            MetricRegistry metricRegistry,
            IConfiguration configuration,
            IMetricsSender metricsSender,
            IRpcClients rpcClients) {
        super(metricsConfig, metricRegistry, configuration, metricsSender, rpcClients);
    }

    @Override
    void internalInit() {
        if (log.isInfoEnabled()) {
            log.info("Registering JVM gauges");
        }
        metricRegistry.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        metricRegistry.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
        metricRegistry.register(
                PROP_METRIC_REG_JVM_BUFFERS,
                new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
        metricRegistry.register(PROP_METRIC_REG_JVM_ATTRIBUTE_SET, new JvmAttributeGaugeSet());
    }

    @Override
    long getReportIntervalSeconds() {
        return metricsConfig.getReportIntervalSeconds();
    }
}
