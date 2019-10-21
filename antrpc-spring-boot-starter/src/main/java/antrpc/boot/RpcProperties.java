package antrpc.boot;

import antrpc.client.zk.lb.RoundLoadBalancer;
import antrpc.commons.config.CircuitBreakerConfig;
import antrpc.commons.config.MetricsConfig;
import antrpc.commons.config.RpcCallLogHolderConfig;
import antrpc.commons.config.RpcClientsConfig;
import antrpc.commons.constants.ConstantValues;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = ConstantValues.ANTRPC_CONFIG_PREFIX)
public class RpcProperties {

    private String zkServers = "localhost:2181";

    private Integer port = 6060;

    private String loadBalancer = RoundLoadBalancer.class.getName();

    private String monitorHost;

    private boolean startServer = true;

    /** global circuit breaker */
    @NestedConfigurationProperty private CircuitBreakerConfig circuitBreakers;

    @NestedConfigurationProperty private MetricsConfig metricsConfig;

    @NestedConfigurationProperty private RpcCallLogHolderConfig rpcCallLogHolderConfig;

    @NestedConfigurationProperty private RpcClientsConfig rpcClientsConfig;

    /** direct rpc host -> ip:port */
    private Map<String, String> directHosts;

    private Map<String, CircuitBreakerConfig> interfaceCircuitBreaker;
}
