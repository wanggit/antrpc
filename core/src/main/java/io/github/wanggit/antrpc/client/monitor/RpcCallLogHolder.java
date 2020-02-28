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

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcCallLogHolder implements IRpcCallLogHolder {

    private final ILogReporter logReporter;

    private final ThreadPoolExecutor threadPoolExecutor;

    private final Map<String, List<IRpcCallLogListener>> listeners = new HashMap<>();

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
        threadPoolExecutor =
                new ThreadPoolExecutor(
                        3,
                        6,
                        500,
                        TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(10),
                        new RejectedExecutionHandler() {
                            @Override
                            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                if (log.isWarnEnabled()) {
                                    log.warn(
                                            "The log queue is full and some logs will be discarded.");
                                }
                            }
                        });
    }

    @Override
    public void log(RpcCallLog rpcCallLog) {
        if (null != logReporter.getConfiguration()) {
            rpcCallLog.setAppName(logReporter.getConfiguration().getApplicationName());
        }
        rpcCallLog.setDate(new Date().toInstant().toString());
        if (log.isDebugEnabled()) {
            log.debug(JSONObject.toJSONString(rpcCallLog));
        }
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        fireListener(rpcCallLog);
                        logReporter.report(rpcCallLog);
                    }
                });
    }

    private void fireListener(RpcCallLog rpcCallLog) {
        String key = rpcCallLog.getClassName() + "#" + rpcCallLog.getMethodName();
        List<IRpcCallLogListener> iRpcCallLogListeners = listeners.get(key);
        if (null != iRpcCallLogListeners) {
            for (IRpcCallLogListener rpcCallLogListener : iRpcCallLogListeners) {
                rpcCallLogListener.listen(rpcCallLog);
            }
        }
    }

    @Override
    public void addListener(String name, IRpcCallLogListener rpcCallLogListener) {
        if (!listeners.containsKey(name)) {
            listeners.put(name, new ArrayList<>());
        }
        listeners.get(name).add(rpcCallLogListener);
    }

    @Override
    public void removeListener(String name, IRpcCallLogListener rpcCallLogListener) {
        List<IRpcCallLogListener> iRpcCallLogListeners = listeners.get(name);
        if (null != iRpcCallLogListeners) {
            iRpcCallLogListeners.remove(rpcCallLogListener);
        }
    }
}
