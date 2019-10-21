package antrpc.client.monitor;

import antrpc.commons.bean.RpcCallLog;

import java.util.List;

public interface RpcMonitorApi {

    void report(List<RpcCallLog> rpcCallLogs);

    void sendMetrics(String metricsJson);
}
