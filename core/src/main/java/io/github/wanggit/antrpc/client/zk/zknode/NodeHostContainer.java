package io.github.wanggit.antrpc.client.zk.zknode;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.client.zk.exception.InterfaceProviderNotFoundException;
import io.github.wanggit.antrpc.client.zk.lb.ILoadBalancer;
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
        ILoadBalancer<NodeHostEntity> loadBalancer =
                loadBalancerHelper.getLoadBalancer(NodeHostEntity.class);
        NodeHostEntity choosed = loadBalancer.chooseFrom(hostEntities);
        if (log.isDebugEnabled()) {
            log.debug(
                    className
                            + " --> hostEntities="
                            + JSONObject.toJSONString(hostEntities)
                            + " \n choosed="
                            + JSONObject.toJSONString(choosed)
                            + " \n loadBalancer="
                            + loadBalancer.getClass().getName());
        }
        return choosed;
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
            List<NodeHostEntity> theClassNameHostEntities = entities.get(className);
            int idx = findNodeHostEntity(theClassNameHostEntities, nodeHostEntity);
            if (idx == -1) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "will add node host info. className="
                                    + className
                                    + " \n nodeHostEntity="
                                    + JSONObject.toJSONString(nodeHostEntity));
                }
                entities.get(className).add(nodeHostEntity);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "will update node host info. className="
                                    + className
                                    + " \n nodeHostEntity="
                                    + JSONObject.toJSONString(nodeHostEntity));
                }
                intervalUpdateNodeInfos(theClassNameHostEntities.get(idx), nodeHostEntity);
            }
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
            intervalUpdateNodeInfos(values.get(idx), nodeHostEntity);
        }
    }

    private void intervalUpdateNodeInfos(NodeHostEntity oldEntity, NodeHostEntity newEntity) {
        oldEntity.setMethodStrs(newEntity.getMethodStrs());
        oldEntity.setRefreshTs(newEntity.getRefreshTs());
        oldEntity.setRegisterTs(newEntity.getRegisterTs());
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
