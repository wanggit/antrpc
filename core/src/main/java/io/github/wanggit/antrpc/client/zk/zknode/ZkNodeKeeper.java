package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Cleans up expired nodes in zookeeper */
@Slf4j
public final class ZkNodeKeeper implements Runnable, ApplicationContextAware {

    private final ScheduledExecutorService executorService =
            Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private CuratorFramework curator;
    private IAntrpcContext antrpcContext;

    public ZkNodeKeeper() {
        init();
    }

    private void init() {
        if (atomicBoolean.compareAndSet(false, true)) {
            executorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
        }
    }

    @Override
    public void run() {
        String root = "/" + ConstantValues.ZK_ROOT_NODE_NAME;
        List<String> paths = childrenNode(root);
        if (null != paths && !paths.isEmpty()) {
            for (int i = 0; i < paths.size(); i++) {
                String subPath = "/" + ConstantValues.ZK_ROOT_NODE_NAME + "/" + paths.get(i);
                ZkNodeType.Type type = ZkNodeType.getType(subPath);
                if (type.equals(ZkNodeType.Type.IP)) {
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
                                                antrpcContext
                                                        .getZkNodeBuilder()
                                                        .build(
                                                                ZkNodeType.Type.INTERFACE,
                                                                childData);
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        antrpcContext = applicationContext.getBean(IAntrpcContext.class);
        this.curator = antrpcContext.getZkClient().getCurator();
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
