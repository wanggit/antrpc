package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.spring.IOnFailHolder;
import io.github.wanggit.antrpc.client.zk.lb.ILoadBalancer;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.zknode.INodeHostContainer;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.breaker.ICircuitBreaker;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.utils.LongToDateUtil;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbsRemoteInterfacesCmd extends AbsCmd {
    AbsRemoteInterfacesCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        String pattern = null;
        if (arguments.length > 0) {
            pattern = arguments[0];
        }
        INodeHostContainer nodeHostContainer = getAntrpcContext().getNodeHostContainer();
        Map<String, List<NodeHostEntity>> entities =
                new TreeMap<>(nodeHostContainer.entitiesSnapshot());
        Map<String, ILoadBalancer<NodeHostEntity>> balancers =
                nodeHostContainer.loadBalancersSnapshot();
        IOnFailHolder onFailHolder = getAntrpcContext().getOnFailHolder();
        Map<String, String> onFailSnapshot = onFailHolder.snapshot();
        ICircuitBreaker circuitBreaker = getAntrpcContext().getCircuitBreaker();
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, List<NodeHostEntity>> entry : entities.entrySet()) {
            String key = entry.getKey();
            if (null != pattern && !key.contains(pattern)) {
                continue;
            }
            String className = key.split("#")[0];
            String methodName = key.split("#")[1];
            if (!checkCondition(className, methodName)) {
                continue;
            }
            List<NodeHostEntity> value = entry.getValue();
            StringBuilder builder = new StringBuilder();
            builder.append(key).append("\r\n");
            value.forEach(
                    hostEntity -> {
                        Map<String, RegisterBean.RegisterBeanMethod> methodMap =
                                hostEntity.getMethodMap();
                        RegisterBean.RegisterBeanMethod registerBeanMethod =
                                methodMap.get(methodName);
                        builder.append("\t")
                                .append(hostEntity.getHostInfo())
                                .append(" registerTime=")
                                .append(LongToDateUtil.toDateStr(hostEntity.getRegisterTs()))
                                .append(" lastRefreshTime=")
                                .append(LongToDateUtil.toDateStr(hostEntity.getRefreshTs()))
                                .append(
                                        "\r\n\t"
                                                + registerBeanMethod.getLimit()
                                                + " calls in "
                                                + registerBeanMethod.getDurationInSeconds()
                                                + " seconds.")
                                .append("\r\n");
                    });
            // 方法的负载均衡
            ILoadBalancer<NodeHostEntity> loadBalancer = balancers.get(key);
            if (null != loadBalancer) {
                builder.append("\t")
                        .append("loadBalancer=")
                        .append(loadBalancer.getClass().getName())
                        .append("\r\n");
            }
            // 失败回调配置
            String onFailClassName = onFailSnapshot.get(className);
            if (null != onFailClassName) {
                builder.append("\t")
                        .append("onFailHandler=")
                        .append(onFailClassName)
                        .append("\r\n");
            }
            // 熔断器配置
            CircuitBreakerConfig breakerConfig =
                    circuitBreaker.getInterfaceCircuitBreaker(className);
            if (null != breakerConfig) {
                builder.append("\t")
                        .append(
                                breakerConfig.getThreshold()
                                        + " failures in "
                                        + breakerConfig.getCheckIntervalSeconds()
                                        + " seconds will turn on the Circuit Breaker.\r\n");
            }
            result.append(builder.toString()).append("\r\n");
        }
        entities.clear();
        balancers.clear();
        onFailSnapshot.clear();
        return result.toString();
    }

    abstract boolean checkCondition(String className, String methodName);
}
