package io.github.wanggit.antrpc.commons.config;

import io.github.wanggit.antrpc.client.monitor.report.KafkaLogReporter;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.Data;

@Data
public class CallLogReporterConfig {

    /** 是否开启日志上报 */
    private boolean enableReport = false;

    /** 默认发送到Kafka的消息Topic */
    private String kafkaTopic = ConstantValues.CALL_LOG_KAFKA_TOPIC;

    /** 是否在日志中携带请求参数 */
    private boolean reportArgumentValues = false;

    /** 默认使用的上报日志实现 */
    private String type = KafkaLogReporter.class.getName();
}
