package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Cleans up expired nodes in zookeeper */
@Slf4j
public final class ZkNodeKeeper implements IZkNodeKeeper, Runnable {

    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private final CuratorFramework curator;
    private final IZkNodeBuilder zkNodeBuilder;

    public ZkNodeKeeper(IZkClient zkClient, IZkNodeBuilder zkNodeBuilder) {
        this.curator = zkClient.getCurator();
        this.zkNodeBuilder = zkNodeBuilder;
        init();
    }

    private void init() {
        if (atomicBoolean.compareAndSet(false, true)) {
            executorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
        }
    }

    @Override
    public void keepSubscribeNodes() {
        String root = "/" + ConstantValues.ZK_ROOT_SUBSCRIBE_NODE_NAME;
        List<String> paths = childrenNode(root);
        if (null != paths && !paths.isEmpty()) {
            for (int i = 0; i < paths.size(); i++) {
                String subPath =
                        "/" + ConstantValues.ZK_ROOT_SUBSCRIBE_NODE_NAME + "/" + paths.get(i);
                List<String> subNodes = childrenNode(subPath);
                if (null == subNodes || subNodes.isEmpty()) {
                    try {
                        curator.delete().forPath(subPath);
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error(
                                    "An exception occurred while clearing the "
                                            + subPath
                                            + " expiration node. It is possible that other nodes have already deleted it.",
                                    e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void keepRegisterNodes() {
        String root = "/" + ConstantValues.ZK_ROOT_NODE_NAME;
        List<String> paths = childrenNode(root);
        if (null != paths && !paths.isEmpty()) {
            for (int i = 0; i < paths.size(); i++) {
                String subPath = "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/" + paths.get(i);
                ZkNodeType.Type type = ZkNodeType.getType(subPath);
                if (Objects.equals(type, ZkNodeType.Type.IP)) {
                    List<String> subPaths = childrenNode(subPath);
                    if (null == subPaths || subPaths.isEmpty()) {
                        if (log.isInfoEnabled()) {
                            log.info("Remove the expired node " + subPath + " from zookeeper.");
                        }
                        try {
                            curator.delete().forPath(subPath);
                        } catch (Exception e) {
                            if (log.isErrorEnabled()) {
                                log.error(
                                        "An exception occurred while clearing the "
                                                + subPath
                                                + " expiration node. It is possible that the node has been deleted by another application or that another application is registering the node.",
                                        e);
                            }
                        }
                    } else {
                        subPaths.forEach(
                                interfaceName -> {
                                    String path = subPath + "/" + interfaceName;
                                    try {
                                        byte[] bytes = curator.getData().forPath(path);
                                        ChildData childData =
                                                new ChildData(path, new Stat(), bytes);
                                        ZkNode zkNode =
                                                zkNodeBuilder.build(
                                                        ZkNodeType.Type.INTERFACE, childData);
                                        zkNode.refresh(Node.OpType.UPDATE);
                                    } catch (Exception e) {
                                        if (log.isWarnEnabled()) {
                                            log.warn(
                                                    "An exception occurred when refreshing "
                                                            + path
                                                            + " node",
                                                    e);
                                        }
                                    }
                                });
                    }
                }
            }
        }
    }

    @Override
    public void run() {
        keepRegisterNodes();
        keepSubscribeNodes();
    }

    private List<String> childrenNode(String path) {
        List<String> paths = null;
        try {
            paths = curator.getChildren().forPath(path);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(
                        "There is an exception when "
                                + path
                                + " root node is obtained. Maybe "
                                + path
                                + " node does not exist, or it is not connected to zookeeper server.",
                        e);
            }
        }
        return paths;
    }
}
