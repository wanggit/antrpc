package io.github.wanggit.antrpc.client.zk.listener;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.zknode.Node;
import io.github.wanggit.antrpc.client.zk.zknode.ZkNode;
import io.github.wanggit.antrpc.client.zk.zknode.ZkNodeType;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class ZkListener implements ApplicationContextAware {

    private IAntrpcContext antrpcContext;

    private void listen() {
        CuratorFramework curator = antrpcContext.getZkClient().getCurator();

        TreeCache treeCache = new TreeCache(curator, "/" + ConstantValues.ZK_ROOT_NODE_NAME);
        treeCache
                .getListenable()
                .addListener(
                        new TreeCacheListener() {
                            @Override
                            public void childEvent(CuratorFramework client, TreeCacheEvent event)
                                    throws Exception {
                                if (TreeCacheEvent.Type.NODE_ADDED.equals(event.getType())) {
                                    ChildData data = event.getData();
                                    String path = data.getPath();
                                    if (log.isInfoEnabled()) {
                                        log.info(path + " node added successfully.");
                                    }
                                    nodeRefresh(path, data, Node.OpType.ADD);
                                } else if (TreeCacheEvent.Type.NODE_UPDATED.equals(
                                        event.getType())) {
                                    ChildData data = event.getData();
                                    String path = data.getPath();
                                    if (log.isInfoEnabled()) {
                                        log.info("The " + path + " node data has been updated.");
                                    }
                                    nodeRefresh(path, data, Node.OpType.UPDATE);
                                } else if (TreeCacheEvent.Type.NODE_REMOVED.equals(
                                        event.getType())) {
                                    ChildData data = event.getData();
                                    String path = data.getPath();
                                    if (log.isInfoEnabled()) {
                                        log.info(
                                                "The "
                                                        + data.getPath()
                                                        + " node has been deleted.");
                                    }
                                    nodeRefresh(path, data, Node.OpType.REMOVE);
                                } else if (TreeCacheEvent.Type.CONNECTION_RECONNECTED.equals(
                                        event.getType())) {
                                    if (log.isInfoEnabled()) {
                                        log.info(
                                                "Zookeeper reconnects and re-registers the service.");
                                    }
                                    antrpcContext.getZkRegisterHolder().allReRegister();
                                }
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
            ZkNode zkNode = antrpcContext.getZkNodeBuilder().build(type, data);
            zkNode.refresh(opType);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.antrpcContext = applicationContext.getBean(IAntrpcContext.class);
        this.listen();
    }
}
