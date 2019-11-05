package io.github.wanggit.antrpc.monitor.kafka;

import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.searchbox.client.JestClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaListener {

    @Autowired private JestClient jestClient;

    private static final String CALL_LOGS_INDEX_NAME = "call_logs";
    private static final String CALL_LOGS_TYPE = "_doc";

    @org.springframework.kafka.annotation.KafkaListener(
            topics = {ConstantValues.CALL_LOG_KAFKA_TOPIC})
    public void listen(ConsumerRecord<String, String> record) {
        if (null != record) {
            return;
        }
        if (ConstantValues.CALL_LOG_KAFKA_TOPIC.equals(record.topic())) {}
    }
}
