package io.github.wanggit.antrpc.client.monitor;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;

import java.util.List;

public interface RpcMonitorApi {

    void report(List<RpcCallLog> rpcCallLogs);

    void sendMetrics(String metricsJson);
}
