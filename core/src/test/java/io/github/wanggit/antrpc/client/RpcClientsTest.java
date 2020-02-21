package io.github.wanggit.antrpc.client;

import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.codec.cryption.AESCodec;
import io.github.wanggit.antrpc.commons.config.CodecConfig;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.*;

public class RpcClientsTest {

    @RpcService
    interface Test3Interface {

        @RpcMethod
        void sayHello(String name);
    }

    public static class Test3Impl implements Test3Interface {

        @Override
        public void sayHello(String name) {
            throw new RuntimeException("raise exception " + name);
        }
    }

    @Test
    public void testServiceProviderRaiseException() throws InterruptedException {
        int httpPort = RandomUtils.nextInt(2000, 9999);
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        RegisterBean registerBean =
                createRegisterBeanCommons(
                        rpcPort,
                        Test3Interface.class.getName(),
                        "sayHello",
                        Lists.newArrayList("java.lang.String"));
        Map<String, Object> singletons = new HashMap<>();
        singletons.put(Test3Impl.class.getName(), new Test3Impl());
        createRpcServer(
                httpPort,
                rpcPort,
                "testServiceProviderRaiseExceptionServer",
                singletons,
                Lists.newArrayList(registerBean),
                new ConfigurationCallback() {
                    @Override
                    public void initConfiguration(Configuration configuration) {}
                });

        int clientTttpPort = RandomUtils.nextInt(2000, 9999);
        int clientRpcPort = RandomUtils.nextInt(2000, 9999);
        AntrpcContext antrpcContext =
                createRpcClient(
                        clientTttpPort,
                        clientRpcPort,
                        "testServiceProviderRaiseExceptionClient",
                        singletons,
                        new ConfigurationCallback() {
                            @Override
                            public void initConfiguration(Configuration configuration) {}
                        });
        Object bean = antrpcContext.getBeanContainer().getOrCreateBean(Test3Interface.class);
        Assert.assertTrue(bean instanceof Test3Interface);
        Test3Interface test3Interface = (Test3Interface) bean;
        test3Interface.sayHello("wanggang");
    }

    @Test
    public void testHeartBeat() throws InterruptedException {
        int httpPort = RandomUtils.nextInt(2000, 9999);
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        RegisterBean registerBean = createRegisterBean2(rpcPort);
        Map<String, Object> singletons = new HashMap<>();
        singletons.put(Test2Impl.class.getName(), new Test2Impl());
        createRpcServer(
                httpPort,
                rpcPort,
                "testHeartBeatServer",
                singletons,
                Lists.newArrayList(registerBean),
                new ConfigurationCallback() {
                    @Override
                    public void initConfiguration(Configuration configuration) {}
                });

        int clientHttpPort = RandomUtils.nextInt(2000, 9999);
        int clientRpcPort = RandomUtils.nextInt(2000, 9999);
        AntrpcContext antrpcContext =
                createRpcClient(
                        clientHttpPort,
                        clientRpcPort,
                        "testHeartBeatClient",
                        new HashMap<>(),
                        new ConfigurationCallback() {
                            @Override
                            public void initConfiguration(Configuration configuration) {}
                        });
        Object bean = antrpcContext.getBeanContainer().getOrCreateBean(Test2Interface.class);
        Assert.assertTrue(bean instanceof Test2Interface);
        Test2Interface testInterface = (Test2Interface) bean;
        testInterface.sendMoreMessage("Hello");
        WaitUtil.wait(20, 1);
    }

