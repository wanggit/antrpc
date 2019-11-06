package io.github.wanggit.antrpc.client.rate;

import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class RateLimitingTest {

    @Test
    public void testAllowAccess() throws InterruptedException {
        RateLimiting rateLimiting = new RateLimiting();
        RegisterBean.RegisterBeanMethod methodARegisterBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(RInterface.class, "methodA"));
        for (int i = 0; i < 1000; i++) {
            Assert.assertTrue(rateLimiting.allowAccess(methodARegisterBeanMethod));
        }

        RegisterBean.RegisterBeanMethod methodBRegisterBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(RInterface.class, "methodB"));
        for (int i = 0; i < 100; i++) {
            if (i < methodBRegisterBeanMethod.getLimit()) {
                Assert.assertTrue(rateLimiting.allowAccess(methodBRegisterBeanMethod));
            } else {
                Assert.assertFalse(rateLimiting.allowAccess(methodBRegisterBeanMethod));
            }
        }
        WaitUtil.wait(2, 1);
        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(rateLimiting.allowAccess(methodBRegisterBeanMethod));
        }
        WaitUtil.wait(1, 1);
        Assert.assertFalse(rateLimiting.allowAccess(methodBRegisterBeanMethod));
        WaitUtil.wait(3, 1);
        Assert.assertTrue(rateLimiting.allowAccess(methodBRegisterBeanMethod));
    }

    @RpcService
    interface RInterface {
        @RpcMethod
        void methodA();

        @RpcMethod(rateLimitEnable = true, durationInSeconds = 2, limit = 10)
        void methodB();
    }
}
