package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.BeansToSpringContextUtil;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RpcBeanContainerTest {

    @Test
    public void testGetOrCreateBean() throws Exception {
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment mockEnvironment =
                new MockEnvironment()
                        .withProperty("spring.application.name", "test")
                        .withProperty(
                                "server.port", String.valueOf(RandomUtils.nextInt(2000, 9999)))
                        .withProperty(
                                "antrpc.port", String.valueOf(RandomUtils.nextInt(2000, 9999)));
        genericApplicationContext.setEnvironment(mockEnvironment);
        genericApplicationContext.refresh();
        BeansToSpringContextUtil.toSpringContext(genericApplicationContext);
        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(RandomUtils.nextInt(2000, 9000));
        configuration.setEnvironment(mockEnvironment);
        antrpcContext.setOnFailProcessor(new OnFailProcessor());
        antrpcContext.setRegister(new ZkRegister());
        antrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        antrpcContext.init(genericApplicationContext);
        List<Object> objects = new ArrayList<>(1500);
        for (int i = 0; i < 1000; i++) {
            new Thread(
                            () ->
                                    objects.add(
                                            antrpcContext
                                                    .getBeanContainer()
                                                    .getOrCreateBean(TestInterface.class)))
                    .start();
        }
        WaitUtil.wait(5, 1);
        Set<String> set = new HashSet<>();
        for (Object object : objects) {
            set.add(object.toString());
        }
        Assert.assertEquals(1, set.size());
    }

    interface TestInterface {}
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
