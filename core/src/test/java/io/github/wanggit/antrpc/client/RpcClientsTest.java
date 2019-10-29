package io.github.wanggit.antrpc.client;

import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.client.spring.IOnFailProcessor;
import io.github.wanggit.antrpc.client.spring.IRpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.Register;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.codec.cryption.AESCodec;
import io.github.wanggit.antrpc.commons.config.CodecConfig;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import io.github.wanggit.antrpc.commons.test.ZkNodeTestUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.*;

public class RpcClientsTest {

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
        ZkNodeTestUtil.afterClear(rpcPort);
        ZkNodeTestUtil.afterClear(clientRpcPort);
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
        ZkNodeTestUtil.afterClear(rpcPort);
        ZkNodeTestUtil.afterClear(clientRpcPort);
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
        ZkNodeTestUtil.afterClear(rpcPort);
        ZkNodeTestUtil.afterClear(clientRpcPort);
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
            clientApplicationContext
                    .getBeanFactory()
                    .registerSingleton(entry.getKey(), entry.getValue());
        }
        Configuration configuration = new Configuration();
        configuration.setPort(rpcPort);
        AntrpcContext clientAntrpcContext = new AntrpcContext(configuration);
        configurationCallback.initConfiguration(
                (Configuration) clientAntrpcContext.getConfiguration());
        clientApplicationContext
                .getBeanFactory()
                .registerSingleton(AntrpcContext.class.getName(), clientAntrpcContext);
        clientApplicationContext
                .getBeanFactory()
                .registerSingleton(Register.class.getName(), new ZkRegister());
        clientApplicationContext
                .getBeanFactory()
                .registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        clientApplicationContext
                .getBeanFactory()
                .registerSingleton(
                        IRpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());
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
                genericApplicationContext
                        .getBeanFactory()
                        .registerSingleton(entry.getKey(), entry.getValue());
            }
        }

        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        configurationCallback.initConfiguration((Configuration) antrpcContext.getConfiguration());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(AntrpcContext.class.getName(), antrpcContext);
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(Register.class.getName(), new ZkRegister());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(
                        IRpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());

        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(rpcPort);
        configuration.setStartServer(true);
        configuration.setEnvironment(environment);
        antrpcContext.init(genericApplicationContext);
        for (RegisterBean registerBean : registerBeans) {
            antrpcContext.getRegister().register(registerBean, antrpcContext.getZkNodeBuilder());
        }
    }

    @RpcService
    interface TestInterface {
        @RpcMethod
        String doMethod();

        @RpcMethod
        String sendMoreMessage(String content);
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
