package io.github.wanggit.antrpc.client.monitor.report;

import org.springframework.kafka.core.KafkaTemplate;

public interface IKafkaLogReporter {

    void setKafkaTemplate(KafkaTemplate kafkaTemplate);
}
