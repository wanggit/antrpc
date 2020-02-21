package io.github.wanggit.antrpc.client.zk.lb;

import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoadBalancerHelper {

    private final Class<? extends ILoadBalancer> loadBalancerClass;

    public LoadBalancerHelper(IConfiguration configuration) {
        this.loadBalancerClass = configuration.getLoadBalancerName();
    }

    public ILoadBalancer getLoadBalancer() {
        return createNewInstance();
    }

    private ILoadBalancer createNewInstance() {
        try {
            return loadBalancerClass.newInstance();
        } catch (Exception e) {
            String message =
                    "Failed to instantiate ILoadBalancer, heartBeatWasContinuousLoss the "
                            + Constants.RPC_LOAD_BALANCER_PROP_NAME
                            + " configuration. The value of the current configuration is "
                            + loadBalancerClass.getName();
            if (log.isWarnEnabled()) {
                log.error(message, e);
            }
            throw new RuntimeException(message, e);
        }
    }
}
