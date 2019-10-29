package io.github.wanggit.antrpc.client.monitor;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.client.RpcClient;
import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.bean.*;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.config.RpcCallLogHolderConfig;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.utils.MonitorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcCallLogHolder implements IRpcCallLogHolder {

    private ScheduledThreadPoolExecutor executor = null;
    private int maxElementsPreBatch;
    private int maxElements;
    private LinkedBlockingQueue<RpcCallLog> rpcCallLogs;
    private EventCountCircuitBreaker breaker;

    private boolean hasMonitor = false;
    private boolean enableReport;
    private final MonitorUtil monitorUtil;
    private final RpcCallLogHolderConfig rpcCallLogHolderConfig;
    private final IRpcClients rpcClients;
    private final ISerializerHolder serializerHolder;

    public RpcCallLogHolder(
            IConfiguration configuration,
            IRpcClients rpcClients,
            ISerializerHolder serializerHolder) {
        this.monitorUtil = new MonitorUtil(configuration);
        this.rpcClients = rpcClients;
        this.serializerHolder = serializerHolder;
        this.rpcCallLogHolderConfig = configuration.getRpcCallLogHolderConfig();
        init();
    }

    private void init() {
        hasMonitor = monitorUtil.hasMonitor();
        if (hasMonitor) {
            this.enableReport = rpcCallLogHolderConfig.isEnableReport();
            this.maxElementsPreBatch = rpcCallLogHolderConfig.getReportToMonitorPerBatch();
            this.maxElements = rpcCallLogHolderConfig.getMaxLogBlockingQueueCapacity();
            this.rpcCallLogs = new LinkedBlockingQueue<>(this.maxElements);
            this.breaker =
                    new EventCountCircuitBreaker(
                            rpcCallLogHolderConfig.getMonitorCircuitBreakerThreshold(),
                            rpcCallLogHolderConfig
                                    .getMonitorCircuitBreakerThresholdCheckIntervalMinutes(),
                            TimeUnit.MINUTES);
            executor = new ScheduledThreadPoolExecutor(1, new RpcCallLogDiscardPolicy());
            executor.scheduleAtFixedRate(
                    this::reportToMonitor,
                    rpcCallLogHolderConfig.getFirstReportDelayMs(),
                    rpcCallLogHolderConfig.getReportPeriodMs(),
                    TimeUnit.MILLISECONDS);
        }
    }

    private void reportToMonitor() {
        List<RpcCallLog> logs = new LinkedList<>();
        for (int i = 0; i < maxElementsPreBatch; i++) {
            RpcCallLog element = rpcCallLogs.poll();
            if (null == element) {
                break;
            }
            logs.add(element);
        }
        if (logs.isEmpty()) {
            return;
        }
        if (breaker.checkState()) {
            try {
                internalReportToMonitor(logs);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(
                            "An exception occurred when the log was reported to the monitoring server, and part of the log will be lost.",
                            e.getMessage());
                }
                breaker.incrementAndCheckState();
            }
        } else {
            if (log.isErrorEnabled()) {
                log.error(
                        "An exception occurred when the log was reported to the monitoring server. "
                                + "If there are "
                                + rpcCallLogHolderConfig.getMonitorCircuitBreakerThreshold()
                                + " exceptions within "
                                + rpcCallLogHolderConfig
                                        .getMonitorCircuitBreakerThresholdCheckIntervalMinutes()
                                + " minutes, the service has been circuit breaked");
            }
        }
        logs.clear();
    }

    private void internalReportToMonitor(List<RpcCallLog> logs) {
        RpcClient rpcClient = rpcClients.getRpcClient(monitorUtil.getMonitorHost());
        RpcRequestBean requestBean = new RpcRequestBean();
        requestBean.setOneway(true);
        SerialNumberThreadLocal.TraceEntity traceEntity = SerialNumberThreadLocal.get();
        requestBean.setSerialNumber(traceEntity.getSerialNumber());
        requestBean.setCaller(traceEntity.getCaller());
        requestBean.setTs(System.currentTimeMillis());
        requestBean.setId(IdGenHelper.getInstance().getUUID());
        requestBean.setFullClassName(RpcMonitorApi.class.getName());
        requestBean.setMethodName("report");
        requestBean.setArgumentTypes(Lists.newArrayList("java.util.List"));
        requestBean.setArgumentValues(new Object[] {logs});
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setCmdId(IdGenHelper.getInstance().getId());
        rpcProtocol.setType(ConstantValues.BIZ_TYPE);
        rpcProtocol.setData(serializerHolder.getSerializer().serialize(requestBean));
        rpcClient.oneway(rpcProtocol);
    }

    @Override
    public void log(RpcCallLog rpcCallLog) {
        try {
            if (hasMonitor && enableReport) {
                if (rpcCallLogHolderConfig.isReportArgumentValues()) {
                    if (null != rpcCallLog.getRequestArgs()
                            && rpcCallLog.getRequestArgs().length > 0) {
                        rpcCallLog.setArgumentsJson(
                                JSONObject.toJSONString(rpcCallLog.getRequestArgs()));
                    }
                }
                rpcCallLog.setRequestArgs(null);
                boolean offered = rpcCallLogs.offer(rpcCallLog);
                if (!offered) {
                    if (log.isWarnEnabled()) {
                        log.warn(
                                "The queue is full [capacity = "
                                        + this.maxElements
                                        + "] and objects have been discarded. Check that Monitor is alive.");
                    }
                }
            }
        } catch (Throwable throwable) {
            if (log.isErrorEnabled()) {
                log.error("An exception occurred while logging the call.", throwable);
            }
        }
    }

    @Slf4j
    static class RpcCallLogDiscardPolicy extends ThreadPoolExecutor.DiscardPolicy {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "The RPC call log handler is discarding logs. Check the "
                                + RpcCallLogHolder.class.getName()
                                + " threads are sufficient.");
            }
        }
    }
}
