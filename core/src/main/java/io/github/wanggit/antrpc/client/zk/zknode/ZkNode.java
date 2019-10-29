package io.github.wanggit.antrpc.client.zk.zknode;

import org.apache.zookeeper.data.Stat;

public abstract class ZkNode<T> implements Node<T> {

    private final INodeHostContainer nodeHostContainer;
    private final String path;
    private final Stat stat;
    private final byte[] data;

    ZkNode(INodeHostContainer nodeHostContainer, String path, Stat stat, byte[] data) {
        this.nodeHostContainer = nodeHostContainer;
        this.path = path;
        this.stat = stat;
        this.data = data;
    }

    public String getPath() {
        return path;
    }

    public Stat getStat() {
        return stat;
    }

    protected byte[] getData() {
        return data;
    }

    protected INodeHostContainer getNodeHostContainer() {
        return nodeHostContainer;
    }
}
