package io.github.wanggit.antrpc.commons.config;

import lombok.Data;

@Data
public class RpcCallLogHolderConfig {

    /** 是否开始日志上报 */
    private boolean enableReport = true;

    /** 每次上报到Monitor的日志条数 */
    private Integer reportToMonitorPerBatch = 500;

    /** 应用最多暂存多少条日志 */
    private Integer maxLogBlockingQueueCapacity = reportToMonitorPerBatch * 10000;

    /** 上报日志到Monitor的熔断器 threshold */
    private Integer monitorCircuitBreakerThreshold = 20;

    /** 上报日志到Monitor的熔断器 checkInterval */
    private Integer monitorCircuitBreakerThresholdCheckIntervalMinutes = 1;

    /** 首次上报延时毫秒数 */
    private Integer firstReportDelayMs = 50;

    /** 每多少毫秒上报一次 */
    private Integer reportPeriodMs = 200;

    /** 是否在日志中携带请求参数 */
    private boolean reportArgumentValues = false;
}
