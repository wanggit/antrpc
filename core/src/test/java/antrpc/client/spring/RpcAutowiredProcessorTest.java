package antrpc.client.spring;

import antrpc.AntrpcContext;
import antrpc.client.monitor.RpcCallLogHolder;
import antrpc.commons.annotations.RpcAutowired;
import antrpc.commons.breaker.CircuitBreaker;
import antrpc.commons.config.Configuration;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

public class RpcAutowiredProcessorTest {

    @Test
    public void testRpcAutowiredProcessor() throws Exception {
        RpcAutowiredProcessor rpcAutowiredProcessor = new RpcAutowiredProcessor();
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment
                .withProperty("antrpc.zk-servers", "localhost:2181")
                .withProperty("server.port", String.valueOf(RandomUtils.nextInt(5000, 9000)))
                .withProperty("antrpc.port", String.valueOf(RandomUtils.nextInt(5000, 9000)));
        applicationContext.setEnvironment(environment);
        applicationContext.refresh();
        AntrpcContext antrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(RandomUtils.nextInt(1000, 9000));
        antrpcContext.init();
        applicationContext
                .getBeanFactory()
                .registerSingleton(AntrpcContext.class.getName(), antrpcContext);
        rpcAutowiredProcessor.setApplicationContext(applicationContext);
        TestAutowired testAutowired = new TestAutowired();
        Object processedObj =
                rpcAutowiredProcessor.postProcessBeforeInitialization(
                        testAutowired, TestAutowired.class.getName());
        Assert.assertNotNull(processedObj);
        Assert.assertTrue(processedObj instanceof TestAutowired);
        TestAutowired test = (TestAutowired) processedObj;
        Assert.assertNotNull(test.getTestAutowiredInterface());
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
