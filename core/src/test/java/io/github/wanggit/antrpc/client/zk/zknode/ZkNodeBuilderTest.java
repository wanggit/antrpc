package io.github.wanggit.antrpc.client.zk.zknode;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.BeansToSpringContextUtil;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import io.github.wanggit.antrpc.commons.zookeeper.ZkNodeType;
import org.apache.commons.lang3.RandomUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.Charset;

public class ZkNodeBuilderTest {

    @Test
    public void testBuild() throws Exception {
        int rpcPort = RandomUtils.nextInt(1000, 9999);
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment mockEnvironment =
                new MockEnvironment()
                        .withProperty("spring.application.name", "test")
                        .withProperty("antrpc.port", String.valueOf(rpcPort))
                        .withProperty(
                                "server.port", String.valueOf(RandomUtils.nextInt(2000, 9999)));
        genericApplicationContext.setEnvironment(mockEnvironment);
        genericApplicationContext.refresh();
        BeansToSpringContextUtil.toSpringContext(genericApplicationContext);
        Configuration configuration = new Configuration();
        configuration.setPort(rpcPort);
        configuration.setEnvironment(mockEnvironment);
        AntrpcContext antrpcContext = new AntrpcContext(configuration);
        antrpcContext.setOnFailProcessor(new OnFailProcessor());
        antrpcContext.setRegister(new ZkRegister());
        antrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        antrpcContext.init(genericApplicationContext);
        ZkNodeBuilder zkNodeBuilder =
                new ZkNodeBuilder(
                        antrpcContext.getZkClient().getCurator(),
                        antrpcContext.getNodeHostContainer());
        ZkNode zkNode =
                zkNodeBuilder.build(
                        ZkNodeType.Type.ROOT,
                        new ChildData("/" + ConstantValues.ZK_ROOT_NODE_NAME, new Stat(), null));
        Assert.assertTrue(zkNode instanceof RootZkNode);

        zkNode =
                zkNodeBuilder.build(
                        ZkNodeType.Type.IP,
                        new ChildData(
                                "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:8989",
                                new Stat(),
                                null));
        Assert.assertTrue(zkNode instanceof IpZkNode);

        zkNode =
                zkNodeBuilder.build(
                        ZkNodeType.Type.INTERFACE,
                        new ChildData(
                                "/"
                                        + ConstantValues.ZK_ROOT_NODE_NAME
                                        + "/127.0.0.1:8989/"
                                        + ZkNodeBuilderTest.class.getName(),
                                new Stat(),
                                null));
        Assert.assertTrue(zkNode instanceof InterfaceZkNode);
    }

    @Test
    public void testRemoteCreateZkNode() throws Exception {
        int rpcPort = RandomUtils.nextInt(1000, 9999);
        GenericApplicationContext genericApplicationContext = new GenericApplicationContext();
        MockEnvironment mockEnvironment =
                new MockEnvironment()
                        .withProperty("spring.application.name", "test")
                        .withProperty("antrpc.port", String.valueOf(rpcPort))
                        .withProperty(
                                "server.port", String.valueOf(RandomUtils.nextInt(2000, 9999)));
        genericApplicationContext.setEnvironment(mockEnvironment);
        Configuration configuration = new Configuration();
        configuration.setPort(rpcPort);
        configuration.setEnvironment(mockEnvironment);
        genericApplicationContext.refresh();
        BeansToSpringContextUtil.toSpringContext(genericApplicationContext);
        AntrpcContext antrpcContext = new AntrpcContext(configuration);
        antrpcContext.setOnFailProcessor(new OnFailProcessor());
        antrpcContext.setRegister(new ZkRegister());
        antrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        antrpcContext.init(genericApplicationContext);
        ZkNodeBuilder zkNodeBuilder = (ZkNodeBuilder) antrpcContext.getZkNodeBuilder();
        RegisterBean.IpNodeDataBean ipNodeDataBean = new RegisterBean.IpNodeDataBean();
        ipNodeDataBean.setHttpPort(RandomUtils.nextInt(1000, 9999));
        ipNodeDataBean.setRpcPort(rpcPort);
        ipNodeDataBean.setTs(System.currentTimeMillis());
        ipNodeDataBean.setAppName("test");
        String tmpPath =
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/127.0.0.1:3210/leafNode_for_keeper_not_delete";
        zkNodeBuilder.remoteCreateZkNode(
                tmpPath,
                JSONObject.toJSONString(ipNodeDataBean).getBytes(Charset.forName("UTF-8")),
                CreateMode.PERSISTENT);
        WaitUtil.wait(1, 1, false);
        CuratorFramework curator = antrpcContext.getZkClient().getCurator();
        byte[] bytes = curator.getData().forPath(tmpPath);
        RegisterBean.IpNodeDataBean dataBean =
                JSONObject.parseObject(
                        new String(bytes, Charset.forName("UTF-8")),
                        RegisterBean.IpNodeDataBean.class);
        Assert.assertNotNull(dataBean);
        Assert.assertEquals(dataBean.getAppName(), ipNodeDataBean.getAppName());
        Assert.assertEquals(dataBean.getHttpPort(), ipNodeDataBean.getHttpPort());
        Assert.assertEquals(dataBean.getRpcPort(), ipNodeDataBean.getRpcPort());

        RegisterBean.InterfaceNodeDataBean interfaceNodeDataBean =
                new RegisterBean.InterfaceNodeDataBean();
        interfaceNodeDataBean.setTs(System.currentTimeMillis());
        interfaceNodeDataBean.setMethods(Lists.newArrayList("getName()", "getType()"));
        zkNodeBuilder.remoteCreateZkNode(
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/127.0.0.1:3210/"
                        + ZkNodeBuilderTest.class.getName(),
                JSONObject.toJSONString(interfaceNodeDataBean).getBytes(Charset.forName("UTF-8")),
                CreateMode.EPHEMERAL);
        WaitUtil.wait(1, 1, false);
        bytes =
                curator.getData()
                        .forPath(
                                "/"
                                        + ConstantValues.ZK_ROOT_NODE_NAME
                                        + "/127.0.0.1:3210/"
                                        + ZkNodeBuilderTest.class.getName());
        RegisterBean.InterfaceNodeDataBean nodeDataBean =
                JSONObject.parseObject(
                        new String(bytes, Charset.forName("UTF-8")),
                        RegisterBean.InterfaceNodeDataBean.class);
        Assert.assertNotNull(nodeDataBean);
        Assert.assertEquals(
                nodeDataBean.getMethods().size(), interfaceNodeDataBean.getMethods().size());
        Assert.assertArrayEquals(
                nodeDataBean.getMethods().toArray(new String[] {}),
                interfaceNodeDataBean.getMethods().toArray(new String[] {}));

        curator.delete()
                .deletingChildrenIfNeeded()
                .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:3210");
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
