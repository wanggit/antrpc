package antrpc.monitor.web;

import antrpc.client.monitor.RpcMonitorApi;
import antrpc.commons.annotations.RpcService;
import antrpc.commons.bean.RpcCallLog;
import antrpc.monitor.domain.Jvm;
import antrpc.monitor.service.CallLogsService;
import antrpc.monitor.service.JvmService;
import antrpc.monitor.web.vm.MetricsVM;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RpcService
public class RpcMonitor implements RpcMonitorApi {

    @Autowired private CallLogsService callLogsService;

    @Autowired private JvmService jvmService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Override
    public void report(List<RpcCallLog> rpcCallLogs) {
        executorService.submit(() -> callLogsService.save(rpcCallLogs));
    }

    @Override
    public void sendMetrics(String metricsJson) {
        executorService.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        MetricsVM metricsVM = JSONObject.parseObject(metricsJson, MetricsVM.class);
                        SortedMap<String, MetricsVM.GaugeValue> gauges = metricsVM.getGauges();
                        List<Jvm> jvms = new ArrayList<>(gauges.size() * 2);
                        gauges.forEach(
                                (key, value) -> {
                                    Jvm jvm = new Jvm();
                                    jvm.setAttrName(key);
                                    jvm.setAttrValue(String.valueOf(value.getValue()));
                                    jvm.setTs(metricsVM.getTs());
                                    jvm.setAppName(metricsVM.getAppName());
                                    jvms.add(jvm);
                                });
                        jvmService.save(jvms);
                    }
                });
    }
}
