package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.BeansToSpringContextUtil;
import io.github.wanggit.antrpc.commons.annotations.RpcAutowired;
import io.github.wanggit.antrpc.commons.config.Configuration;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

public class RpcAutowiredProcessorTest {

    @Test
    public void testRpcAutowiredProcessor() throws Exception {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment
                .withProperty("antrpc.zk-servers", "localhost:2181")
                .withProperty("server.port", String.valueOf(RandomUtils.nextInt(5000, 9000)))
                .withProperty("antrpc.port", String.valueOf(RandomUtils.nextInt(5000, 9000)))
                .withProperty("spring.application.name", "test");
        applicationContext.setEnvironment(environment);
        applicationContext.refresh();
        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(RandomUtils.nextInt(1000, 9000));
        configuration.setEnvironment(environment);
        BeansToSpringContextUtil.toSpringContext(applicationContext);
        applicationContext
                .getBeanFactory()
                .registerSingleton(AntrpcContext.class.getName(), antrpcContext);
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(TestAutowired.class);
        ((BeanDefinitionRegistry) applicationContext)
                .registerBeanDefinition(TestAutowired.class.getName(), genericBeanDefinition);
        antrpcContext.init(applicationContext);
    }

    interface TestAutowiredInterface {
        String getName();
    }

    public static class TestAutowired {
        @RpcAutowired private TestAutowiredInterface testAutowiredInterface;

        TestAutowiredInterface getTestAutowiredInterface() {
            return testAutowiredInterface;
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
