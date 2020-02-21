package io.github.wanggit.antrpc.client;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.config.Configuration;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

public class AntrpcContextTest {

    @Test
    public void beautyShutdown() {
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.application.name", "Test");
        genericApplicationContext.setEnvironment(environment);
        genericApplicationContext.refresh();
        GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
        genericBeanDefinition.setBeanClass(AImpl.class);
        ((BeanDefinitionRegistry) genericApplicationContext)
                .registerBeanDefinition(AImpl.class.getName(), genericBeanDefinition);

        Configuration configuration = new Configuration();
        configuration.setPort(RandomUtils.nextInt(3000, 9999));
        configuration.setEnvironment(environment);
        AntrpcContext antrpcContext = new AntrpcContext(configuration);
        antrpcContext.init(genericApplicationContext);
    }

    public interface AInterface {
        void test();
    }

    @RpcService
    public static class AImpl implements AInterface {

        @Override
        @RpcMethod
        public void test() {
            System.out.println("Test");
        }
    }
}
