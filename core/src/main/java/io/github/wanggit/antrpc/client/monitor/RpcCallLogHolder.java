package io.github.wanggit.antrpc.client.monitor;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.client.monitor.report.IKafkaLogReporter;
import io.github.wanggit.antrpc.client.monitor.report.ILogReporter;
import io.github.wanggit.antrpc.client.monitor.report.NoOpLogReporter;
import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.commons.config.CallLogReporterConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class RpcCallLogHolder implements IRpcCallLogHolder {

    private final ILogReporter logReporter;

    public RpcCallLogHolder(IConfiguration configuration, ApplicationContext applicationContext)
            throws Exception {
        CallLogReporterConfig logHolderConfig = configuration.getCallLogReporterConfig();
        if (logHolderConfig.isEnableReport()) {
            logReporter = (ILogReporter) Class.forName(logHolderConfig.getType()).newInstance();
        } else {
            logReporter = new NoOpLogReporter();
        }
        logReporter.setConfiguration(configuration);
        if (logReporter instanceof IKafkaLogReporter) {
            ((IKafkaLogReporter) logReporter)
                    .setKafkaTemplate(applicationContext.getBean(KafkaTemplate.class));
        }
    }

    @Override
    public void log(RpcCallLog rpcCallLog) {
        if (log.isInfoEnabled()) {
            log.info(JSONObject.toJSONString(rpcCallLog));
        }
        if (null != logReporter.getConfiguration()) {
            rpcCallLog.setAppName(logReporter.getConfiguration().getApplicationName());
        }
        logReporter.report(rpcCallLog);
    }
}
