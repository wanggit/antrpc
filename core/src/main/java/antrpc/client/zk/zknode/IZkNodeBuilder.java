package antrpc.client.zk.zknode;

import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.CreateMode;

public interface IZkNodeBuilder {
    ZkNode build(ZkNodeType.Type type, ChildData childData);

    void remoteCreateZkNode(String zkFullpath, byte[] nodeData, CreateMode createMode);
}
