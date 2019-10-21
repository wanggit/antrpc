package antrpc.client.zk.zknode;

import antrpc.client.zk.register.RegisterBean;
import com.alibaba.fastjson.JSONObject;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;

public class IpZkNode extends ZkNode<RegisterBean.IpNodeDataBean> {
    IpZkNode(INodeHostContainer nodeHostContainer, String path, Stat stat, byte[] data) {
        super(nodeHostContainer, path, stat, data);
    }

    @Override
    public RegisterBean.IpNodeDataBean getNodeData() {
        byte[] data = getData();
        String json = new String(data, Charset.forName("UTF-8"));
        return JSONObject.parseObject(json, RegisterBean.IpNodeDataBean.class);
    }

    @Override
    public void refresh(OpType opType) {
        // do nothing
    }
}
