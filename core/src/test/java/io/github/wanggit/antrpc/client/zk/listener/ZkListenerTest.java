package io.github.wanggit.antrpc.client.zk.listener;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.BeansToSpringContextUtil;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.Assert;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.util.ReflectionUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZkListenerTest {

    /*@Test*/
    public void testZkListener() throws Exception {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment
                .withProperty("server.port", String.valueOf(RandomUtils.nextInt(2000, 9000)))
                .withProperty("antrpc.port", String.valueOf(RandomUtils.nextInt(2000, 9000)))
                .withProperty("spring.application.name", "test");
        applicationContext.setEnvironment(environment);
        applicationContext.refresh();
        BeansToSpringContextUtil.toSpringContext(applicationContext);
        AntrpcContext antrpcContext = new AntrpcContext(new Configuration());
        applicationContext
                .getBeanFactory()
                .registerSingleton(IAntrpcContext.class.getName(), antrpcContext);
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(RandomUtils.nextInt(5000, 9000));
        configuration.setEnvironment(environment);
        antrpcContext.init(applicationContext);

        RegisterBean.IpNodeDataBean ipNodeDataBean = new RegisterBean.IpNodeDataBean();
        ipNodeDataBean.setAppName("testApp");
        ipNodeDataBean.setHttpPort(RandomUtils.nextInt());
        ipNodeDataBean.setRpcPort(RandomUtils.nextInt());
        ipNodeDataBean.setTs(System.currentTimeMillis());
        String randomNodePort = String.valueOf(RandomUtils.nextInt(2000, 9000));
        antrpcContext
                .getZkNodeBuilder()
                .remoteCreateZkNode(
                        "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:" + randomNodePort,
                        JSONObject.toJSONString(ipNodeDataBean).getBytes(Charset.forName("UTF-8")),
                        CreateMode.PERSISTENT);

        RegisterBean.InterfaceNodeDataBean interfaceNodeDataBean =
                new RegisterBean.InterfaceNodeDataBean();
        interfaceNodeDataBean.setTs(System.currentTimeMillis());
        RegisterBean.RegisterBeanMethod testMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(AInterface.class, "test"));
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = new HashMap<>();
        methodMap.put(testMethod.toString(), testMethod);
        interfaceNodeDataBean.setMethodMap(methodMap);
        interfaceNodeDataBean.setMethods(Lists.newArrayList(testMethod.toString()));
        String path =
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/127.0.0.1:"
                        + randomNodePort
                        + "/"
                        + AInterface.class.getName();
        antrpcContext
                .getZkNodeBuilder()
                .remoteCreateZkNode(
                        path,
                        JSONObject.toJSONString(interfaceNodeDataBean)
                                .getBytes(Charset.forName("UTF-8")),
                        CreateMode.EPHEMERAL);
        WaitUtil.wait(2, 1);
        List<NodeHostEntity> hostEntities =
                antrpcContext
                        .getNodeHostContainer()
                        .getHostEntities(AInterface.class.getName(), testMethod.toString());
        Assert.assertEquals(1, hostEntities.size());
        for (NodeHostEntity hostEntity : hostEntities) {
            Assert.assertEquals(hostEntity.getHostInfo(), "127.0.0.1:" + randomNodePort);
        }

        Long refreshTs = hostEntities.get(0).getRefreshTs();
        Long registerTs = hostEntities.get(0).getRegisterTs();
        antrpcContext
                .getZkNodeBuilder()
                .remoteCreateZkNode(
                        path,
                        JSONObject.toJSONString(interfaceNodeDataBean)
                                .getBytes(Charset.forName("UTF-8")),
                        CreateMode.EPHEMERAL);
        WaitUtil.wait(1, 1);
        List<NodeHostEntity> entities =
                antrpcContext
                        .getNodeHostContainer()
                        .getHostEntities(AInterface.class.getName(), testMethod.toString());
        Assert.assertEquals(entities.size(), 1);
        for (NodeHostEntity hostEntity : entities) {
            Assert.assertEquals(hostEntity.getHostInfo(), "127.0.0.1:" + randomNodePort);
        }
        Assert.assertEquals(registerTs.longValue(), entities.get(0).getRegisterTs().longValue());
        Assert.assertTrue(entities.get(0).getRefreshTs() > refreshTs);

        antrpcContext.getZkClient().getCurator().delete().forPath(path);
        WaitUtil.wait(1, 1);
        hostEntities =
                antrpcContext
                        .getNodeHostContainer()
                        .getHostEntities(AInterface.class.getName(), testMethod.toString());
        Assert.assertTrue(null == hostEntities || hostEntities.isEmpty());

        antrpcContext
                .getZkClient()
                .getCurator()
                .delete()
                .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:" + randomNodePort);

        WaitUtil.wait(3, 1);
    }

    interface AInterface {
        void test();
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
