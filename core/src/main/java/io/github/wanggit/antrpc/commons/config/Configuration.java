package io.github.wanggit.antrpc.commons.config;

import io.github.wanggit.antrpc.client.zk.lb.RoundLoadBalancer;
import io.github.wanggit.antrpc.client.zk.zknode.DirectNodeHostEntity;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import org.springframework.core.env.Environment;

import java.util.Map;

public class Configuration implements IConfiguration {

    /** 暴露的RPC端口 */
    private Integer port = 6060;

    /** 对外注册时暴露的IP */
    private String exposeIp = null;

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

    /** 全局的熔断器配置 */
    private CircuitBreakerConfig globalBreakerConfig;

    /** 处理连接是否可用的熔断器配置 */
    private CircuitBreakerConfig connectionBreakerConfig;

    /** 调用日志记录配置 */
    private CallLogReporterConfig callLogReporterConfig;

    /** RPC 客户端连接池配置 */
    private RpcClientsConfig rpcClientsConfig;

    /** 指定接口的熔断器配置 */
    private Map<String, CircuitBreakerConfig> interfaceBreakerConfigs;

    /** 用于开发状态下接口的直连设置，在生产环境下不建议使用 */
    private Map<String, DirectNodeHostEntity> directHosts;

    /** Codec Config */
    private CodecConfig codecConfig;

    /** Serializer Config */
    private SerializeConfig serializeConfig;

    /** Spring Environment */
    private Environment environment;

    /** Application Name */
    private String appName;

    /**
     * 获取此服务的名称
     *
     * @return Application Name
     */
    @Override
    public String getApplicationName() {
        if (null == appName) {
            appName = environment.getProperty("spring.application.name");
            if (null == appName) {
                throw new RuntimeException("spring.application.name is not configured,");
            }
        }
        return this.appName;
    }

    @Override
    public String getExposeIp() {
        return exposeIp;
    }

    public void setExposeIp(String exposeIp) {
        this.exposeIp = exposeIp;
    }

    @Override
    public SerializeConfig getSerializeConfig() {
        return null == serializeConfig ? new SerializeConfig() : serializeConfig;
    }

    public void setSerializeConfig(SerializeConfig serializeConfig) {
        this.serializeConfig = serializeConfig;
    }

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
    public CallLogReporterConfig getCallLogReporterConfig() {
        return null == callLogReporterConfig ? new CallLogReporterConfig() : callLogReporterConfig;
    }

    public void setCallLogReporterConfig(CallLogReporterConfig callLogReporterConfig) {
        this.callLogReporterConfig = callLogReporterConfig;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
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

    public void setGlobalBreakerConfig(CircuitBreakerConfig globalBreakerConfig) {
        this.globalBreakerConfig = globalBreakerConfig;
    }

    @Override
    public CircuitBreakerConfig getConnectionBreakerConfig() {
        if (null == connectionBreakerConfig) {
            this.connectionBreakerConfig = new CircuitBreakerConfig();
            this.connectionBreakerConfig.setThreshold(
                    ConstantValues.CONNECTION_CIRCUIT_BREAKER_THRESHOLD);
            this.connectionBreakerConfig.setCheckIntervalSeconds(
                    ConstantValues.CONNECTION_CIRCUIT_BREAKER_CHECK_INTERVAL_SECONDS);
            this.connectionBreakerConfig.checkSelf();
        }
        return connectionBreakerConfig;
    }

    public void setConnectionBreakerConfig(CircuitBreakerConfig connectionBreakerConfig) {
        if (null != connectionBreakerConfig) {
            connectionBreakerConfig.checkSelf();
            this.connectionBreakerConfig = connectionBreakerConfig;
        }
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
    public CircuitBreakerConfig getGlobalBreakerConfig() {
        return globalBreakerConfig;
    }

    @Override
    public Map<String, CircuitBreakerConfig> getInterfaceBreakerConfigs() {
        return interfaceBreakerConfigs;
    }
}
