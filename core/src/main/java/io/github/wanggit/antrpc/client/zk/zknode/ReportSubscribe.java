package io.github.wanggit.antrpc.client.zk.zknode;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import org.apache.zookeeper.CreateMode;

import java.nio.charset.Charset;

public class ReportSubscribe implements IReportSubscriber {

    private final IZkNodeOperator zkNodeOperator;

    public ReportSubscribe(IZkNodeOperator zkNodeOperator) {
        this.zkNodeOperator = zkNodeOperator;
    }

    @Override
    public void report(SubscribeNode subscribeNode) {
        String fullPath = getFullpath(subscribeNode);
        byte[] bytes = JSONObject.toJSONString(subscribeNode).getBytes(Charset.forName("UTF-8"));
        zkNodeOperator.remoteCreateZkNode(fullPath, bytes, CreateMode.EPHEMERAL);
    }

    private String getFullpath(SubscribeNode subscribeNode) {
        return "/"
                + ConstantValues.ZK_ROOT_SUBSCRIBE_NODE_NAME
                + "/"
                + subscribeNode.getHost()
                + "/"
                + subscribeNode.getClassName();
    }
}
