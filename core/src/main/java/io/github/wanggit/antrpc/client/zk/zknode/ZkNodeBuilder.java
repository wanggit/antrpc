package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.zk.register.ZkRegisterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

@Slf4j
public class ZkNodeBuilder implements IZkNodeBuilder {

    private final CuratorFramework curator;
    private final INodeHostContainer nodeHostContainer;

    public ZkNodeBuilder(CuratorFramework curator, INodeHostContainer nodeHostContainer) {
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

    /**
     * 在zk服务器上创建节点
     *
     * @param zkFullpath 节点全路径
     * @param nodeData 节点数据
     * @param createMode 节点类型
     */
    @Override
    public void remoteCreateZkNode(String zkFullpath, byte[] nodeData, CreateMode createMode) {
        // 因为zk的临时节点超时需要一段时间，如果在服务快速重启时，可能上一次会话的临时节点还未消失，所以在此手动删除一次
        try {
            curator.delete().deletingChildrenIfNeeded().forPath(zkFullpath);
        } catch (Exception e) {
            if (e instanceof KeeperException.NoNodeException) {
                if (log.isInfoEnabled()) {
                    log.info(
                            "The "
                                    + zkFullpath
                                    + " node for the last session has been deleted. "
                                    + e.getMessage());
                }
            } else if (e instanceof KeeperException.NotEmptyException) {
                if (log.isInfoEnabled()) {
                    log.info(
                            "The "
                                    + zkFullpath
                                    + " node has children and cannot be deleted. "
                                    + e.getMessage());
                }
            } else {
                throw new ZkRegisterException(
                        "An exception occurred while deleting the "
                                + zkFullpath
                                + " node from the last session.",
                        e);
            }
        }
        try {
            String forPath =
                    curator.create()
                            .creatingParentsIfNeeded()
                            .withMode(createMode)
                            .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                            .forPath(zkFullpath, nodeData);
            if (log.isInfoEnabled()) {
                log.info("Zookeeper node " + forPath + " was created successfully.");
            }
        } catch (Exception e) {
            throw new ZkRegisterException("Zookeeper node " + zkFullpath + " creation failed.", e);
        }
    }

    @Override
    public void deleteNode(String zookeeperFullPath) {
        try {
            curator.delete().deletingChildrenIfNeeded().forPath(zookeeperFullPath);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to delete " + zookeeperFullPath + " node.", e);
            }
        }
    }
}
