package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class RegisterBeanHelperTest {

    @Test
    public void testGetRegisterBeanMethod() throws Exception {
        Method method =
                ReflectionUtils.findMethod(ZkRegisterHolder.class, "add", RegisterBean.class);
        RegisterBean.RegisterBeanMethod registerBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(method);
        Assert.assertEquals(registerBeanMethod.getMethodName(), "add");
        Assert.assertFalse(registerBeanMethod.getParameterTypeNames().isEmpty());
        Assert.assertEquals(
                registerBeanMethod.getParameterTypeNames().get(0), RegisterBean.class.getName());

        Method beanMethod =
                ReflectionUtils.findMethod(
                        RegisterBeanHelperTest.class, "testGetRegisterBeanMethod");
        registerBeanMethod = RegisterBeanHelper.getRegisterBeanMethod(beanMethod);
        Assert.assertEquals(registerBeanMethod.getMethodName(), "testGetRegisterBeanMethod");
        Assert.assertTrue(registerBeanMethod.getParameterTypeNames().isEmpty());

        Method methodA = ReflectionUtils.findMethod(AService.class, "methodA");
        registerBeanMethod = RegisterBeanHelper.getRegisterBeanMethod(methodA);
        Assert.assertNotNull(registerBeanMethod);
        Assert.assertEquals(10, registerBeanMethod.getLimit());
        Assert.assertEquals(2, registerBeanMethod.getDurationInSeconds());

        Method methodB = ReflectionUtils.findMethod(AService.class, "methodB");
        registerBeanMethod = RegisterBeanHelper.getRegisterBeanMethod(methodB);
        Assert.assertNotNull(registerBeanMethod);
        Assert.assertEquals(0, registerBeanMethod.getLimit());
        Assert.assertEquals(0, registerBeanMethod.getDurationInSeconds());

        Method methodC = ReflectionUtils.findMethod(AService.class, "methodC");
        final RegisterBean.RegisterBeanMethod methodCBeanMethod =
                RegisterBeanHelper.getRegisterBeanMethod(methodC);
        Assert.assertNotNull(methodCBeanMethod);
        Assert.assertEquals(0, methodCBeanMethod.getLimit());
        Assert.assertEquals(0, methodCBeanMethod.getDurationInSeconds());

        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    RegisterBean.RegisterBeanMethod otherBeanMethod =
                            RegisterBeanHelper.getRegisterBeanMethod(methodC);
                    Assert.assertEquals(otherBeanMethod, methodCBeanMethod);
                }
            }.start();
        }
        WaitUtil.wait(3, 1);
    }

    @RpcService
    interface AService {
        @RpcMethod(rateLimitEnable = true, limit = 10, durationInSeconds = 2)
        void methodA();

        @RpcMethod
        void methodB();

        @RpcMethod(limit = 20, durationInSeconds = 3)
        void methodC();
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
