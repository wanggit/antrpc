package io.github.wanggit.antrpc.client.zk.register;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.monitor.RpcCallLogHolder;
import io.github.wanggit.antrpc.client.spring.RpcBeanContainer;
import io.github.wanggit.antrpc.commons.annotations.RpcMethod;
import io.github.wanggit.antrpc.commons.annotations.RpcService;
import io.github.wanggit.antrpc.commons.breaker.CircuitBreaker;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.utils.ApplicationNameUtil;
import io.github.wanggit.antrpc.commons.utils.NetUtil;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;

public class ZkRegisterTest {

    @Test
    public void testRegister() throws Exception {
        Integer rpcPort = RandomUtils.nextInt(1000, 9999);
        Configuration configuration = new Configuration();
        configuration.setPort(rpcPort);
        IAntrpcContext antrpcContext =
                new AntrpcContext(
                        configuration,
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment
                .withProperty("spring.application.name", "test")
                .withProperty("antrpc.port", rpcPort.toString())
                .withProperty("server.port", String.valueOf(RandomUtils.nextInt(1000, 9999)));
        configuration.setEnvironment(environment);
        applicationContext.refresh();
        antrpcContext.init();
        ZkRegister register = new ZkRegister();
        applicationContext.getBeanFactory().registerSingleton(Register.class.getName(), register);
        applicationContext
                .getBeanFactory()
                .registerSingleton(IAntrpcContext.class.getName(), antrpcContext);
        register.setApplicationContext(applicationContext);

        String ipPath =
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/"
                        + NetUtil.getInstance().getLocalIp()
                        + (null == rpcPort ? "" : ":" + rpcPort);
        byte[] bytes = antrpcContext.getZkClient().getCurator().getData().forPath(ipPath);
        Assert.assertNotNull(bytes);
        String json = new String(bytes, Charset.forName("UTF-8"));
        RegisterBean.IpNodeDataBean ipNodeDataBean =
                JSONObject.parseObject(json, RegisterBean.IpNodeDataBean.class);
        Assert.assertNotNull(ipNodeDataBean);
        Assert.assertEquals(
                ApplicationNameUtil.getApplicationName(environment), ipNodeDataBean.getAppName());
        Assert.assertEquals(rpcPort, ipNodeDataBean.getRpcPort());

        AInterface aInterface = new AImpl();
        register.postProcessBeforeInitialization(aInterface, "aInterface");

        IZkRegisterHolder zkRegisterHolder = antrpcContext.getZkRegisterHolder();
        Field field =
                ReflectionUtils.findField(ZkRegisterHolder.class, "registerBeans", List.class);
        ReflectionUtils.makeAccessible(field);
        Object fieldValue = ReflectionUtils.getField(field, zkRegisterHolder);
        Assert.assertNotNull(fieldValue);
        Assert.assertTrue(fieldValue instanceof List);
        List list = (List) fieldValue;
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.get(0) instanceof RegisterBean);
        RegisterBean registerBean = (RegisterBean) list.get(0);
        Assert.assertEquals(registerBean.getClassName(), AInterface.class.getName());
        Assert.assertTrue(registerBean.getZookeeperFullPath().contains(AInterface.class.getName()));

        byte[] nodeData =
                antrpcContext
                        .getZkClient()
                        .getCurator()
                        .getData()
                        .forPath(registerBean.getZookeeperFullPath());
        Assert.assertNotNull(nodeData);
        json = new String(nodeData, Charset.forName("UTF-8"));
        RegisterBean.InterfaceNodeDataBean interfaceNodeDataBean =
                JSONObject.parseObject(json, RegisterBean.InterfaceNodeDataBean.class);
        Assert.assertNotNull(interfaceNodeDataBean);

        antrpcContext
                .getZkClient()
                .getCurator()
                .delete()
                .forPath(registerBean.getZookeeperFullPath());
    }

    @RpcService
    interface AInterface {
        @RpcMethod
        String getName();
    }

    static class AImpl implements AInterface {

        @Override
        public String getName() {
            return "AImpl";
        }
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
