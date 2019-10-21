package antrpc.client.zk.zknode;

import antrpc.AntrpcContext;
import antrpc.IAntrpcContext;
import antrpc.client.monitor.RpcCallLogHolder;
import antrpc.client.spring.RpcBeanContainer;
import antrpc.client.zk.ZkClient;
import antrpc.client.zk.lb.LoadBalancerHelper;
import antrpc.client.zk.register.RegisterBean;
import antrpc.commons.breaker.CircuitBreaker;
import antrpc.commons.config.Configuration;
import antrpc.commons.constants.ConstantValues;
import antrpc.commons.test.WaitUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

import java.nio.charset.Charset;
import java.util.List;

public class ZkNodeKeeperTest {

    @Before
    public void setUp() {}

    @Test
    public void testRun() throws Exception {
        Configuration configuration = new Configuration();
        configuration.setPort(RandomUtils.nextInt(1000, 9000));
        ZkClient zkClient = new ZkClient(configuration);
        LoadBalancerHelper loadBalancerHelper = new LoadBalancerHelper(configuration);
        INodeHostContainer nodeHostContainer =
                new NodeHostContainer(loadBalancerHelper, configuration.getDirectHosts());
        ZkNodeBuilder zkNodeBuilder = new ZkNodeBuilder(zkClient.getCurator(), nodeHostContainer);
        IAntrpcContext antrpcContext =
                new AntrpcContext(
                        configuration,
                        new RpcBeanContainer(),
                        new CircuitBreaker(),
                        new RpcCallLogHolder());
        antrpcContext.init();
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
        ZkNodeKeeper zkNodeKeeper = new ZkNodeKeeper();
        zkNodeKeeper.setApplicationContext(
                new AbstractApplicationContext() {
                    @Override
                    protected void refreshBeanFactory()
                            throws BeansException, IllegalStateException {}

                    @Override
                    protected void closeBeanFactory() {}

                    @Override
                    public ConfigurableListableBeanFactory getBeanFactory()
                            throws IllegalStateException {
                        return null;
                    }

                    @Override
                    public <T> T getBean(Class<T> requiredType) throws BeansException {
                        if (IAntrpcContext.class.getName().equals(requiredType.getName())) {
                            return (T) antrpcContext;
                        }
                        return null;
                    }
                });
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
