package io.github.wanggit.antrpc.commons.config;

import io.github.wanggit.antrpc.client.zk.zknode.DirectNodeHostEntity;
import org.springframework.core.env.Environment;

import java.util.Map;

public interface IConfiguration {

    SerializeConfig getSerializeConfig();

    CodecConfig getCodecConfig();

    RpcClientsConfig getRpcClientsConfig();

    CallLogReporterConfig getCallLogReporterConfig();

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
