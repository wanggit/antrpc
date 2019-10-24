package io.github.wanggit.antrpc.commons.config;

import io.github.wanggit.antrpc.client.zk.lb.RoundLoadBalancer;
import io.github.wanggit.antrpc.client.zk.zknode.DirectNodeHostEntity;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import org.springframework.core.env.Environment;

import java.util.Map;

public class Configuration implements IConfiguration {

    /** 暴露的RPC端口 */
    private Integer port = 6060;

    /** 是否开启RPC服务端 */
    private boolean startServer = true;

    /** zookeeper服务器ip地址，多个使用逗号间隔 */
    private String zkIps = "localhost:2181";

    /** zookeeper连接断开之后进行重试的基础等待时间毫秒 */
    private Integer zkConnectRetryBaseSleepMs = 1000;

    /** zookeeper连接断开之后重试的最大次数 */
    private Integer zkConnectMaxRetries = 5;

    /** zookeeper连接断开之后进行重试的最大等待时间毫秒 */
    private Integer zkConnectRetryMaxSleepMs = 30000;

    /** zookeeper连接超时时间毫秒 */
    private Integer zkConnectionTimeoutMs = 5000;

    /** 注册到zookeeper中的根节点名称 */
    private String zkRootNodeName = ConstantValues.ZK_ROOT_NODE_NAME;

    /** 负载均衡默认对象 */
    private Class loadBalancerName = RoundLoadBalancer.class;

    /** 监控平台地址，多个使用逗号间隔 */
    private String monitorHosts;

    /** 全局的熔断器配置 */
    private CircuitBreakerConfig globalBreakerConfig;

    /** 调用日志记录配置 */
    private RpcCallLogHolderConfig rpcCallLogHolderConfig;

    /** RPC 客户端连接池配置 */
    private RpcClientsConfig rpcClientsConfig;

    /** 指定接口的熔断器配置 */
    private Map<String, CircuitBreakerConfig> interfaceBreakerConfigs;

    /** 用于开发状态下接口的直连设置，在生产环境下不建议使用 */
    private Map<String, DirectNodeHostEntity> directHosts;

    /** Metrics Config */
    private MetricsConfig metricsConfig;

    /** Codec Config */
    private CodecConfig codecConfig;

    /** Spring Environment */
    private Environment environment;

    @Override
    public CodecConfig getCodecConfig() {
        return null == codecConfig ? new CodecConfig() : codecConfig;
    }

    public void setCodecConfig(CodecConfig codecConfig) {
        this.codecConfig = codecConfig;
    }

    @Override
    public RpcClientsConfig getRpcClientsConfig() {
        return null == rpcClientsConfig ? new RpcClientsConfig() : rpcClientsConfig;
    }

    public void setRpcClientsConfig(RpcClientsConfig rpcClientsConfig) {
        this.rpcClientsConfig = rpcClientsConfig;
    }

    @Override
    public RpcCallLogHolderConfig getRpcCallLogHolderConfig() {
        return null == rpcCallLogHolderConfig
                ? new RpcCallLogHolderConfig()
                : rpcCallLogHolderConfig;
    }

    public void setRpcCallLogHolderConfig(RpcCallLogHolderConfig rpcCallLogHolderConfig) {
        this.rpcCallLogHolderConfig = rpcCallLogHolderConfig;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public MetricsConfig getMetricsConfig() {
        return metricsConfig;
    }

    public void setMetricsConfig(MetricsConfig metricsConfig) {
        this.metricsConfig = metricsConfig;
    }

    @Override
    public Map<String, DirectNodeHostEntity> getDirectHosts() {
        return directHosts;
    }

    public void setDirectHosts(Map<String, DirectNodeHostEntity> directHosts) {
        this.directHosts = directHosts;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setZkIps(String zkIps) {
        this.zkIps = zkIps;
    }

    public void setZkConnectRetryBaseSleepMs(Integer zkConnectRetryBaseSleepMs) {
        this.zkConnectRetryBaseSleepMs = zkConnectRetryBaseSleepMs;
    }

    public void setZkConnectMaxRetries(Integer zkConnectMaxRetries) {
        this.zkConnectMaxRetries = zkConnectMaxRetries;
    }

    public void setZkConnectRetryMaxSleepMs(Integer zkConnectRetryMaxSleepMs) {
        this.zkConnectRetryMaxSleepMs = zkConnectRetryMaxSleepMs;
    }

    public void setZkConnectionTimeoutMs(Integer zkConnectionTimeoutMs) {
        this.zkConnectionTimeoutMs = zkConnectionTimeoutMs;
    }

    public void setZkRootNodeName(String zkRootNodeName) {
        this.zkRootNodeName = zkRootNodeName;
    }

    public void setLoadBalancerName(Class loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    public void setMonitorHosts(String monitorHosts) {
        this.monitorHosts = monitorHosts;
    }

    public void setGlobalBreakerConfig(CircuitBreakerConfig globalBreakerConfig) {
        this.globalBreakerConfig = globalBreakerConfig;
    }

    public void setInterfaceBreakerConfigs(
            Map<String, CircuitBreakerConfig> interfaceBreakerConfigs) {
        this.interfaceBreakerConfigs = interfaceBreakerConfigs;
    }

    @Override
    public boolean isStartServer() {
        return startServer;
    }

    public void setStartServer(boolean startServer) {
        this.startServer = startServer;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public String getZkIps() {
        return zkIps;
    }

    @Override
    public Integer getZkConnectRetryBaseSleepMs() {
        return zkConnectRetryBaseSleepMs;
    }

    @Override
    public Integer getZkConnectMaxRetries() {
        return zkConnectMaxRetries;
    }

    @Override
    public Integer getZkConnectRetryMaxSleepMs() {
        return zkConnectRetryMaxSleepMs;
    }

    @Override
    public Integer getZkConnectionTimeoutMs() {
        return zkConnectionTimeoutMs;
    }

    @Override
    public String getZkRootNodeName() {
        return zkRootNodeName;
    }

    @Override
    public Class getLoadBalancerName() {
        return loadBalancerName;
    }

    @Override
    public String getMonitorHosts() {
        return monitorHosts;
    }

    @Override
    public CircuitBreakerConfig getGlobalBreakerConfig() {
        return globalBreakerConfig;
    }

    @Override
    public Map<String, CircuitBreakerConfig> getInterfaceBreakerConfigs() {
        return interfaceBreakerConfigs;
    }
}
