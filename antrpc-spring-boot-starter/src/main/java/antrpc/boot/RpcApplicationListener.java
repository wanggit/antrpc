package antrpc.boot;

import antrpc.AntrpcContext;
import antrpc.IAntrpcContext;
import antrpc.client.Host;
import antrpc.client.monitor.IRpcCallLogHolder;
import antrpc.client.monitor.MonitorMetricsSender;
import antrpc.client.monitor.RpcCallLogHolder;
import antrpc.client.spring.BeanContainer;
import antrpc.client.spring.RpcBeanContainer;
import antrpc.client.zk.register.Register;
import antrpc.client.zk.zknode.DirectNodeHostEntity;
import antrpc.commons.breaker.CircuitBreaker;
import antrpc.commons.breaker.ICircuitBreaker;
import antrpc.commons.config.CircuitBreakerConfig;
import antrpc.commons.config.Configuration;
import antrpc.commons.config.IConfiguration;
import antrpc.commons.config.MetricsConfig;
import antrpc.commons.constants.ConstantValues;
import antrpc.commons.metrics.IMetricsSender;
import antrpc.commons.metrics.JvmMetrics;
import antrpc.commons.utils.ApplicationNameUtil;
import antrpc.server.invoker.RpcRequestBeanInvoker;
import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.*;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
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
public class RpcApplicationListener implements GenericApplicationListener {

    private static final String INTERFACE_BREAKER = "interface-circuit-breaker";
    private final ConfigurationPropertyName RPC_INTERFACE_PROP =
            ConfigurationPropertyName.of(
                    ConstantValues.ANTRPC_CONFIG_PREFIX + "." + INTERFACE_BREAKER);
    private final Bindable<Map<String, String>> RPC_INTERFACE_MAP =
            Bindable.mapOf(String.class, String.class);

    private static final String ANTRPC_CONTEXT_BEAN_NAME = "antrpcContext";
    private IAntrpcContext context;

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        } else if (event instanceof ApplicationStartingEvent) {
            onApplicationStartingEvent((ApplicationStartingEvent) event);
        } else if (event instanceof ApplicationPreparedEvent) {
            onApplicationPreparedEvent((ApplicationPreparedEvent) event);
        } else if (event instanceof ApplicationContextInitializedEvent) {
            onApplicationContextInitializedEvent((ApplicationContextInitializedEvent) event);
        } else if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof ApplicationReadyEvent) {
            onApplicationReadyEvent((ApplicationReadyEvent) event);
        }
    }

    private void onApplicationReadyEvent(ApplicationReadyEvent event) {
        IAntrpcContext antrpcContext = event.getApplicationContext().getBean(IAntrpcContext.class);
        antrpcContext.startServer();
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        Register register = applicationContext.getBean(Register.class);
        AntrpcContext antrpcContext = (AntrpcContext) context;
        antrpcContext.setRegister(register);
        RpcProperties rpcProperties = applicationContext.getBean(RpcProperties.class);
        MetricsConfig metricsConfig = rpcProperties.getMetricsConfig();
        if (null != metricsConfig) {
            ConfigurableListableBeanFactory beanFactory =
                    ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
            IMetricsSender metricsSender =
                    new MonitorMetricsSender(
                            ApplicationNameUtil.getApplicationName(
                                    applicationContext.getEnvironment()),
                            antrpcContext.getRpcClients());
            beanFactory.registerSingleton(IMetricsSender.class.getName(), metricsSender);
            JvmMetrics jvmMetrics =
                    new JvmMetrics(
                            metricsConfig,
                            applicationContext.getBean(MetricRegistry.class),
                            antrpcContext.getConfiguration(),
                            metricsSender,
                            antrpcContext.getRpcClients());
            beanFactory.registerSingleton(JvmMetrics.class.getName(), jvmMetrics);
            jvmMetrics.init();
        }
    }

    private void onApplicationContextInitializedEvent(ApplicationContextInitializedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        if (!beanFactory.containsBean(ANTRPC_CONTEXT_BEAN_NAME)) {
            beanFactory.registerSingleton(ANTRPC_CONTEXT_BEAN_NAME, context);
        }
    }

    private void onApplicationStartingEvent(ApplicationStartingEvent event) {
        if (null == context) {
            IConfiguration configuration = new Configuration();
            BeanContainer beanContainer = new RpcBeanContainer();
            ICircuitBreaker circuitBreaker = new CircuitBreaker();
            IRpcCallLogHolder rpcCallLogHolder = new RpcCallLogHolder();
            context =
                    new AntrpcContext(
                            configuration, beanContainer, circuitBreaker, rpcCallLogHolder);
        }
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
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
        context.init();
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

    private void onApplicationPreparedEvent(ApplicationPreparedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        AntrpcContext antrpcContext = (AntrpcContext) context;
        antrpcContext.setRpcRequestBeanInvoker(new RpcRequestBeanInvoker(beanFactory));
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
        String loadBalancer = rpcProperties.getLoadBalancer();
        if (null != loadBalancer) {
            try {
                configuration.setLoadBalancerName(Class.forName(loadBalancer));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        String monitorHost = rpcProperties.getMonitorHost();
        if (null != monitorHost) {
            configuration.setMonitorHosts(monitorHost);
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
        configuration.setRpcCallLogHolderConfig(rpcProperties.getRpcCallLogHolderConfig());
        configuration.setRpcClientsConfig(rpcProperties.getRpcClientsConfig());
        configuration.setCodecConfig(rpcProperties.getCodecConfig());
    }
}
