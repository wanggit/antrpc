package io.github.wanggit.antrpc.client.zk.listener;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.AntrpcContext;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.monitor.RpcCallLogHolder;
import io.github.wanggit.antrpc.client.spring.RpcBeanContainer;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.zknode.NodeHostEntity;
import io.github.wanggit.antrpc.commons.breaker.CircuitBreaker;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.nio.charset.Charset;
import java.util.List;

public class ZkListenerTest {

    @Test
    public void testZkListener() throws Exception {
        GenericApplicationContext applicationContext = new GenericApplicationContext();
        MockEnvironment environment = new MockEnvironment();
        environment.withProperty("server.port", String.valueOf(RandomUtils.nextInt(5000, 9000)));
        applicationContext.setEnvironment(environment);
        AntrpcContext antrpcContext =
                new AntrpcContext(
                        new Configuration(),
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        Configuration configuration = (Configuration) antrpcContext.getConfiguration();
        configuration.setPort(RandomUtils.nextInt(5000, 9000));
        applicationContext.refresh();
        antrpcContext.init();
        applicationContext
                .getBeanFactory()
                .registerSingleton(IAntrpcContext.class.getName(), antrpcContext);
        ZkListener zkListener = new ZkListener();
        zkListener.setApplicationContext(applicationContext);

        RegisterBean.IpNodeDataBean ipNodeDataBean = new RegisterBean.IpNodeDataBean();
        ipNodeDataBean.setAppName("testApp");
        ipNodeDataBean.setHttpPort(RandomUtils.nextInt());
        ipNodeDataBean.setRpcPort(RandomUtils.nextInt());
        ipNodeDataBean.setTs(System.currentTimeMillis());
        antrpcContext
                .getZkNodeBuilder()
                .remoteCreateZkNode(
                        "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:6061",
                        JSONObject.toJSONString(ipNodeDataBean).getBytes(Charset.forName("UTF-8")),
                        CreateMode.PERSISTENT);

        RegisterBean.InterfaceNodeDataBean interfaceNodeDataBean =
                new RegisterBean.InterfaceNodeDataBean();
        interfaceNodeDataBean.setMethods(Lists.newArrayList("getName()"));
        interfaceNodeDataBean.setTs(System.currentTimeMillis());
        String path =
                "/"
                        + ConstantValues.ZK_ROOT_NODE_NAME
                        + "/127.0.0.1:6061/"
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
                antrpcContext.getNodeHostContainer().getHostEntities(AInterface.class.getName());
        Assert.assertEquals(1, hostEntities.size());
        for (NodeHostEntity hostEntity : hostEntities) {
            Assert.assertEquals(hostEntity.getHostInfo(), "127.0.0.1:6061");
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
                antrpcContext.getNodeHostContainer().getHostEntities(AInterface.class.getName());
        Assert.assertEquals(entities.size(), 1);
        for (NodeHostEntity hostEntity : entities) {
            Assert.assertEquals(hostEntity.getHostInfo(), "127.0.0.1:6061");
        }
        Assert.assertEquals(registerTs.longValue(), entities.get(0).getRegisterTs().longValue());
        Assert.assertTrue(entities.get(0).getRefreshTs() > refreshTs);

        antrpcContext.getZkClient().getCurator().delete().forPath(path);
        WaitUtil.wait(1, 1);
        hostEntities =
                antrpcContext.getNodeHostContainer().getHostEntities(AInterface.class.getName());
        Assert.assertTrue(null == hostEntities || hostEntities.isEmpty());

        antrpcContext
                .getZkClient()
                .getCurator()
                .delete()
                .forPath("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/127.0.0.1:6061");

        WaitUtil.wait(3, 1);
    }

    interface AInterface {}
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
