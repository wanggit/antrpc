package io.github.wanggit.antrpc.commons.generic;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.bean.Host;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;

public class DefaultServiceProviderInvokerTest {

    @Test
    public void invoke() throws InterruptedException {
        int rpcPort = createSimpleAntrpcServer();
        // 未指定目标，也未指定zookeeper地址
        IServiceProviderInvoker serviceProviderInvoker = new DefaultServiceProviderInvoker();
        InvokeDTO invokeDTO = new InvokeDTO();
        invokeDTO.setInterfaceName(
                "io.github.wanggit.antrpc.commons.generic.DefaultServiceProviderInvokerTest$ZInterface");
        invokeDTO.setMethodName("sayHello");
        invokeDTO.setParameterTypeNames(
                Lists.newArrayList(
                        "io.github.wanggit.antrpc.commons.generic.DefaultServiceProviderInvokerTest.HelloDto"));
        Object[] args = new Object[1];
        Map<String, Object> data = new HashMap<>();
        data.put("id", 100);
        data.put("name", "wanggang");
        args[0] = data;
        invokeDTO.setArgumentValues(args);
        try {
            serviceProviderInvoker.invoke(invokeDTO);
        } catch (Exception e) {
            Assert.assertTrue(
                    e.getMessage().contains("Host and zkServers at least one is not empty"));
        }
        // 指定目标
        invokeDTO.setHost(new Host("127.0.0.1", rpcPort));
        Object result = serviceProviderInvoker.invoke(invokeDTO);
        System.out.println(result);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.toString().startsWith("Hello"));
        // 指定zookeeper注册中心
        invokeDTO.setHost(null);
        IServiceProviderInvoker hasZkServiceProviderInvoker =
                new DefaultServiceProviderInvoker("localhost:2181");
        result = hasZkServiceProviderInvoker.invoke(invokeDTO);
        System.out.println(result);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.toString().startsWith("Hello"));
        result = hasZkServiceProviderInvoker.invoke(invokeDTO);
        System.out.println(result);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.toString().startsWith("Hello"));
    }

    private int createSimpleAntrpcServer() throws InterruptedException {
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        int serverPort = RandomUtils.nextInt(3000, 50000);
        int rpcPort = RandomUtils.nextInt(3000, 50000);
        environment.setProperty("server.port", String.valueOf(serverPort));
        environment.setProperty("antrpc.port", String.valueOf(rpcPort));
        environment.setProperty("spring.application.name", "generic_server");
        genericApplicationContext.setEnvironment(environment);
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(ZImpl.class);
        genericApplicationContext.registerBeanDefinition(
                ZImpl.class.getName(), genericBeanDefinition);
        genericApplicationContext.refresh();
        Configuration configuration = new Configuration();
        configuration.setPort(rpcPort);
        configuration.setEnvironment(environment);
        AntrpcContext antrpcContext = new AntrpcContext(configuration);
        antrpcContext.init(genericApplicationContext);
        WaitUtil.wait(2, 1);
        return rpcPort;
    }

    public static class HelloDto {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public interface ZInterface {
        String sayHello(HelloDto helloDto);
    }

    @RpcService
    public static class ZImpl implements ZInterface {

        @Override
        @RpcMethod
        public String sayHello(HelloDto helloDto) {
            return "Hello " + JSONObject.toJSONString(helloDto);
        }
    }
}