    @Test
    public void testCodecAndZipDoRpc() throws Exception {
        String key = UUID.randomUUID().toString();
        // server
        int httpPort = RandomUtils.nextInt(2000, 9999);
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        RegisterBean registerBean = createRegisterBean(rpcPort);
        Map<String, Object> singletons = new HashMap<>();
        singletons.put(TestImpl.class.getName(), new TestImpl());
        createRpcServer(
                httpPort,
                rpcPort,
                "testServer",
                singletons,
                Lists.newArrayList(registerBean),
                new ConfigurationCallback() {
                    @Override
                    public void initConfiguration(Configuration configuration) {
                        configuration.setCodecConfig(getCodecConfig(key));
                    }
                });

        // client
        int clientHttpPort = RandomUtils.nextInt(1300, 9999);
        int clientRpcPort = RandomUtils.nextInt(1300, 9999);
        AntrpcContext clientAntrpcContext =
                createRpcClient(
                        clientHttpPort,
                        clientRpcPort,
                        "testClient",
                        new HashMap<>(),
                        new ConfigurationCallback() {
                            @Override
                            public void initConfiguration(Configuration configuration) {
                                configuration.setCodecConfig(getCodecConfig(key));
                            }
                        });
        Assert.assertTrue(clientAntrpcContext.getCodecHolder().getCodec() instanceof AESCodec);
        Object bean = clientAntrpcContext.getBeanContainer().getOrCreateBean(TestInterface.class);
        Assert.assertTrue(bean instanceof TestInterface);
        TestInterface testInterface = (TestInterface) bean;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append(RandomStringUtils.randomAlphanumeric(50));
        }
        String result = testInterface.sendMoreMessage(builder.toString());
        Assert.assertEquals(result, builder.toString());
    }

    @Test
    public void testCodecDoRpc() throws Exception {
        String key = UUID.randomUUID().toString();
        // server
        int httpPort = RandomUtils.nextInt(2000, 9999);
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        RegisterBean registerBean = createRegisterBean(rpcPort);
        Map<String, Object> singletons = new HashMap<>();
        singletons.put(TestImpl.class.getName(), new TestImpl());
        createRpcServer(
                httpPort,
                rpcPort,
                "testServer",
                singletons,
                Lists.newArrayList(registerBean),
                new ConfigurationCallback() {
                    @Override
                    public void initConfiguration(Configuration configuration) {
                        configuration.setCodecConfig(getCodecConfig(key));
                    }
                });

        // client
        int clientHttpPort = RandomUtils.nextInt(1300, 9999);
        int clientRpcPort = RandomUtils.nextInt(1300, 9999);
        AntrpcContext clientAntrpcContext =
                createRpcClient(
                        clientHttpPort,
                        clientRpcPort,
                        "testClient",
                        new HashMap<>(),
                        new ConfigurationCallback() {
                            @Override
                            public void initConfiguration(Configuration configuration) {
                                configuration.setCodecConfig(getCodecConfig(key));
                            }
                        });
        Assert.assertTrue(clientAntrpcContext.getCodecHolder().getCodec() instanceof AESCodec);
        Object bean = clientAntrpcContext.getBeanContainer().getOrCreateBean(TestInterface.class);
        Assert.assertTrue(bean instanceof TestInterface);
        TestInterface testInterface = (TestInterface) bean;
        String result = testInterface.doMethod();
        Assert.assertEquals(result, "Hello Test");
    }

    @Test
    public void testDoRpc() throws Exception {
        // server
        int httpPort = RandomUtils.nextInt(2000, 9999);
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        RegisterBean registerBean = createRegisterBean(rpcPort);
        Map<String, Object> singletons = new HashMap<>();
        singletons.put(TestImpl.class.getName(), new TestImpl());
        createRpcServer(
                httpPort,
                rpcPort,
                "testServer",
                singletons,
                Lists.newArrayList(registerBean),
                new ConfigurationCallback() {
                    @Override
                    public void initConfiguration(Configuration configuration) {}
                });

        // client
        int clientHttpPort = RandomUtils.nextInt(1300, 9999);
        int clientRpcPort = RandomUtils.nextInt(1300, 9999);
        AntrpcContext clientAntrpcContext =
                createRpcClient(
                        clientHttpPort,
                        clientRpcPort,
                        "testClient",
                        new HashMap<>(),
                        new ConfigurationCallback() {
                            @Override
                            public void initConfiguration(Configuration configuration) {}
                        });
        // WaitUtil.wait(5, 1);
        Object bean = clientAntrpcContext.getBeanContainer().getOrCreateBean(TestInterface.class);
        Assert.assertTrue(bean instanceof TestInterface);
        TestInterface testInterface = (TestInterface) bean;
        String result = testInterface.doMethod();
        Assert.assertEquals(result, "Hello Test");
    }

    private CodecConfig getCodecConfig(String key) {
        CodecConfig codecConfig = new CodecConfig();
        codecConfig.setEnable(true);
        codecConfig.setKey(key);
        codecConfig.setType(AESCodec.class.getName());
        return codecConfig;
    }

    private RegisterBean createRegisterBean(int rpcPort) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.setClassName(TestInterface.class.getName());
        registerBean.setPort(rpcPort);
        List<RegisterBean.RegisterBeanMethod> registerBeanMethods = new ArrayList<>();
        RegisterBean.RegisterBeanMethod registerBeanMethod = new RegisterBean.RegisterBeanMethod();
        registerBeanMethod.setMethodName("doMethod");
        registerBeanMethod.setParameterTypeNames(new ArrayList<>());
        registerBeanMethods.add(registerBeanMethod);
        registerBean.setMethods(registerBeanMethods);
        return registerBean;
    }

    private RegisterBean createRegisterBean2(int rpcPort) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.setClassName(Test2Interface.class.getName());
        registerBean.setPort(rpcPort);
        List<RegisterBean.RegisterBeanMethod> registerBeanMethods = new ArrayList<>();
        RegisterBean.RegisterBeanMethod registerBeanMethod = new RegisterBean.RegisterBeanMethod();
        registerBeanMethod.setMethodName("sendMoreMessage");
        registerBeanMethod.setParameterTypeNames(Lists.newArrayList("java.lang.String"));
        registerBeanMethods.add(registerBeanMethod);
        registerBean.setMethods(registerBeanMethods);
        return registerBean;
    }

    private RegisterBean createRegisterBeanCommons(
            int rpcPort, String className, String methodName, List<String> parameterTypes) {
        RegisterBean registerBean = new RegisterBean();
        registerBean.setClassName(className);
        registerBean.setPort(rpcPort);
        List<RegisterBean.RegisterBeanMethod> registerBeanMethods = new ArrayList<>();
        RegisterBean.RegisterBeanMethod registerBeanMethod = new RegisterBean.RegisterBeanMethod();
        registerBeanMethod.setMethodName(methodName);
        registerBeanMethod.setParameterTypeNames(parameterTypes);
        registerBeanMethods.add(registerBeanMethod);
        registerBean.setMethods(registerBeanMethods);
        return registerBean;
    }

    private AntrpcContext createRpcClient(
            int httpPort,
            int rpcPort,
            String serverName,
            Map<String, Object> singletons,
            ConfigurationCallback configurationCallback)
            throws InterruptedException {
        GenericApplicationContext clientApplicationContext = new GenericApplicationContext();
        MockEnvironment clientEnv = new MockEnvironment();
        clientEnv
                .withProperty("server.port", String.valueOf(httpPort))
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("spring.application.name", serverName);
        clientApplicationContext.setEnvironment(clientEnv);
        clientApplicationContext.refresh();
        for (Map.Entry<String, Object> entry : singletons.entrySet()) {
            GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
            genericBeanDefinition.setBeanClass(entry.getValue().getClass());
            ((BeanDefinitionRegistry) clientApplicationContext)
                    .registerBeanDefinition(entry.getKey(), genericBeanDefinition);
        }
        Configuration configuration = new Configuration();
        configuration.setPort(rpcPort);
        AntrpcContext clientAntrpcContext = new AntrpcContext(configuration);
        configurationCallback.initConfiguration(
                (Configuration) clientAntrpcContext.getConfiguration());
        Configuration clientConfiguration = (Configuration) clientAntrpcContext.getConfiguration();

        WaitUtil.wait(3, 1);
        clientConfiguration.setStartServer(false);
        clientConfiguration.setEnvironment(clientEnv);
        clientAntrpcContext.init(clientApplicationContext);
        return clientAntrpcContext;
    }

    private void createRpcServer(
            int httpPort,
            int rpcPort,
            String serverName,
            Map<String, Object> singletons,
            List<RegisterBean> registerBeans,
            ConfigurationCallback configurationCallback) {
        // server
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment
                .withProperty("server.port", String.valueOf(httpPort))
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("spring.application.name", serverName);
        genericApplicationContext.setEnvironment(environment);
        genericApplicationContext.refresh();
        if (null != singletons) {
            for (Map.Entry<String, Object> entry : singletons.entrySet()) {
                GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
                genericBeanDefinition.setBeanClass(entry.getValue().getClass());
                ((BeanDefinitionRegistry) genericApplicationContext)
                        .registerBeanDefinition(entry.getKey(), genericBeanDefinition);
            }
        }

        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        configurationCallback.initConfiguration((Configuration) antrpcContext.getConfiguration());

        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(rpcPort);
        configuration.setStartServer(true);
        configuration.setEnvironment(environment);
        antrpcContext.init(genericApplicationContext);
    }

    @RpcService
    interface TestInterface {
        @RpcMethod
        String doMethod();

        @RpcMethod
        String sendMoreMessage(String content);
    }

    @RpcService
    interface Test2Interface {
        @RpcMethod
        String sendMoreMessage(String content);
    }

    public static class Test2Impl implements Test2Interface {

        @Override
        public String sendMoreMessage(String content) {
            return content;
        }
    }

    public static class TestImpl implements TestInterface {

        @Override
        public String doMethod() {
            return "Hello Test";
        }

        @Override
        public String sendMoreMessage(String content) {
            return content;
        }
    }

    interface ConfigurationCallback {
        void initConfiguration(Configuration configuration);
    }
}
