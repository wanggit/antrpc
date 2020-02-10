package io.github.wanggit.antrpc.client.rate;

import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public class RateLimitingTest {

    @Test
    public void testAllowAccess() throws InterruptedException {
        RateLimiting rateLimiting = new RateLimiting();
        RegisterBean.RegisterBeanMethod methodARegisterBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(RInterface.class, "methodA"));
        NodeHostEntity hostEntity = new NodeHostEntity();
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = new HashMap<>();
        methodMap.put(methodARegisterBeanMethod.toString(), methodARegisterBeanMethod);
        hostEntity.setMethodMap(methodMap);
        for (int i = 0; i < 1000; i++) {
            Assert.assertTrue(rateLimiting.allowAccess(methodARegisterBeanMethod, hostEntity));
        }

        RegisterBean.RegisterBeanMethod methodBRegisterBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(RInterface.class, "methodB"));
        hostEntity = new NodeHostEntity();
        methodMap = new HashMap<>();
        methodMap.put(methodBRegisterBeanMethod.toString(), methodBRegisterBeanMethod);
        hostEntity.setMethodMap(methodMap);
        for (int i = 0; i < 100; i++) {
            if (i < methodBRegisterBeanMethod.getLimit()) {
                Assert.assertTrue(rateLimiting.allowAccess(methodBRegisterBeanMethod, hostEntity));
            } else {
                Assert.assertFalse(rateLimiting.allowAccess(methodBRegisterBeanMethod, hostEntity));
            }
        }
        WaitUtil.wait(2, 1);
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(rateLimiting.allowAccess(methodBRegisterBeanMethod, hostEntity));
        }
        WaitUtil.wait(1, 1);
        Assert.assertFalse(rateLimiting.allowAccess(methodBRegisterBeanMethod, hostEntity));
        WaitUtil.wait(3, 1);
        Assert.assertTrue(rateLimiting.allowAccess(methodBRegisterBeanMethod, hostEntity));
    }

    @RpcService
    interface RInterface {
        @RpcMethod
        void methodA();

        @RpcMethod(rateLimitEnable = true, durationInSeconds = 2, limit = 10)
        void methodB();
    }
}
