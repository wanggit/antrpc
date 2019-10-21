package antrpc.commons.config;

import antrpc.client.zk.zknode.DirectNodeHostEntity;
import org.springframework.core.env.Environment;

import java.util.Map;

public interface IConfiguration {
    RpcClientsConfig getRpcClientsConfig();

    RpcCallLogHolderConfig getRpcCallLogHolderConfig();

    Environment getEnvironment();

    MetricsConfig getMetricsConfig();

    Map<String, DirectNodeHostEntity> getDirectHosts();

    boolean isStartServer();

    Integer getPort();

    String getZkIps();

    Integer getZkConnectRetryBaseSleepMs();

    Integer getZkConnectMaxRetries();

    Integer getZkConnectRetryMaxSleepMs();

    Integer getZkConnectionTimeoutMs();

    String getZkRootNodeName();

    Class getLoadBalancerName();

    String getMonitorHosts();

    CircuitBreakerConfig getGlobalBreakerConfig();

    Map<String, CircuitBreakerConfig> getInterfaceBreakerConfigs();
}
