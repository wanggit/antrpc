package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.zk.exception.InterfaceProviderNotFoundException;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class NodeHostContainer implements INodeHostContainer {

    private final ConcurrentHashMap<String, List<NodeHostEntity>> entities =
            new ConcurrentHashMap<>();
    private final LoadBalancerHelper loadBalancerHelper;
    private final Map<String, DirectNodeHostEntity> directHosts;

    public NodeHostContainer(
            LoadBalancerHelper loadBalancerHelper, Map<String, DirectNodeHostEntity> directHosts) {
        this.loadBalancerHelper = loadBalancerHelper;
        this.directHosts = directHosts;
    }

    @Override
    public Map<String, List<NodeHostEntity>> snapshot() {
        return new TreeMap<>(entities);
    }

    @Override
    public List<NodeHostEntity> getHostEntities(String className) {
        return entities.get(className);
    }

    @Override
    public NodeHostEntity choose(String className) {
        if (null == className) {
            throw new IllegalArgumentException("className cannot be null.");
        }
        if (null != directHosts && !directHosts.isEmpty()) {
            DirectNodeHostEntity directNodeHostEntity = directHosts.get(className);
            if (null != directNodeHostEntity) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            "The "
                                    + className
                                    + " interface is directly connected to "
                                    + directNodeHostEntity.getHostInfo()
                                    + ", "
                                    + "and this configuration cannot be turned on in a production environment.");
                }
                return directNodeHostEntity;
            }
        }
        List<NodeHostEntity> hostEntities = entities.get(className);
        if (null == hostEntities || hostEntities.isEmpty()) {
            throw new InterfaceProviderNotFoundException(
                    "No service provider for the " + className + " interface was found.");
        }
        return loadBalancerHelper.getLoadBalancer(NodeHostEntity.class).chooseFrom(hostEntities);
    }

    @Override
    public void add(String className, NodeHostEntity nodeHostEntity) {
        if (null == className || null == nodeHostEntity) {
            throw new IllegalArgumentException("className and nodeHostEntity cannot be null.");
        }
        synchronized (className.intern()) {
            if (!entities.containsKey(className)) {
                entities.put(className, new ArrayList<>());
            }
            entities.get(className).add(nodeHostEntity);
        }
    }

    @Override
    public void update(String className, NodeHostEntity nodeHostEntity) {
        if (null == className || null == nodeHostEntity) {
            throw new IllegalArgumentException("className and nodeHostEntity cannot be null.");
        }
        synchronized (className.intern()) {
            internalUpdate(className, nodeHostEntity);
        }
    }

    private void internalUpdate(String className, NodeHostEntity nodeHostEntity) {
        List<NodeHostEntity> values = entities.get(className);
        if (null == values) {
            if (log.isWarnEnabled()) {
                log.warn(className + " configuration does not exist. will add it.");
            }
            entities.put(className, new ArrayList<>());
            values = entities.get(className);
        }
        if (log.isInfoEnabled()) {
            log.info(
                    "Update the "
                            + nodeHostEntity.getHostInfo()
                            + " node of the "
                            + className
                            + ".");
        }
        int idx = findNodeHostEntity(values, nodeHostEntity);
        if (idx == -1) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Not found the "
                                + nodeHostEntity.getHostInfo()
                                + " node of the "
                                + className
                                + ", will add it.");
            }
            values.add(nodeHostEntity);
        } else {
            NodeHostEntity entity = values.get(idx);
            entity.setMethodStrs(nodeHostEntity.getMethodStrs());
            entity.setRefreshTs(nodeHostEntity.getRefreshTs());
            entity.setRegisterTs(nodeHostEntity.getRegisterTs());
        }
    }

    @Override
    public void delete(String className, NodeHostEntity nodeHostEntity) {
        if (null == className || null == nodeHostEntity) {
            throw new IllegalArgumentException("className and nodeHostEntity cannot be null.");
        }
        synchronized (className.intern()) {
            internalDelete(className, nodeHostEntity);
        }
    }

    private void internalDelete(String className, NodeHostEntity nodeHostEntity) {
        List<NodeHostEntity> values = entities.get(className);
        if (null == values) {
            if (log.isWarnEnabled()) {
                log.warn(className + " configuration does not exist.");
            }
        }
        if (log.isInfoEnabled()) {
            log.info(
                    "Delete the "
                            + nodeHostEntity.getHostInfo()
                            + " node of the "
                            + className
                            + ".");
        }
        int idx = findNodeHostEntity(values, nodeHostEntity);
        if (idx == -1) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Not found the "
                                + nodeHostEntity.getHostInfo()
                                + " node of the "
                                + className
                                + ".");
            }
        } else {
            values.remove(idx);
        }
        if (values.isEmpty()) {
            entities.remove(className);
        }
    }

    @Override
    public int findNodeHostEntity(List<NodeHostEntity> values, NodeHostEntity nodeHostEntity) {
        int idx = -1;
        for (int i = 0; i < values.size(); i++) {
            NodeHostEntity value = values.get(i);
            if (value.getHostInfo().equals(nodeHostEntity.getHostInfo())) {
                idx = i;
                break;
            }
        }
        return idx;
    }
}
