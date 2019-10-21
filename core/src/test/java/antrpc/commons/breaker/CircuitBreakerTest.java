package antrpc.commons.breaker;

import antrpc.commons.config.CircuitBreakerConfig;
import antrpc.commons.bean.RpcCallLog;
import antrpc.commons.bean.RpcRequestBean;
import antrpc.commons.config.Configuration;
import antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CircuitBreakerTest {

    private RpcCallLog createRpcCallLog(){
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

    private Map<String, CircuitBreakerConfig> setCircuitBreakerConfigToMap(String className, int threshold, int checkIntervalSeconds){
        Map<String, CircuitBreakerConfig> interfaceConfigs = new HashMap<>();
        interfaceConfigs.put(className, new CircuitBreakerConfig(threshold,checkIntervalSeconds));
        return interfaceConfigs;
    }

    @Test
    public void testInitCircuitBreaker(){
        CircuitBreaker breaker = new CircuitBreaker();
        Configuration configuration = new Configuration();
        configuration.setGlobalBreakerConfig(new CircuitBreakerConfig(10, 10));
        Map<String, CircuitBreakerConfig> interfaceConfigs = new HashMap<>();
        interfaceConfigs.put(CircuitBreakerTest.class.getName(), new CircuitBreakerConfig(10, 2));
        interfaceConfigs.put(CircuitBreaker.class.getName(), new CircuitBreakerConfig(2, 2));
        interfaceConfigs.put(RpcRequestBean.class.getName(), new CircuitBreakerConfig(5, 2));
        configuration.setInterfaceBreakerConfigs(interfaceConfigs);
        breaker.init(configuration);
    }

    @Test
    public void testNotCircuitBreakerWrongConfig(){
        CircuitBreaker breaker = new CircuitBreaker();
        Configuration configuration = new Configuration();
        RpcCallLog rpcCallLog = createRpcCallLog();
        rpcCallLog.setClassName(RpcCallLog.class.getName());
        configuration.setInterfaceBreakerConfigs(setCircuitBreakerConfigToMap(rpcCallLog.getClassName(), 0, 0));
        try {
            breaker.init(configuration);
        }catch (Exception e){
            Assert.assertTrue(e.getMessage().contains("Circuit Breaker must be greater than 0"));
        }
        configuration.setInterfaceBreakerConfigs(setCircuitBreakerConfigToMap(rpcCallLog.getClassName(), -1, 10));
        try {
            breaker.init(configuration);
        }catch (Exception e){
            Assert.assertTrue(e.getMessage().contains("Circuit Breaker must be greater than 0"));
        }

    }

    @Test
    public void testNotCircuitBreaker(){
        CircuitBreaker breaker = new CircuitBreaker();
        breaker.init(new Configuration());
        RpcCallLog rpcCallLog = createRpcCallLog();
        rpcCallLog.setClassName(CircuitBreakerTest.class.getName());
        for (int i = 0; i < 50; i++) {
            Assert.assertTrue(breaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        }
    }

    @Test
    public void testFireCircuitBreaker() throws InterruptedException {
        CircuitBreaker circuitBreaker = new CircuitBreaker();
        Configuration configuration = new Configuration();
        RpcCallLog rpcCallLog = createRpcCallLog();
        rpcCallLog.setClassName(CircuitBreakerTest.class.getName());
        configuration.setInterfaceBreakerConfigs(setCircuitBreakerConfigToMap(rpcCallLog.getClassName(), 5, 10));
        circuitBreaker.init(configuration);
        circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey());
        for (int i = 0; i < 50; i++) {
            if (i < 10){
                circuitBreaker.increament(rpcCallLog.getCallLogKey());
            }
            if (i < 5){
                Assert.assertTrue(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
            }
            if (i >= 5 && i < 15){
                Assert.assertFalse(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
            }
            if (i >= 15){
                Assert.assertTrue(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
            }
            System.out.println(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()) + " --> " + i);
            WaitUtil.wait(1, 1, false);
        }
    }

    @Test
    public void testCheckState() throws Exception {
        CircuitBreaker circuitBreaker = new CircuitBreaker();
        Configuration configuration = new Configuration();
        RpcCallLog rpcCallLog = createRpcCallLog();
        configuration.setInterfaceBreakerConfigs(setCircuitBreakerConfigToMap(CircuitBreaker.class.getName(), 10, 10));
        circuitBreaker.init(configuration);
        Assert.assertTrue(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        Assert.assertTrue(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));

        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        WaitUtil.wait(5, 2);
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        circuitBreaker.increament(rpcCallLog.getCallLogKey());
        Assert.assertTrue(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
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
        Assert.assertFalse(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
        WaitUtil.wait(10, 2);
        Assert.assertTrue(circuitBreaker.checkState(rpcCallLog.getClassName(), rpcCallLog.getCallLogKey()));
    }

}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme