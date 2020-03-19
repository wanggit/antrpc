package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.zk.register.ZkRegisterException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

@Slf4j
public class ZkNodeOperator implements IZkNodeOperator {

    private final CuratorFramework curator;

    public ZkNodeOperator(CuratorFramework curator) {
        this.curator = curator;
    }

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
