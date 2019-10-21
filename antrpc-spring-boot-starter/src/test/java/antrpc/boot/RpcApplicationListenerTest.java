package antrpc.boot;

import antrpc.AntrpcContext;
import antrpc.IAntrpcContext;
import antrpc.client.spring.RpcAutowiredProcessor;
import antrpc.client.zk.listener.ZkListener;
import antrpc.client.zk.register.Register;
import antrpc.client.zk.register.ZkRegister;
import antrpc.client.zk.zknode.ZkNodeKeeper;
import antrpc.commons.config.CircuitBreakerConfig;
import antrpc.commons.config.IConfiguration;
import antrpc.commons.config.RpcCallLogHolderConfig;
import antrpc.commons.config.RpcClientsConfig;
import antrpc.commons.constants.ConstantValues;
import antrpc.commons.metrics.IMetricsSender;
import antrpc.commons.metrics.JvmMetrics;
import antrpc.server.IServer;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.*;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class RpcApplicationListenerTest {

    @Test
    public void testOnApplicationEvent() throws Exception {
        RpcApplicationListener rpcApplicationListener = new RpcApplicationListener();
        // 1 ApplicationStartingEvent
        SpringApplication springApplication =
                new SpringApplication(RpcApplicationListenerTestApplication.class);
        ApplicationStartingEvent applicationStartingEvent =
                new ApplicationStartingEvent(springApplication, new String[] {});
        rpcApplicationListener.onApplicationEvent(applicationStartingEvent);
        Field field =
                ReflectionUtils.findField(
                        RpcApplicationListener.class, "context", IAntrpcContext.class);
        ReflectionUtils.makeAccessible(field);
        Object contextObj = ReflectionUtils.getField(field, rpcApplicationListener);
        Assert.assertNotNull(contextObj);
        Assert.assertTrue(contextObj instanceof IAntrpcContext);
        IAntrpcContext antrpcContext = (IAntrpcContext) contextObj;
        Assert.assertNotNull(antrpcContext.getConfiguration());
        Assert.assertNotNull(antrpcContext.getBeanContainer());
        Assert.assertNotNull(antrpcContext.getCircuitBreaker());
        Assert.assertNotNull(antrpcContext.getRpcCallLogHolder());

        // 2 ApplicationEnvironmentPreparedEvent
        MockEnvironment environment = new MockEnvironment();
        int rpcPort = RandomUtils.nextInt(1000, 9999);
        environment
                .withProperty("server.port", String.valueOf(RandomUtils.nextInt(1000, 9999)))
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.zk-servers", "localhost:2181")
                .withProperty("antrpc.circuit-breakers.threshold", "10")
                .withProperty("antrpc.circuit-breakers.check-interval-seconds", "5")
                .withProperty("antrpc.metrics-config.enable", "true")
                .withProperty("antrpc.rpc-call-log-holder-config.report-argument-values", "true")
                .withProperty("antrpc.rpc-clients-config.max-total", "200");
        ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent =
                new ApplicationEnvironmentPreparedEvent(
                        springApplication, new String[] {}, environment);
        rpcApplicationListener.onApplicationEvent(applicationEnvironmentPreparedEvent);
        IConfiguration configuration = antrpcContext.getConfiguration();
        Assert.assertEquals(configuration.getPort().intValue(), rpcPort);
        Assert.assertEquals(configuration.getZkIps(), "localhost:2181");
        CircuitBreakerConfig globalBreakerConfig = configuration.getGlobalBreakerConfig();
        Assert.assertNotNull(globalBreakerConfig);
        Assert.assertEquals(globalBreakerConfig.getThreshold(), 10);
        Assert.assertEquals(globalBreakerConfig.getCheckIntervalSeconds(), 5);

        RpcCallLogHolderConfig rpcCallLogHolderConfig = configuration.getRpcCallLogHolderConfig();
        Assert.assertNotNull(rpcCallLogHolderConfig);
        Assert.assertTrue(rpcCallLogHolderConfig.isReportArgumentValues());

        RpcClientsConfig rpcClientsConfig = configuration.getRpcClientsConfig();
        Assert.assertNotNull(rpcClientsConfig);
        Assert.assertEquals(rpcClientsConfig.getMaxTotal(), 200);

        RpcProperties properties =
                Binder.get(environment)
                        .bind(
                                ConfigurationPropertyName.of(ConstantValues.ANTRPC_CONFIG_PREFIX),
                                Bindable.of(RpcProperties.class))
                        .orElseGet(RpcProperties::new);

        // 3 ApplicationContextInitializedEvent
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        genericApplicationContext.setEnvironment(environment);
        ApplicationContextInitializedEvent applicationContextInitializedEvent =
                new ApplicationContextInitializedEvent(
                        springApplication, new String[] {}, genericApplicationContext);
        genericApplicationContext.refresh();
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(RpcProperties.class.getName(), properties);
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(
                        RpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(ZkRegister.class.getName(), new ZkRegister());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(ZkListener.class.getName(), new ZkListener());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(ZkNodeKeeper.class.getName(), new ZkNodeKeeper());
        rpcApplicationListener.onApplicationEvent(applicationContextInitializedEvent);
        IAntrpcContext bean = genericApplicationContext.getBean(IAntrpcContext.class);
        Assert.assertNotNull(bean);
        Object contextBean = genericApplicationContext.getBean("antrpcContext");
        Assert.assertNotNull(contextBean);
        Assert.assertTrue(contextBean instanceof IAntrpcContext);

        // 4 ApplicationPreparedEvent
        ApplicationPreparedEvent applicationPreparedEvent =
                new ApplicationPreparedEvent(
                        springApplication, new String[] {}, genericApplicationContext);
        rpcApplicationListener.onApplicationEvent(applicationPreparedEvent);
        IAntrpcContext context = genericApplicationContext.getBean(IAntrpcContext.class);
        Assert.assertNotNull(context);
        Assert.assertNotNull(context.getRpcRequestBeanInvoker());

        // 5 ContextRefreshedEvent
        ContextRefreshedEvent contextRefreshedEvent =
                new ContextRefreshedEvent(genericApplicationContext);
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(MetricRegistry.class.getName(), new MetricRegistry());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(HealthCheckRegistry.class.getName(), new HealthCheckRegistry());
        rpcApplicationListener.onApplicationEvent(contextRefreshedEvent);
        Register register = genericApplicationContext.getBean(Register.class);
        Assert.assertNotNull(register);
        RpcProperties rpcProperties = genericApplicationContext.getBean(RpcProperties.class);
        Assert.assertNotNull(rpcProperties);
        if (null != rpcProperties.getMetricsConfig()
                && rpcProperties.getMetricsConfig().isEnable()) {
            IMetricsSender metricsSender = genericApplicationContext.getBean(IMetricsSender.class);
            Assert.assertNotNull(metricsSender);
            JvmMetrics jvmMetrics = genericApplicationContext.getBean(JvmMetrics.class);
            Assert.assertNotNull(jvmMetrics);
        }
        Assert.assertNotNull(antrpcContext.getRpcRequestBeanInvoker());
        Assert.assertNotNull(antrpcContext.getCircuitBreaker());
        Assert.assertNotNull(antrpcContext.getBeanContainer());
        Assert.assertNotNull(antrpcContext.getLoadBalancerHelper());
        Assert.assertNotNull(antrpcContext.getNodeHostContainer());
        Assert.assertNotNull(antrpcContext.getConfiguration());
        Assert.assertNotNull(antrpcContext.getRegister());
        Assert.assertNotNull(antrpcContext.getRpcCallLogHolder());
        Assert.assertNotNull(antrpcContext.getZkClient());
        Assert.assertNotNull(antrpcContext.getZkNodeBuilder());
        Assert.assertNotNull(antrpcContext.getZkRegisterHolder());

        // 8 ApplicationReadyEvent
        ApplicationReadyEvent applicationReadyEvent =
                new ApplicationReadyEvent(
                        springApplication, new String[] {}, genericApplicationContext);
        rpcApplicationListener.onApplicationEvent(applicationReadyEvent);
        AntrpcContext antrpcContextImpl = (AntrpcContext) antrpcContext;
        IServer server = antrpcContextImpl.getServer();
        Assert.assertNotNull(server);
        Assert.assertTrue(server.isActive());
    }

    @SpringBootApplication
    public static class RpcApplicationListenerTestApplication {
        public static void main(String[] args) {
            SpringApplication.run(RpcApplicationListenerTestApplication.class);
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
