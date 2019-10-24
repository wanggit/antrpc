package io.github.wanggit.antrpc.commons.utils;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.client.zk.lb.ILoadBalancer;
import io.github.wanggit.antrpc.client.zk.lb.RoundLoadBalancer;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class MonitorUtil {

    private List<Host> hosts = null;
    private final ILoadBalancer<Host> loadBalancer = new RoundLoadBalancer<>();
    private IConfiguration configuration;

    public MonitorUtil(IConfiguration configuration) {
        this.configuration = configuration;
    }

    public boolean hasMonitor() {
        String monitorHost = configuration.getMonitorHosts();
        return null != monitorHost;
    }

    public Host getMonitorHost() {
        if (null == hosts) {
            synchronized (MonitorUtil.class) {
                if (null == hosts) {
                    internalCreateHosts();
                }
            }
        }
        return loadBalancer.chooseFrom(hosts);
    }

    private void internalCreateHosts() {
        String monitorHost = configuration.getMonitorHosts();
        String[] hostStrs = monitorHost.trim().split(",");
        List<Host> hs = new ArrayList<>(hostStrs.length * 2);
        for (int i = 0; i < hostStrs.length; i++) {
            String[] tmps = hostStrs[i].trim().split(":");
            if (tmps.length != 2) {
                throw new IllegalArgumentException(
                        Constants.RPC_MONITOR_PROP_NAME
                                + " configuration format is incorrect. It must be IP:port");
            }
            String ip = tmps[0];
            Integer port = null;
            try {
                port = Integer.parseInt(tmps[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Port must be integer. " + monitorHost);
            }
            hs.add(new Host(ip, port));
        }
        hosts = new ArrayList<>(hs);
        hs.clear();
    }
}
