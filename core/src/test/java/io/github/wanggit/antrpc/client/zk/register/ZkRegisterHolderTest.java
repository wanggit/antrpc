package io.github.wanggit.antrpc.client.zk.register;

import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.BeansToSpringContextUtil;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ZkRegisterHolderTest {

    @Test
    public void testRegisterHolder() throws Exception {
        Configuration configuration = new Configuration();
        Integer rpcPort = RandomUtils.nextInt(1000, 9999);
        configuration.setPort(rpcPort);
        MockEnvironment environment = new MockEnvironment();
        environment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", String.valueOf(rpcPort))
                .withProperty("server.port", String.valueOf(RandomUtils.nextInt(1000, 9999)));
        configuration.setEnvironment(environment);

        GenericApplicationContext applicationContext = new GenericApplicationContext();
        applicationContext.setEnvironment(environment);
        applicationContext.refresh();
        BeansToSpringContextUtil.toSpringContext(applicationContext);
        AntrpcContext antrpcContext = new AntrpcContext(configuration);
        antrpcContext.init(applicationContext);
        applicationContext
                .getBeanFactory()
                .registerSingleton(IAntrpcContext.class.getName(), antrpcContext);

        ZkRegisterHolder zkRegisterHolder =
                new ZkRegisterHolder(
                        antrpcContext.getRegister(),
                        antrpcContext.getZkNodeBuilder(),
                        antrpcContext.getZkClient());
        WaitUtil.wait(70, 2);
        RegisterBean registerBean = new RegisterBean();
        registerBean.setClassName(AInterface.class.getName());
        List<RegisterBean.RegisterBeanMethod> methods = new ArrayList<>();
        RegisterBean.RegisterBeanMethod method =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(AImpl.class, "getName"));
        methods.add(method);
        registerBean.setMethods(methods);
        registerBean.setPort(rpcPort);
        zkRegisterHolder.add(registerBean);
        WaitUtil.wait(70, 2);
        byte[] bytes =
                antrpcContext
                        .getZkClient()
                        .getCurator()
                        .getData()
                        .forPath(registerBean.getZookeeperFullPath());
        Assert.assertNotNull(bytes);

        antrpcContext
                .getZkClient()
                .getCurator()
                .delete()
                .forPath(registerBean.getZookeeperFullPath());
        WaitUtil.wait(70, 2);
        bytes =
                antrpcContext
                        .getZkClient()
                        .getCurator()
                        .getData()
                        .forPath(registerBean.getZookeeperFullPath());
        Assert.assertNotNull(bytes);
    }

    interface AInterface {
        String getName();
    }

    static class AImpl implements AInterface {

        @Override
        public String getName() {
            return AImpl.class.getName();
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
