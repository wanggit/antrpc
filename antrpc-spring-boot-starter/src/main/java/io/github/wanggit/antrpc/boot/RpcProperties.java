package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.client.zk.lb.RoundLoadBalancer;
import io.github.wanggit.antrpc.commons.config.*;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = ConstantValues.ANTRPC_CONFIG_PREFIX)
class RpcProperties {

    private String zkServers = "localhost:2181";

    private Integer port = 6060;

    private String loadBalancer = RoundLoadBalancer.class.getName();

    private boolean startServer = true;

    private String exposedIp;

    /** global circuit breaker */
    @NestedConfigurationProperty private CircuitBreakerConfig circuitBreakers;

    /** connection circuit breaker */
    @NestedConfigurationProperty private CircuitBreakerConfig connectionBreakerConfig;

    @NestedConfigurationProperty private CallLogReporterConfig callLogReporterConfig;

    @NestedConfigurationProperty private RpcClientsConfig rpcClientsConfig;

    @NestedConfigurationProperty private CodecConfig codecConfig;

    @NestedConfigurationProperty private TelnetConfig telnetConfig;

    @NestedConfigurationProperty private SerializeConfig serializeConfig;

    /** direct rpc host -> ip:port */
    private Map<String, String> directHosts;

    private Map<String, CircuitBreakerConfig> interfaceCircuitBreaker;
}
