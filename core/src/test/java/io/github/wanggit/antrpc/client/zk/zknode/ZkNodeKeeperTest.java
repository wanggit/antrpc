package io.github.wanggit.antrpc.client.zk.zknode;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.BeansToSpringContextUtil;
import io.github.wanggit.antrpc.client.spring.OnFailProcessor;
import io.github.wanggit.antrpc.client.spring.RpcAutowiredProcessor;
import io.github.wanggit.antrpc.client.zk.ZkClient;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.ZkRegister;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.Charset;
import java.util.List;

public class ZkNodeKeeperTest {

    @Before
    public void setUp() {}

    @Test
    public void testRun() throws Exception {
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
        configuration.setPort(RandomUtils.nextInt(1000, 9000));
        configuration.setEnvironment(mockEnvironment);
        AntrpcContext antrpcContext = new AntrpcContext(configuration);
        antrpcContext.setOnFailProcessor(new OnFailProcessor());
        antrpcContext.setRegister(new ZkRegister());
        antrpcContext.setRpcAutowiredProcessor(new RpcAutowiredProcessor());
        antrpcContext.init(genericApplicationContext);

        ZkClient zkClient = (ZkClient) antrpcContext.getZkClient();
        ZkNodeBuilder zkNodeBuilder = (ZkNodeBuilder) antrpcContext.getZkNodeBuilder();

        zkNodeBuilder.remoteCreateZkNode(
                "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/ip_node_will_delete",
                "data".getBytes(Charset.forName("UTF-8")),
                CreateMode.PERSISTENT);
        RegisterBean.InterfaceNodeDataBean interfaceNodeDataBean =
                new RegisterBean.InterfaceNodeDataBean();
        interfaceNodeDataBean.setMethods(Lists.newArrayList("find()"));
        interfaceNodeDataBean.setTs(System.currentTimeMillis());
        zkNodeBuilder.remoteCreateZkNode(
                "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:9909/leaf",
                JSONObject.toJSONString(interfaceNodeDataBean).getBytes(Charset.forName("UTF-8")),
                CreateMode.PERSISTENT);
        List<String> paths =
                zkClient.getCurator().getChildren().forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME);
        System.out.println(paths);
        Assert.assertTrue(paths.contains("ip_node_will_delete"));
        Assert.assertTrue(paths.contains("127.0.0.1:9909"));
        IZkNodeKeeper zkNodeKeeper = antrpcContext.getZkNodeKeeper();
        System.out.println(
                "Wait 4 minutes to make sure ZkNodeCleaner has cleaned up at least once.");
        WaitUtil.wait(240, 10);
        paths = zkClient.getCurator().getChildren().forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME);
        System.out.println(paths);
        Assert.assertFalse(paths.contains("ip_node_will_delete"));
        Assert.assertTrue(paths.contains("127.0.0.1:9909"));
        zkClient.getCurator()
                .delete()
                .deletingChildrenIfNeeded()
                .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:9909");
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
