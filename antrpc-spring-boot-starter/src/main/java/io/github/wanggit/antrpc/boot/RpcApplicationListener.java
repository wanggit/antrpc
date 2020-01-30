package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.client.spring.IOnFailProcessor;
import io.github.wanggit.antrpc.client.spring.IRpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.client.zk.zknode.DirectNodeHostEntity;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.*;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring事件顺序 1 ApplicationStartingEvent 2 ApplicationEnvironmentPreparedEvent 3
 * ApplicationContextInitializedEvent 4 ApplicationPreparedEvent 5 ContextRefreshedEvent 6
 * ServletWebServerInitializedEvent 7 ApplicationStartedEvent 8 ApplicationReadyEvent
 */
@Slf4j
public class RpcApplicationListener implements ApplicationListener<ApplicationEvent> {

    private static final String INTERFACE_BREAKER = "interface-circuit-breaker";
    private final ConfigurationPropertyName RPC_INTERFACE_PROP =
            ConfigurationPropertyName.of(
                    ConstantValues.ANTRPC_CONFIG_PREFIX + "." + INTERFACE_BREAKER);
    private final Bindable<Map<String, String>> RPC_INTERFACE_MAP =
            Bindable.mapOf(String.class, String.class);

    private static final String ANTRPC_CONTEXT_BEAN_NAME = "antrpcContext";
    private IAntrpcContext context;
    private boolean onApplicationEnvironmentPreparedEventExecuted = false;
    private boolean onApplicationReadyEventExecuted = false;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationStartingEvent) {
            // 1
            onApplicationStartingEvent((ApplicationStartingEvent) event);
        } else if (event instanceof ApplicationEnvironmentPreparedEvent) {
            // 2
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        } else if (event instanceof ApplicationPreparedEvent) {
            // 4
            onApplicationPreparedEvent((ApplicationPreparedEvent) event);
        } else if (event instanceof ContextRefreshedEvent) {
            // 5
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ServletWebServerInitializedEvent) {
            // 6
        } else if (event instanceof ApplicationStartedEvent) {
            // 7
        } else if (event instanceof ApplicationReadyEvent) {
            // 8
            onApplicationReadyEvent((ApplicationReadyEvent) event);
        }
    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        if (null == context) {
            IConfiguration configuration = new Configuration();
            context = new AntrpcContext(configuration);
        }
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        if (onApplicationEnvironmentPreparedEventExecuted) {
            return;
        }
        onApplicationEnvironmentPreparedEventExecuted = true;
        ConfigurableEnvironment environment = event.getEnvironment();
        Binder binder = Binder.get(environment);
        IConfiguration configuration = context.getConfiguration();
        if (null == configuration) {
            throw new IllegalStateException("Configuration cannot be empty.");
        }
        RpcProperties rpcProperties = getRpcProperties(binder);
        initConfiguration((Configuration) configuration, rpcProperties);

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
        ((Configuration) configuration).setInterfaceBreakerConfigs(interfaceBreakers);
        ((Configuration) configuration).setEnvironment(environment);
    }

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        if (!beanFactory.containsBean(IRpcAutowiredProcessor.class.getName())) {
            beanFactory.registerSingleton(
                    IRpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());
        }
        if (!beanFactory.containsBean(IOnFailProcessor.class.getName())) {
            beanFactory.registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        }
        if (!beanFactory.containsBean(IRegister.class.getName())) {
            beanFactory.registerSingleton(IRegister.class.getName(), new ZkRegister());
        }
        if (!beanFactory.containsBean(ANTRPC_CONTEXT_BEAN_NAME)) {
            beanFactory.registerSingleton(ANTRPC_CONTEXT_BEAN_NAME, context);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {}

    private void onApplicationReadyEvent(ApplicationReadyEvent event) {
        if (onApplicationReadyEventExecuted) {
            return;
        }
        onApplicationReadyEventExecuted = true;
        IAntrpcContext antrpcContext = event.getApplicationContext().getBean(IAntrpcContext.class);
        antrpcContext.init(event.getApplicationContext());
    }

    private Map<String, String> getCircuitBreakerConfigMap(Binder binder) {
        return binder.bind(RPC_INTERFACE_PROP, RPC_INTERFACE_MAP).orElseGet(Collections::emptyMap);
    }

    private RpcProperties getRpcProperties(Binder binder) {
        return binder.bind(
                        ConfigurationPropertyName.of(ConstantValues.ANTRPC_CONFIG_PREFIX),
                        Bindable.of(RpcProperties.class))
                .orElseGet(RpcProperties::new);
    }

    private void initConfiguration(Configuration configuration, RpcProperties rpcProperties) {
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
}
