package io.github.wanggit.antrpc.commons.breaker;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.commons.bean.RpcRequestBean;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerTest {

    private RpcCallLog createRpcCallLog() {
        String className = CircuitBreaker.class.getName();
        String methodName = "checkState";
        String ip = "localhost";
        Integer port = 8080;
        RpcCallLog rpcCallLog = new RpcCallLog();
        rpcCallLog.setClassName(className);
        rpcCallLog.setMethodName(methodName);
        rpcCallLog.setIp(ip);
        rpcCallLog.setPort(port);
        return rpcCallLog;
    }

    private Map<String, CircuitBreakerConfig> setCircuitBreakerConfigToMap(
            String className, int threshold, int checkIntervalSeconds) {
        Map<String, CircuitBreakerConfig> interfaceConfigs = new HashMap<>();
        interfaceConfigs.put(className, new CircuitBreakerConfig(threshold, checkIntervalSeconds));
        return interfaceConfigs;
    }

    @Test
    public void testInitCircuitBreaker() {
        Configuration configuration = new Configuration();
        configuration.setGlobalBreakerConfig(new CircuitBreakerConfig(10, 10));
        Map<String, CircuitBreakerConfig> interfaceConfigs = new HashMap<>();
        interfaceConfigs.put(CircuitBreakerTest.class.getName(), new CircuitBreakerConfig(10, 2));
        interfaceConfigs.put(CircuitBreaker.class.getName(), new CircuitBreakerConfig(2, 2));
        interfaceConfigs.put(RpcRequestBean.class.getName(), new CircuitBreakerConfig(5, 2));
        configuration.setInterfaceBreakerConfigs(interfaceConfigs);
        new CircuitBreaker(configuration);
    }

    @Test
    public void testNotCircuitBreakerWrongConfig() {
        Configuration configuration = new Configuration();
        RpcCallLog rpcCallLog = createRpcCallLog();
        rpcCallLog.setClassName(RpcCallLog.class.getName());
        configuration.setInterfaceBreakerConfigs(
                setCircuitBreakerConfigToMap(rpcCallLog.getClassName(), 0, 0));
        try {
            new CircuitBreaker(configuration);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Circuit Breaker must be greater than 0"));
        }
        configuration.setInterfaceBreakerConfigs(
                setCircuitBreakerConfigToMap(rpcCallLog.getClassName(), -1, 10));
        try {
            new CircuitBreaker(configuration);
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Circuit Breaker must be greater than 0"));
        }
    }

    @Test
    public void testNotCircuitBreaker() {
        CircuitBreaker breaker = new CircuitBreaker(new Configuration());
        RpcCallLog rpcCallLog = createRpcCallLog();
        rpcCallLog.setClassName(CircuitBreakerTest.class.getName());
        for (int i = 0; i < 50; i++) {
            Assert.assertTrue(
                    breaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        }
    }

    @Test
    public void testFireCircuitBreaker() throws InterruptedException {
        Configuration configuration = new Configuration();
        RpcCallLog rpcCallLog = createRpcCallLog();
        rpcCallLog.setClassName(CircuitBreakerTest.class.getName());
        configuration.setInterfaceBreakerConfigs(
                setCircuitBreakerConfigToMap(rpcCallLog.getClassName(), 5, 10));
        CircuitBreaker circuitBreaker = new CircuitBreaker(configuration);
        circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey());
        for (int i = 0; i < 50; i++) {
            if (i < 10) {
                circuitBreaker.increament(rpcCallLog.getCallLogKey());
            }
            if (i < 5) {
                Assert.assertTrue(
                        circuitBreaker.checkState(
                                rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
            }
            if (i >= 5 && i < 15) {
                Assert.assertFalse(
                        circuitBreaker.checkState(
                                rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
            }
            if (i >= 15) {
                Assert.assertTrue(
                        circuitBreaker.checkState(
                                rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
            }
            System.out.println(
                    circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey())
                            + " --> "
                            + i);
            WaitUtil.wait(1, 1, false);
        }
    }

    @Test
    public void testCheckState() throws Exception {
        Configuration configuration = new Configuration();
        RpcCallLog rpcCallLog = createRpcCallLog();
        configuration.setInterfaceBreakerConfigs(
                setCircuitBreakerConfigToMap(CircuitBreaker.class.getName(), 10, 10));
        CircuitBreaker circuitBreaker = new CircuitBreaker(configuration);
        Assert.assertTrue(
                circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        Assert.assertTrue(
                circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));

        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        WaitUtil.wait(5, 2);
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        Assert.assertTrue(
                circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        WaitUtil.wait(3, 2);

        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        System.out.println(circuitBreaker.increament(rpcCallLog.getCallLogKey()));
        Assert.assertFalse(
                circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        WaitUtil.wait(10, 2);
        Assert.assertTrue(
                circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
