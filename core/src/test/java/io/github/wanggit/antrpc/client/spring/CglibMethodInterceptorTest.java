package io.github.wanggit.antrpc.client.spring;

import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.annotations.OnRpcFail;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.config.RpcClientsConfig;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CglibMethodInterceptorTest {

    @Test
    public void testRateLimitingAndDefaultResponse() throws InterruptedException {
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment mockEnvironment = new MockEnvironment();
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        int serverPort = RandomUtils.nextInt(2000, 9999);
        mockEnvironment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("server.port", String.valueOf(serverPort));
        genericApplicationContext.setEnvironment(mockEnvironment);
        genericApplicationContext.refresh();
        setBeansToSpringContext(genericApplicationContext);
        genericApplicationContext
                .getBeanFactory()
                .registerSingleton(DogInterface.class.getName(), new MyDog());
        AntrpcContext serverAntrpcContext = new AntrpcContext(new Configuration());
        Configuration configuration = (Configuration) serverAntrpcContext.getConfiguration();
        configuration.setEnvironment(mockEnvironment);
        configuration.setPort(serverPort);
        configuration.setZkIps("localhost:2181");
        /*genericApplicationContext
        .getBeanFactory()
        .registerSingleton(IAntrpcContext.class.getName(), serverAntrpcContext);*/
        IRegister register = genericApplicationContext.getBean(IRegister.class);
        ZkRegister zkRegister = (ZkRegister) register;
        zkRegister.checkHasRpcService(genericApplicationContext.getBean(DogInterface.class));
        serverAntrpcContext.setOnFailProcessor(new OnFailProcessor());
        serverAntrpcContext.setRegister(new ZkRegister());
        serverAntrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        serverAntrpcContext.init(genericApplicationContext);

        // 无频率控制
        GenericApplicationContext clientApplicationContext = new GenericApplicationContext();
        MockEnvironment clientEnvironment = new MockEnvironment();
        int clientRpcPort = RandomUtils.nextInt(2000, 9999);
        int clientServerPort = RandomUtils.nextInt(2000, 9999);
        clientEnvironment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(clientRpcPort))
                .withProperty("server.port", String.valueOf(clientServerPort));
        clientApplicationContext.setEnvironment(clientEnvironment);
        clientApplicationContext.refresh();
        setBeansToSpringContext(clientApplicationContext);
        AntrpcContext clientAntrpcContext = new AntrpcContext(new Configuration());
        Configuration clientConfiguration = (Configuration) clientAntrpcContext.getConfiguration();
        clientConfiguration.setPort(rpcPort);
        clientConfiguration.setEnvironment(clientEnvironment);
        clientAntrpcContext.setOnFailProcessor(new OnFailProcessor());
        clientAntrpcContext.setRegister(new ZkRegister());
        clientAntrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        clientAntrpcContext.init(clientApplicationContext);
        NodeHostEntity nodeHostEntity = getNodeHostEntity(serverPort);
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
        int rateRpcPort = RandomUtils.nextInt(2000, 9999);
        int rateServerPort = RandomUtils.nextInt(2000, 9999);
        rateLimitingEnvironment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(rateRpcPort))
                .withProperty("server.port", String.valueOf(rateServerPort));
        rateLimitingApplicationContext.setEnvironment(rateLimitingEnvironment);
        rateLimitingApplicationContext.refresh();
        setBeansToSpringContext(rateLimitingApplicationContext);
        AntrpcContext rateLimitingAntrpcContext = new AntrpcContext(new Configuration());
        Configuration rateConfiguration =
                (Configuration) rateLimitingAntrpcContext.getConfiguration();
        rateConfiguration.setPort(rateRpcPort);
        rateConfiguration.setEnvironment(rateLimitingEnvironment);
        rateLimitingAntrpcContext.setOnFailProcessor(new OnFailProcessor());
        rateLimitingAntrpcContext.setRegister(new ZkRegister());
        rateLimitingAntrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        rateLimitingAntrpcContext.init(rateLimitingApplicationContext);
        rateLimitingAntrpcContext
                .getNodeHostContainer()
                .add(DogInterface.class.getName(), getNodeHostEntity(serverPort));
        List<NodeHostEntity> hostEntities =
                rateLimitingAntrpcContext
                        .getNodeHostContainer()
                        .getHostEntities(DogInterface.class.getName());

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
        int defaultRespRpcPort = RandomUtils.nextInt(2000, 9999);
        int defaultRespServerPort = RandomUtils.nextInt(2000, 9999);
        defaultRespEnv
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(defaultRespRpcPort))
                .withProperty("server.port", String.valueOf(defaultRespServerPort));
        defaultRespApplicationContext.setEnvironment(defaultRespEnv);
        defaultRespApplicationContext.refresh();
        setBeansToSpringContext(defaultRespApplicationContext);
        defaultRespApplicationContext
                .getBeanFactory()
                .registerSingleton(DogInterface.class.getName(), new DefaultMyDog());
        AntrpcContext defaultRespAntrpcContext = new AntrpcContext(new Configuration());
        Configuration defaultRespConfiguration =
                (Configuration) defaultRespAntrpcContext.getConfiguration();
        defaultRespConfiguration.setEnvironment(defaultRespEnv);
        defaultRespConfiguration.setPort(defaultRespRpcPort);
        /*defaultRespApplicationContext
        .getBeanFactory()
        .registerSingleton(IAntrpcContext.class.getName(), defaultRespAntrpcContext);*/
        OnFailProcessor onFailProcessor =
                defaultRespApplicationContext.getBean(OnFailProcessor.class);
        onFailProcessor.checkHasOnRpcFail(
                defaultRespApplicationContext.getBean(DogInterface.class.getName()));
        defaultRespAntrpcContext.setOnFailProcessor(onFailProcessor);
        defaultRespAntrpcContext.setRegister(new ZkRegister());
        defaultRespAntrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        defaultRespAntrpcContext.init(defaultRespApplicationContext);
        defaultRespAntrpcContext
                .getNodeHostContainer()
                .add(DogInterface.class.getName(), getNodeHostEntity(serverPort));
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
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = new HashMap<>();
        Method eat = ReflectionUtils.findMethod(DogInterface.class, "eat");
        ReflectionUtils.makeAccessible(eat);
        RegisterBean.RegisterBeanMethod eatMethod = RegisterBeanHelper.getRegisterBeanMethod(eat);
        methodMap.put(eatMethod.toString(), eatMethod);
        Method getType = ReflectionUtils.findMethod(DogInterface.class, "getType");
        ReflectionUtils.makeAccessible(getType);
        RegisterBean.RegisterBeanMethod getTypeMethod =
                RegisterBeanHelper.getRegisterBeanMethod(getType);
        methodMap.put(getTypeMethod.toString(), getTypeMethod);
        nodeHostEntity.setMethodStrs(
                Lists.newArrayList(eatMethod.toString(), getTypeMethod.toString()));
        nodeHostEntity.setMethodMap(methodMap);
        return nodeHostEntity;
    }

    @Test
    public void testCatInterfaceToStringMethodIntercept() throws Exception {
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        int serverPort = RandomUtils.nextInt(2000, 9999);
        environment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("server.port", String.valueOf(serverPort));
        genericApplicationContext.setEnvironment(environment);
        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setEnvironment(environment);
        configuration.setZkIps("localhost:2181");
        configuration.setPort(rpcPort);
        genericApplicationContext.refresh();
        setBeansToSpringContext(genericApplicationContext);
        antrpcContext.setOnFailProcessor(new OnFailProcessor());
        antrpcContext.setRegister(new ZkRegister());
        antrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        antrpcContext.init(genericApplicationContext);
        Object bean = antrpcContext.getBeanContainer().getOrCreateBean(CatInterface.class);
        Assert.assertTrue(bean.toString().contains("CatInterface"));
        Assert.assertTrue(bean.toString().contains("EnhancerByCGLIB"));
    }

    @Test
    public void testCatInterfaceGetNameMethodIntercept() throws Exception {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        int rpcPort = RandomUtils.nextInt(2000, 9999);
        int serverPort = RandomUtils.nextInt(2000, 9999);
        environment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("server.port", String.valueOf(serverPort));
        applicationContext.setEnvironment(environment);
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        applicationContext.refresh();
        beanFactory.registerSingleton(CatInterface.class.getName(), new MyCat());
        AntrpcContext serverAntrpcContext = new AntrpcContext(new Configuration());
        Configuration serverConfiguration = (Configuration) serverAntrpcContext.getConfiguration();
        serverConfiguration.setZkIps("localhost:2181");
        serverConfiguration.setPort(serverPort);
        serverConfiguration.setEnvironment(environment);
        setBeansToSpringContext(applicationContext);
        serverAntrpcContext.setOnFailProcessor(new OnFailProcessor());
        serverAntrpcContext.setRegister(new ZkRegister());
        serverAntrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        serverAntrpcContext.init(applicationContext);

        GenericApplicationContext clientApplicationContext = new GenericApplicationContext();
        MockEnvironment clientEnvironment = new MockEnvironment();
        clientEnvironment.withProperty("spring.application.name", "test");
        clientApplicationContext.setEnvironment(clientEnvironment);
        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setZkIps("localhost:2181");
        configuration.setStartServer(false);
        configuration.setRpcClientsConfig(new RpcClientsConfig());
        configuration.setEnvironment(clientEnvironment);
        clientApplicationContext.refresh();
        setBeansToSpringContext(clientApplicationContext);
        antrpcContext.setOnFailProcessor(new OnFailProcessor());
        antrpcContext.setRegister(new ZkRegister());
        antrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        antrpcContext.init(clientApplicationContext);
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

    private void setBeansToSpringContext(GenericApplicationContext applicationContext) {
        applicationContext
                .getBeanFactory()
                .registerSingleton(IRegister.class.getName(), new ZkRegister());
        applicationContext
                .getBeanFactory()
                .registerSingleton(IOnFailProcessor.class.getName(), new OnFailProcessor());
        applicationContext
                .getBeanFactory()
                .registerSingleton(
                        IRpcAutowiredProcessor.class.getName(), new RpcAutowiredProcessor());
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
