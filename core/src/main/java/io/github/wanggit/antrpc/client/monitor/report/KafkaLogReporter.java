package io.github.wanggit.antrpc.client.monitor.report;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.commons.config.CallLogReporterConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaLogReporter implements IKafkaLogReporter, ILogReporter {

    private KafkaTemplate kafkaTemplate;

    private String topic;

    private boolean reportArgumentValues;

    private IConfiguration configuration;

    @Override
    public void report(RpcCallLog log) {
        if (!reportArgumentValues) {
            log.setArgumentsJson(null);
        }
        if (null != kafkaTemplate) {
            kafkaTemplate.send(topic, JSONObject.toJSONString(log));
        }
    }

    @Override
    public void setKafkaTemplate(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
        CallLogReporterConfig callLogReporterConfig = configuration.getCallLogReporterConfig();
        this.topic = callLogReporterConfig.getKafkaTopic();
        this.reportArgumentValues = callLogReporterConfig.isReportArgumentValues();
    }

    @Override
    public IConfiguration getConfiguration() {
        return configuration;
    }
}
