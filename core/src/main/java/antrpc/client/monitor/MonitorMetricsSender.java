package antrpc.client.monitor;

import antrpc.client.RpcClient;
import antrpc.commons.IRpcClients;
import antrpc.commons.bean.IdGenHelper;
import antrpc.commons.bean.RpcProtocol;
import antrpc.commons.bean.RpcRequestBean;
import antrpc.commons.bean.SerialNumberThreadLocal;
import antrpc.commons.codec.kryo.KryoSerializer;
import antrpc.commons.constants.ConstantValues;
import antrpc.commons.metrics.AbstractCircuitBreakerMetricsSender;
import antrpc.commons.utils.MonitorUtil;
import com.alibaba.fastjson.JSONObject;
import com.codahale.metrics.*;
import com.google.common.collect.Lists;

import java.util.SortedMap;

public class MonitorMetricsSender extends AbstractCircuitBreakerMetricsSender {

    private MonitorUtil monitorUtil;
    private String appName;
    private IRpcClients rpcClients;

    public MonitorMetricsSender(String appName, IRpcClients rpcClients) {
        this.appName = appName;
        this.rpcClients = rpcClients;
    }

    @Override
    protected void internalSend(
            SortedMap<String, Gauge> gauges,
            SortedMap<String, Counter> counters,
            SortedMap<String, Histogram> histograms,
            SortedMap<String, Meter> meters,
            SortedMap<String, Timer> timers) {
        RpcClient rpcClient = rpcClients.getRpcClient(monitorUtil.getMonitorHost());
        RpcRequestBean requestBean = new RpcRequestBean();
        requestBean.setOneway(true);
        SerialNumberThreadLocal.TraceEntity traceEntity = SerialNumberThreadLocal.get();
        requestBean.setSerialNumber(traceEntity.getSerialNumber());
        requestBean.setCaller(traceEntity.getCaller());
        requestBean.setTs(System.currentTimeMillis());
        requestBean.setId(IdGenHelper.getInstance().getUUID());
        requestBean.setFullClassName(RpcMonitorApi.class.getName());
        requestBean.setMethodName("sendMetrics");
        requestBean.setArgumentTypes(Lists.newArrayList("java.lang.String"));
        MetricsObjs metricsObjs =
                new MetricsObjs(
                        appName,
                        System.currentTimeMillis(),
                        gauges,
                        counters,
                        histograms,
                        meters,
                        timers);
        requestBean.setArgumentValues(new Object[] {JSONObject.toJSONString(metricsObjs)});
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setCmdId(IdGenHelper.getInstance().getId());
        rpcProtocol.setType(ConstantValues.BIZ_TYPE);
        rpcProtocol.setData(KryoSerializer.getInstance().serialize(requestBean));
        rpcClient.oneway(rpcProtocol);
    }

    @Override
    public void setMonitorUtil(MonitorUtil monitorUtil) {
        this.monitorUtil = monitorUtil;
    }
}
