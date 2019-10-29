package io.github.wanggit.antrpc.client.zk.listener;

import io.github.wanggit.antrpc.client.zk.IZkClient;
import io.github.wanggit.antrpc.client.zk.register.IZkRegisterHolder;
import io.github.wanggit.antrpc.client.zk.zknode.IZkNodeBuilder;
import io.github.wanggit.antrpc.client.zk.zknode.Node;
import io.github.wanggit.antrpc.client.zk.zknode.ZkNode;
import io.github.wanggit.antrpc.client.zk.zknode.ZkNodeType;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

@Slf4j
public class ZkListener implements Listener {

    private final IZkClient zkClient;
    private final IZkRegisterHolder zkRegisterHolder;
    private final IZkNodeBuilder zkNodeBuilder;

    public ZkListener(
            IZkClient zkClient, IZkRegisterHolder zkRegisterHolder, IZkNodeBuilder zkNodeBuilder) {
        this.zkClient = zkClient;
        this.zkRegisterHolder = zkRegisterHolder;
        this.zkNodeBuilder = zkNodeBuilder;
    }

    @Override
    public void listen() {
        CuratorFramework curator = zkClient.getCurator();
        TreeCache treeCache = new TreeCache(curator, "/" + ConstantValues.ZK_ROOT_NODE_NAME);
        treeCache
                .getListenable()
                .addListener(
                        (client, event) -> {
                            if (TreeCacheEvent.Type.NODE_ADDED.equals(event.getType())) {
                                ChildData data = event.getData();
                                String path = data.getPath();
                                if (log.isInfoEnabled()) {
                                    log.info(path + " node added successfully.");
                                }
                                nodeRefresh(path, data, Node.OpType.ADD);
                            } else if (TreeCacheEvent.Type.NODE_UPDATED.equals(event.getType())) {
                                ChildData data = event.getData();
                                String path = data.getPath();
                                if (log.isInfoEnabled()) {
                                    log.info("The " + path + " node data has been updated.");
                                }
                                nodeRefresh(path, data, Node.OpType.UPDATE);
                            } else if (TreeCacheEvent.Type.NODE_REMOVED.equals(event.getType())) {
                                ChildData data = event.getData();
                                String path = data.getPath();
                                if (log.isInfoEnabled()) {
                                    log.info("The " + data.getPath() + " node has been deleted.");
                                }
                                nodeRefresh(path, data, Node.OpType.REMOVE);
                            } else if (TreeCacheEvent.Type.CONNECTION_RECONNECTED.equals(
                                    event.getType())) {
                                if (log.isInfoEnabled()) {
                                    log.info("Zookeeper reconnects and re-registers the service.");
                                }
                                zkRegisterHolder.allReRegister();
                            }
                        });
        try {
            treeCache.start();
        } catch (Exception e) {
            String errorMessage = "TreeCache failed to start.";
            if (log.isErrorEnabled()) {
                log.error(errorMessage, e);
            }
            throw new ZkListenerException(errorMessage, e);
        }
    }

    private void nodeRefresh(String path, ChildData data, Node.OpType opType) {
        ZkNodeType.Type type = ZkNodeType.getType(path);
        if (null == type) {
            if (log.isErrorEnabled()) {
                log.error("Unknown node type. " + path);
            }
        } else {
            ZkNode zkNode = zkNodeBuilder.build(type, data);
            zkNode.refresh(opType);
        }
    }
}
