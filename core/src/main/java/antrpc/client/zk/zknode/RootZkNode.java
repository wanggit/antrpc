package antrpc.client.zk.zknode;

import org.apache.zookeeper.data.Stat;

public class RootZkNode extends ZkNode<Object> {
    RootZkNode(INodeHostContainer nodeHostContainer, String path, Stat stat, byte[] data) {
        super(nodeHostContainer, path, stat, data);
    }

    @Override
    public Object getNodeData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refresh(OpType opType) {
        // do nothing
    }
}
