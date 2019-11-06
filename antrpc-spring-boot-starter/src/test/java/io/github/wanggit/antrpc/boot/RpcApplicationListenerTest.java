package io.github.wanggit.antrpc.boot;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.spring.IOnFailProcessor;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.Register;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.commons.config.CallLogReporterConfig;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.config.RpcClientsConfig;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.server.IServer;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
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

        CallLogReporterConfig callLogReporterConfig = configuration.getCallLogReporterConfig();
        Assert.assertNotNull(callLogReporterConfig);
        Assert.assertFalse(callLogReporterConfig.isReportArgumentValues());

        RpcClientsConfig rpcClientsConfig = configuration.getRpcClientsConfig();
        Assert.assertNotNull(rpcClientsConfig);
        Assert.assertEquals(rpcClientsConfig.getMaxTotal(), 200);

        RpcProperties properties =
                Binder.get(environment)
                        .bind(
                                ConfigurationPropertyName.of(ConstantValues.ANTRPC_CONFIG_PREFIX),
                                Bindable.of(RpcProperties.class))
                        .orElseGet(RpcProperties::new);

        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        genericApplicationContext.setEnvironment(environment);
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
        // 3 ApplicationContextInitializedEvent
        /*ApplicationContextInitializedEvent applicationContextInitializedEvent =
                new ApplicationContextInitializedEvent(
                        springApplication, new String[] {}, genericApplicationContext);
        rpcApplicationListener.onApplicationEvent(applicationContextInitializedEvent);
        */

        // 4 ApplicationPreparedEvent
        ApplicationPreparedEvent applicationPreparedEvent =
                new ApplicationPreparedEvent(
                        springApplication, new String[] {}, genericApplicationContext);
        rpcApplicationListener.onApplicationEvent(applicationPreparedEvent);
        IAntrpcContext bean = genericApplicationContext.getBean(IAntrpcContext.class);
        Assert.assertNotNull(bean);
        Object contextBean = genericApplicationContext.getBean("antrpcContext");
        Assert.assertNotNull(contextBean);
        Assert.assertTrue(contextBean instanceof IAntrpcContext);

        // 5 ContextRefreshedEvent
        ContextRefreshedEvent contextRefreshedEvent =
                new ContextRefreshedEvent(genericApplicationContext);
        rpcApplicationListener.onApplicationEvent(contextRefreshedEvent);
        Register register = genericApplicationContext.getBean(Register.class);
        Assert.assertNotNull(register);
        RpcProperties rpcProperties = genericApplicationContext.getBean(RpcProperties.class);
        Assert.assertNotNull(rpcProperties);
        KafkaTemplate kafkaTemplate = null;
        try {
            kafkaTemplate = genericApplicationContext.getBean(KafkaTemplate.class);
        } catch (Exception e) {
        }

        // 8 ApplicationReadyEvent
        ApplicationReadyEvent applicationReadyEvent =
                new ApplicationReadyEvent(
                        springApplication, new String[] {}, genericApplicationContext);
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        rpcApplicationListener.onApplicationEvent(applicationReadyEvent);
        AntrpcContext antrpcContextImpl = (AntrpcContext) antrpcContext;
        IServer server = antrpcContextImpl.getServer();
        Assert.assertNotNull(server);
        Assert.assertNotNull(antrpcContext.getLoadBalancerHelper());
        Assert.assertNotNull(antrpcContext.getNodeHostContainer());
        Assert.assertNotNull(antrpcContext.getConfiguration());
        Assert.assertNotNull(antrpcContext.getRegister());
        Assert.assertNotNull(antrpcContext.getRpcCallLogHolder());
        Assert.assertNotNull(antrpcContext.getZkClient());
        Assert.assertNotNull(antrpcContext.getZkNodeBuilder());
        Assert.assertNotNull(antrpcContext.getZkRegisterHolder());
        Assert.assertNotNull(antrpcContext.getBeanContainer());
        Assert.assertNotNull(antrpcContext.getCircuitBreaker());
        Assert.assertNotNull(antrpcContext.getRpcCallLogHolder());
        Assert.assertNotNull(antrpcContext.getRpcRequestBeanInvoker());
        Assert.assertNotNull(antrpcContext.getCircuitBreaker());
        Assert.assertNotNull(antrpcContext.getBeanContainer());
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
