package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.Charset;

@Slf4j
public class InterfaceZkNode extends ZkNode<RegisterBean.InterfaceNodeDataBean> {

    InterfaceZkNode(INodeHostContainer nodeHostContainer, String path, Stat stat, byte[] data) {
        super(nodeHostContainer, path, stat, data);
    }

    @Override
    public RegisterBean.InterfaceNodeDataBean getNodeData() {
        byte[] data = getData();
        String json = new String(data, Charset.forName("UTF-8"));
        return JSONObject.parseObject(json, RegisterBean.InterfaceNodeDataBean.class);
    }

    @Override
    public void refresh(OpType opType) {
        if (null == opType) {
            throw new IllegalArgumentException("opType cannot be null.");
        }
        String path = getPath();
        if (log.isInfoEnabled()) {
            log.info(path + " refresh type was " + opType.name() + ".");
        }
        path = path.replaceFirst("/" + ConstantValues.ZK_ROOT_NODE_NAME + "/", "");
        String[] tmps = path.split("/");
        String host = tmps[0];
        String className = tmps[1];
        RegisterBean.InterfaceNodeDataBean nodeData = getNodeData();
        tmps = host.split(":");
        host = tmps[0];
        Integer port = NumberUtils.toInt(tmps[1]);
        NodeHostEntity hostEntity = new NodeHostEntity();
        hostEntity.setIp(host);
        hostEntity.setPort(port);
        hostEntity.setMethodStrs(nodeData.getMethods());
        hostEntity.setRegisterTs(nodeData.getTs());
        hostEntity.setRefreshTs(System.currentTimeMillis());
        hostEntity.setClassName(className);
        if (OpType.ADD.equals(opType)) {
            getNodeHostContainer().add(className, hostEntity);
        } else if (OpType.UPDATE.equals(opType)) {
            getNodeHostContainer().update(className, hostEntity);
        } else if (OpType.REMOVE.equals(opType)) {
            getNodeHostContainer().delete(className, hostEntity);
        } else {
            if (log.isErrorEnabled()) {
                log.error("Unknown node refresh type. " + opType.name());
            }
        }
    }
}
