package io.github.wanggit.antrpc.client.zk.zknode;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.client.zk.register.RegisterBeanHelper;
import io.github.wanggit.antrpc.commons.config.Configuration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.test.WaitUtil;
import org.apache.zookeeper.data.Stat;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterfaceZkNodeTest {

    @Test
    public void testRefresh() throws InterruptedException {
        Configuration configuration = new Configuration();
        LoadBalancerHelper loadBalancerHelper = new LoadBalancerHelper(configuration);
        Map<String, DirectNodeHostEntity> directHosts = new HashMap<>();
        INodeHostContainer nodeHostContainer =
                new NodeHostContainer(loadBalancerHelper, directHosts);
        RegisterBean.InterfaceNodeDataBean interfaceNodeDataBean =
                new RegisterBean.InterfaceNodeDataBean();
        Long ts = System.currentTimeMillis();
        interfaceNodeDataBean.setTs(ts);
        interfaceNodeDataBean.setMethods(Lists.newArrayList("getName()", "getType()"));
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = new HashMap<>();
        RegisterBean.RegisterBeanMethod testRefreshMethod =
                RegisterBeanHelper.getRegisterBeanMethod(
                        ReflectionUtils.findMethod(InterfaceZkNodeTest.class, "testRefresh"));
        methodMap.put(testRefreshMethod.toString(), testRefreshMethod);
        interfaceNodeDataBean.setMethodMap(methodMap);
        InterfaceZkNode interfaceZkNode =
                new InterfaceZkNode(
                        nodeHostContainer,
                        "/"
                                + ConstantValues.ZK_ROOT_NODE_NAME
                                + "/127.0.0.1:6868/"
                                + InterfaceZkNodeTest.class.getName(),
                        new Stat(),
                        JSONObject.toJSONString(interfaceNodeDataBean)
                                .getBytes(Charset.forName("UTF-8")));
        WaitUtil.wait(1, 1, false);
        RegisterBean.InterfaceNodeDataBean nodeData = interfaceZkNode.getNodeData();
        Assert.assertEquals(ts.longValue(), nodeData.getTs().longValue());

        interfaceZkNode.refresh(Node.OpType.ADD);
        List<NodeHostEntity> entities =
                nodeHostContainer.getHostEntities(
                        InterfaceZkNodeTest.class.getName(), testRefreshMethod.toString());
        Assert.assertFalse(entities.isEmpty());
        Assert.assertEquals(1, entities.size());
        NodeHostEntity nodeHostEntity = entities.get(0);
        Assert.assertEquals(nodeHostEntity.getHostInfo(), "127.0.0.1:6868");

        Long refreshTs = nodeHostEntity.getRefreshTs();
        WaitUtil.wait(1, 1, false);
        interfaceZkNode.refresh(Node.OpType.UPDATE);
        entities =
                nodeHostContainer.getHostEntities(
                        InterfaceZkNodeTest.class.getName(), testRefreshMethod.toString());
        Assert.assertFalse(entities.isEmpty());
        Assert.assertEquals(1, entities.size());
        nodeHostEntity = entities.get(0);
        Assert.assertEquals(nodeHostEntity.getHostInfo(), "127.0.0.1:6868");
        Assert.assertTrue(refreshTs < nodeHostEntity.getRefreshTs());

        interfaceZkNode.refresh(Node.OpType.REMOVE);
        entities =
                nodeHostContainer.getHostEntities(
                        InterfaceZkNodeTest.class.getName(), testRefreshMethod.toString());
        Assert.assertTrue(null == entities || entities.isEmpty());
    }
}

// Generated with love by TestMe :) Please report issues and submit feature requests at:
// http://weirddev.com/forum#!/testme
