package io.github.wanggit.antrpc.client.zk.lb;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadBalancerHelper {

    private final ILoadBalancer loadBalancer;

    public LoadBalancerHelper(IConfiguration configuration) {
        try {
            loadBalancer = (ILoadBalancer) configuration.getLoadBalancerName().newInstance();
        } catch (Exception e) {
            String message =
                    "Failed to instantiate ILoadBalancer, heartBeatWasContinuousLoss the "
                            + Constants.RPC_LOAD_BALANCER_PROP_NAME
                            + " configuration. The value of the current configuration is "
                            + configuration.getLoadBalancerName().getName();
            if (log.isWarnEnabled()) {
                log.error(message, e);
            }
            throw new RuntimeException(message, e);
        }
    }

    public <T extends Host> ILoadBalancer<T> getLoadBalancer(Class<T> elementType) {
        return loadBalancer;
    }
}
