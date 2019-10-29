package io.github.wanggit.antrpc.client.monitor;

import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.*;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.metrics.AbstractCircuitBreakerMetricsSender;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.SortedMap;

public class MonitorMetricsSender extends AbstractCircuitBreakerMetricsSender {

    private String appName;
    private KafkaTemplate kafkaTemplate;

    public MonitorMetricsSender(String appName, KafkaTemplate kafkaTemplate) {
        this.appName = appName;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void internalSend(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        MetricsObjs metricsObjs =
                new MetricsObjs(
                        appName,
                        System.currentTimeMillis(),
                        gauges,
                        counters,
                        histograms,
                        meters,
                        timers);
        kafkaTemplate.send(
                ConstantValues.METRICS_KAFKA_TOPIC, JSONObject.toJSONString(metricsObjs));
    }
}
