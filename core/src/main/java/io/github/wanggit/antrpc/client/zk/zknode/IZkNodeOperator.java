package io.github.wanggit.antrpc.client.zk.zknode;

import org.apache.zookeeper.CreateMode;

public interface IZkNodeOperator {

    void remoteCreateZkNode(String zkFullpath, byte[] nodeData, CreateMode createMode);

    void deleteNode(String zookeeperFullPath);
}
