package io.github.wanggit.antrpc.client.spring;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.client.monitor.RpcCallLogHolder;
import io.github.wanggit.antrpc.client.monitor.RpcMonitorApi;
import io.github.wanggit.antrpc.commons.breaker.CircuitBreaker;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RpcBeanContainerTest {

    @Test
    public void testGetOrCreateBean() throws Exception {
        BeanContainer beanContainer = new RpcBeanContainer();
        AntrpcContext antrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        beanContainer,
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(RandomUtils.nextInt(2000, 9000));
        antrpcContext.init();
        List<Object> objects = new ArrayList<>(1500);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> objects.add(beanContainer.getOrCreateBean(RpcMonitorApi.class)))
                    .start();
        }
        WaitUtil.wait(5, 1);
        Set<String> set = new HashSet<>();
        for (Object object : objects) {
            set.add(object.toString());
        }
        Assert.assertEquals(1, set.size());
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
