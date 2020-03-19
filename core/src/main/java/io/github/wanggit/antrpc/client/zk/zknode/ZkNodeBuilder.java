package io.github.wanggit.antrpc.client.zk.zknode;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.data.Stat;

@Slf4j
public class ZkNodeBuilder extends ZkNodeOperator implements IZkNodeBuilder {

    private final CuratorFramework curator;
    private final INodeHostContainer nodeHostContainer;

    public ZkNodeBuilder(CuratorFramework curator, INodeHostContainer nodeHostContainer) {
        super(curator);
        this.curator = curator;
        this.nodeHostContainer = nodeHostContainer;
    }

    @Override
    public ZkNode build(ZkNodeType.Type type, ChildData childData) {
        if (null == type || null == childData) {
            throw new IllegalArgumentException("type and childData cannot be null.");
        }
        String path = childData.getPath();
        Stat stat = childData.getStat();
        byte[] data = childData.getData();
        ZkNode zkNode = null;
        if (ZkNodeType.Type.ROOT.equals(type)) {
            zkNode = new RootZkNode(nodeHostContainer, path, stat, data);
        } else if (ZkNodeType.Type.IP.equals(type)) {
            zkNode = new IpZkNode(nodeHostContainer, path, stat, data);
        } else if (ZkNodeType.Type.INTERFACE.equals(type)) {
            zkNode = new InterfaceZkNode(nodeHostContainer, path, stat, data);
        }
        return zkNode;
    }
}
