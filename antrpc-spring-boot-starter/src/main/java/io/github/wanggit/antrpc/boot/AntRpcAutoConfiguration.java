package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.client.zk.zknode.DirectNodeHostEntity;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(RpcProperties.class)
public class AntRpcAutoConfiguration {

    private static final String INTERFACE_BREAKER = "interface-circuit-breaker";
    private final ConfigurationPropertyName RPC_INTERFACE_PROP =
            ConfigurationPropertyName.of(
                    ConstantValues.ANTRPC_CONFIG_PREFIX + "." + INTERFACE_BREAKER);
    private final Bindable<Map<String, String>> RPC_INTERFACE_MAP =
            Bindable.mapOf(String.class, String.class);

    @Autowired private RpcProperties rpcProperties;

    @Bean
    public AntrpcStater antrpcStater(ApplicationContext applicationContext) {
        return new AntrpcStater(applicationContext);
    }

    @Bean
    public IConfiguration configuration(Environment environment) {
        io.github.wanggit.antrpc.commons.config.Configuration configuration =
                new io.github.wanggit.antrpc.commons.config.Configuration();

        Binder binder = Binder.get(environment);
        if (null == configuration) {
            throw new IllegalStateException("Configuration cannot be empty.");
        }
        RpcProperties rpcProperties = getRpcProperties(binder);
        initConfiguration(configuration, rpcProperties);

        Map<String, String> map = getCircuitBreakerConfigMap(binder);
        Map<String, CircuitBreakerConfig> interfaceBreakers = new HashMap<>(map.size() * 2);
        map.forEach(
                (key, value) -> {
                    String interfaceName = key.substring(0, key.lastIndexOf("."));
                    if (!interfaceBreakers.containsKey(interfaceName)) {
                        interfaceBreakers.put(interfaceName, new CircuitBreakerConfig());
                    }
                    CircuitBreakerConfig config = interfaceBreakers.get(interfaceName);
                    if (key.endsWith("threshold")) {
                        config.setThreshold(Integer.parseInt(value));
                    } else {
                        config.setCheckIntervalSeconds(Long.parseLong(value));
                    }
                });
        interfaceBreakers.forEach(
                (key, value) -> {
                    value.checkSelf();
                });
        configuration.setInterfaceBreakerConfigs(interfaceBreakers);
        configuration.setEnvironment(environment);
        return configuration;
    }

    private RpcProperties getRpcProperties(Binder binder) {
        return binder.bind(
                        ConfigurationPropertyName.of(ConstantValues.ANTRPC_CONFIG_PREFIX),
                        Bindable.of(RpcProperties.class))
                .orElseGet(RpcProperties::new);
    }

    private void initConfiguration(
            io.github.wanggit.antrpc.commons.config.Configuration configuration,
            RpcProperties rpcProperties) {
        configuration.setZkIps(
                null == rpcProperties.getZkServers()
                        ? ConstantValues.RPC_DEFAULT_ZK_SERVER
                        : rpcProperties.getZkServers());
        configuration.setPort(
                null == rpcProperties.getPort()
                        ? ConstantValues.RPC_DEFAULT_PORT
                        : rpcProperties.getPort());
        configuration.setExposeIp(rpcProperties.getExposedIp());
        String loadBalancer = rpcProperties.getLoadBalancer();
        if (null != loadBalancer) {
            try {
                configuration.setLoadBalancerName(Class.forName(loadBalancer));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        if (null != rpcProperties.getDirectHosts() && !rpcProperties.getDirectHosts().isEmpty()) {
            Map<String, DirectNodeHostEntity> directHostEntities =
                    new HashMap<>(rpcProperties.getDirectHosts().size() * 2);
            rpcProperties
                    .getDirectHosts()
                    .forEach(
                            (key, value) -> {
                                DirectNodeHostEntity directNodeHostEntity =
                                        DirectNodeHostEntity.from(Host.parse(value));
                                directHostEntities.put(key, directNodeHostEntity);
                            });
            configuration.setDirectHosts(directHostEntities);
        }
        configuration.setStartServer(rpcProperties.isStartServer());
        configuration.setGlobalBreakerConfig(rpcProperties.getCircuitBreakers());
        configuration.setConnectionBreakerConfig(rpcProperties.getConnectionBreakerConfig());
        configuration.setCallLogReporterConfig(rpcProperties.getCallLogReporterConfig());
        configuration.setRpcClientsConfig(rpcProperties.getRpcClientsConfig());
        configuration.setCodecConfig(rpcProperties.getCodecConfig());
        configuration.setSerializeConfig(rpcProperties.getSerializeConfig());
    }

    private Map<String, String> getCircuitBreakerConfigMap(Binder binder) {
        return binder.bind(RPC_INTERFACE_PROP, RPC_INTERFACE_MAP).orElseGet(Collections::emptyMap);
    }
}
