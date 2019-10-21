package antrpc.client.spring;

import antrpc.AntrpcContext;
import antrpc.IAntrpcContext;
import antrpc.client.monitor.RpcCallLogHolder;
import antrpc.client.zk.register.Register;
import antrpc.client.zk.register.ZkRegister;
import antrpc.client.zk.zknode.NodeHostEntity;
import antrpc.commons.annotations.OnRpcFail;
import antrpc.commons.annotations.RpcMethod;
import antrpc.commons.annotations.RpcService;
import antrpc.commons.breaker.CircuitBreaker;
import antrpc.commons.config.Configuration;
import antrpc.commons.config.RpcClientsConfig;
import antrpc.commons.test.WaitUtil;
import antrpc.server.invoker.RpcRequestBeanInvoker;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

public class CglibMethodInterceptorTest {

    @Test
    public void testRateLimitingAndDefaultResponse() throws InterruptedException {
        int serverRpcPort = RandomUtils.nextInt(1000, 9999);
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.withProperty("spring.application.name", "test");
        genericApplicationContext.setEnvironment(mockEnvironment);
        genericApplicationContext.refresh();
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(DogInterface.class.getName(), new MyDog());
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(Register.class.getName(), new ZkRegister());
        AntrpcContext serverAntrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        serverAntrpcContext.setRpcRequestBeanInvoker(
                new RpcRequestBeanInvoker(genericApplicationContext.getBeanFactory()));
        Configuration configuration = (Configuration) serverAntrpcContext.getConfiguration();
        configuration.setEnvironment(mockEnvironment);
        configuration.setPort(serverRpcPort);
        configuration.setZkIps("localhost:2181");
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(IAntrpcContext.class.getName(), serverAntrpcContext);
        serverAntrpcContext.init();
        serverAntrpcContext.startServer();
        Register register = genericApplicationContext.getBean(Register.class);
        ZkRegister zkRegister = (ZkRegister) register;
        zkRegister.setApplicationContext(genericApplicationContext);
        zkRegister.postProcessBeforeInitialization(
                genericApplicationContext.getBean(DogInterface.class),
                DogInterface.class.getName());

        // 无频率控制
        AntrpcContext clientAntrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        clientAntrpcContext.init();
        NodeHostEntity nodeHostEntity = getNodeHostEntity(serverRpcPort);
        clientAntrpcContext
                .getNodeHostContainer()
                .add(DogInterface.class.getName(), nodeHostEntity);
        Object bean = clientAntrpcContext.getBeanContainer().getOrCreateBean(DogInterface.class);
        Assert.assertTrue(bean instanceof DogInterface);
        DogInterface dogInterface = (DogInterface) bean;
        Assert.assertEquals(dogInterface.getType(), "dog type");
        Assert.assertEquals(dogInterface.eat(), "dog eat");

        // 有频率控制
        GenericApplicationContext rateLimitingApplicationContext = new GenericApplicationContext();
        MockEnvironment rateLimitingEnvironment = new MockEnvironment();
        rateLimitingApplicationContext.setEnvironment(rateLimitingEnvironment);
        rateLimitingApplicationContext.refresh();
        AntrpcContext rateLimitingAntrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        rateLimitingAntrpcContext.init();
        rateLimitingAntrpcContext
                .getNodeHostContainer()
                .add(DogInterface.class.getName(), getNodeHostEntity(serverRpcPort));
        Object rateLimitingBean =
                rateLimitingAntrpcContext.getBeanContainer().getOrCreateBean(DogInterface.class);
        Assert.assertTrue(rateLimitingBean instanceof DogInterface);
        DogInterface rateLimitingDogInterface = (DogInterface) rateLimitingBean;
        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                Assert.assertEquals(rateLimitingDogInterface.eat(), "dog eat");
            } else {
                Assert.assertNull(rateLimitingDogInterface.eat());
            }
            Assert.assertEquals(rateLimitingDogInterface.getType(), "dog type");
        }
        WaitUtil.wait(2, 1);
        Assert.assertEquals(rateLimitingDogInterface.eat(), "dog eat");
        Assert.assertEquals(rateLimitingDogInterface.getType(), "dog type");

        // 有频率控制与默认实现
        GenericApplicationContext defaultRespApplicationContext = new GenericApplicationContext();
        MockEnvironment defaultRespEnv = new MockEnvironment();
        defaultRespApplicationContext.setEnvironment(defaultRespEnv);
        defaultRespApplicationContext.refresh();
        defaultRespApplicationContext
                .getBeanFactory()
                .registerSingleton(OnFailProcessor.class.getName(), new OnFailProcessor());
        defaultRespApplicationContext
                .getBeanFactory()
                .registerSingleton(DogInterface.class.getName(), new DefaultMyDog());
        AntrpcContext defaultRespAntrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        defaultRespApplicationContext
                .getBeanFactory()
                .registerSingleton(IAntrpcContext.class.getName(), defaultRespAntrpcContext);
        defaultRespAntrpcContext.init();
        OnFailProcessor onFailProcessor =
                defaultRespApplicationContext.getBean(OnFailProcessor.class);
        onFailProcessor.setApplicationContext(defaultRespApplicationContext);
        onFailProcessor.postProcessBeforeInitialization(
                defaultRespApplicationContext.getBean(DogInterface.class.getName()),
                DogInterface.class.getName());
        defaultRespAntrpcContext
                .getNodeHostContainer()
                .add(DogInterface.class.getName(), getNodeHostEntity(serverRpcPort));
        Object defaultRespBean =
                defaultRespAntrpcContext.getBeanContainer().getOrCreateBean(DogInterface.class);
        Assert.assertTrue(defaultRespBean instanceof DogInterface);
        DogInterface defaultRespDogInterface = (DogInterface) defaultRespBean;
        for (int i = 0; i < 100; i++) {
            if (i < 10) {
                Assert.assertEquals(defaultRespDogInterface.eat(), "dog eat");
            } else {
                Assert.assertEquals(defaultRespDogInterface.eat(), "default dog eat");
            }
            Assert.assertEquals(defaultRespDogInterface.getType(), "dog type");
        }
        WaitUtil.wait(2, 1);
        Assert.assertEquals(defaultRespDogInterface.eat(), "dog eat");
    }

    private NodeHostEntity getNodeHostEntity(int serverRpcPort) {
        NodeHostEntity nodeHostEntity = new NodeHostEntity();
        nodeHostEntity.setIp("localhost");
        nodeHostEntity.setPort(serverRpcPort);
        nodeHostEntity.setRegisterTs(System.currentTimeMillis());
        nodeHostEntity.setRefreshTs(System.currentTimeMillis());
        nodeHostEntity.setClassName(DogInterface.class.getName());
        nodeHostEntity.setMethodStrs(Lists.newArrayList("getType()", "eat()"));
        return nodeHostEntity;
    }

    @Test
    public void testCatInterfaceToStringMethodIntercept() throws Exception {
        AntrpcContext antrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setZkIps("localhost:2181");
        antrpcContext.init();
        Object bean = antrpcContext.getBeanContainer().getOrCreateBean(CatInterface.class);
        Assert.assertTrue(bean.toString().contains("CatInterface"));
        Assert.assertTrue(bean.toString().contains("EnhancerByCGLIB"));
    }

    @Test
    public void testCatInterfaceGetNameMethodIntercept() throws Exception {
        int serverPort = RandomUtils.nextInt(5000, 9000);
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        applicationContext.setEnvironment(environment);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        applicationContext.refresh();
        beanFactory.registerSingleton(CatInterface.class.getName(), new MyCat());
        AntrpcContext serverAntrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        Configuration serverConfiguration = (Configuration) serverAntrpcContext.getConfiguration();
        serverConfiguration.setZkIps("localhost:2181");
        serverConfiguration.setPort(serverPort);
        serverAntrpcContext.init();
        serverAntrpcContext.startServer();
        serverAntrpcContext.setRpcRequestBeanInvoker(new RpcRequestBeanInvoker(beanFactory));

        AntrpcContext antrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setZkIps("localhost:2181");
        configuration.setStartServer(false);
        configuration.setRpcClientsConfig(new RpcClientsConfig());
        antrpcContext.init();
        NodeHostEntity nodeHostEntity = new NodeHostEntity();
        nodeHostEntity.setIp("localhost");
        nodeHostEntity.setPort(serverPort);
        nodeHostEntity.setClassName(CatInterface.class.getName());
        nodeHostEntity.setMethodStrs(Lists.newArrayList("getName()", "getType()"));
        nodeHostEntity.setRefreshTs(System.currentTimeMillis());
        nodeHostEntity.setRegisterTs(System.currentTimeMillis());
        antrpcContext.getNodeHostContainer().add(CatInterface.class.getName(), nodeHostEntity);
        Object bean = antrpcContext.getBeanContainer().getOrCreateBean(CatInterface.class);
        Assert.assertNotNull(bean);
        Assert.assertTrue(bean instanceof CatInterface);
        CatInterface catInterface = (CatInterface) bean;
        Assert.assertEquals(catInterface.getName(), "MaoMao");
        Assert.assertEquals(catInterface.getType(), "MyCat");
    }

    interface AnimalInterface {
        String getType();
    }

    @RpcService
    interface DogInterface extends AnimalInterface {
        @RpcMethod(rateLimitEnable = true, limit = 10, durationInSeconds = 2)
        String eat();
    }

    interface CatInterface extends AnimalInterface {
        String getName();
    }

    @OnRpcFail(clazz = DogInterface.class)
    public static class DefaultMyDog implements DogInterface {

        @Override
        public String getType() {
            return "default dog type";
        }

        @Override
        public String eat() {
            return "default dog eat";
        }
    }

    public static class MyDog implements DogInterface {

        @Override
        public String getType() {
            return "dog type";
        }

        @Override
        public String eat() {
            return "dog eat";
        }
    }

    public static class MyCat implements CatInterface {

        @Override
        public String getType() {
            return "MyCat";
        }

        @Override
        public String getName() {
            return "MaoMao";
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
