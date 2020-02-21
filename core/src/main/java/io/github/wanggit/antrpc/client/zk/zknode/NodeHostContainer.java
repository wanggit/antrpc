package io.github.wanggit.antrpc.client.zk.zknode;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.client.zk.exception.InterfaceProviderNotFoundException;
import io.github.wanggit.antrpc.client.zk.lb.ILoadBalancer;
import io.github.wanggit.antrpc.client.zk.lb.LoadBalancerHelper;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class NodeHostContainer implements INodeHostContainer {

    private final ConcurrentHashMap<String, List<NodeHostEntity>> entities =
            new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ILoadBalancer<NodeHostEntity>> loadBalancers =
            new ConcurrentHashMap<>();
    private final LoadBalancerHelper loadBalancerHelper;
    private final Map<String, DirectNodeHostEntity> directHosts;

    public NodeHostContainer(
            LoadBalancerHelper loadBalancerHelper, Map<String, DirectNodeHostEntity> directHosts) {
        this.loadBalancerHelper = loadBalancerHelper;
        this.directHosts = directHosts;
    }

    @Override
    public List<NodeHostEntity> getHostEntities(String className, String methodFullName) {
        if (null == className || null == methodFullName) {
            throw new IllegalArgumentException("className and methodFullName cannot be null.");
        }
        return entities.get(getKey(className, methodFullName));
    }

    @Override
    public NodeHostEntity choose(String className, String methodFullName) {
        if (null == className || null == methodFullName) {
            throw new IllegalArgumentException("className and methodFullName cannot be null.");
        }
        // 直连时只依据接口判断
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
        String fullName = getKey(className, methodFullName);
        if (!entities.containsKey(fullName)) {
            throw new InterfaceProviderNotFoundException(
                    "No service provider for the " + fullName + " interface was found.");
        }
        List<NodeHostEntity> hostEntities = entities.get(fullName);
        if (null == hostEntities || hostEntities.isEmpty()) {
            throw new InterfaceProviderNotFoundException(
                    "No service provider for the "
                            + className
                            + "#"
                            + methodFullName
                            + " interface was found.");
        }
        ILoadBalancer<NodeHostEntity> loadBalancer = loadBalancers.get(fullName);
        if (null == loadBalancer) {
            throw new IllegalArgumentException("The " + fullName + " has no load balancer.");
        }
        NodeHostEntity choosed = loadBalancer.chooseFrom(hostEntities);
        if (log.isDebugEnabled()) {
            log.debug(
                    fullName
                            + "#"
                            + methodFullName
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
            Map<String, RegisterBean.RegisterBeanMethod> methodMap = nodeHostEntity.getMethodMap();
            methodMap.forEach(
                    (key, value) -> {
                        String fullName = getKey(className, value.toString());
                        List<NodeHostEntity> nodeHostEntities = entities.get(fullName);
                        if (null == nodeHostEntities) {
                            entities.put(fullName, new ArrayList<>());
                            nodeHostEntities = entities.get(fullName);
                            loadBalancers.put(fullName, loadBalancerHelper.getLoadBalancer());
                        }
                        int idx = findNodeHostEntity(nodeHostEntities, nodeHostEntity);
                        if (idx == -1) {
                            if (log.isDebugEnabled()) {
                                log.debug(
                                        "will add node host info. className="
                                                + fullName
                                                + " \n nodeHostEntity="
                                                + JSONObject.toJSONString(nodeHostEntity));
                            }
                            entities.get(fullName).add(nodeHostEntity);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug(
                                        "will update node host info. className="
                                                + fullName
                                                + " \n nodeHostEntity="
                                                + JSONObject.toJSONString(nodeHostEntity));
                            }
                            intervalUpdateNodeInfos(nodeHostEntities.get(idx), nodeHostEntity);
                        }
                    });
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
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = nodeHostEntity.getMethodMap();
        methodMap.forEach(
                (key, value) -> {
                    String fullName = getKey(className, value.toString());
                    List<NodeHostEntity> values = entities.get(fullName);
                    if (null == values) {
                        if (log.isWarnEnabled()) {
                            log.warn(fullName + " configuration does not exist. will add it.");
                        }
                        entities.put(fullName, new ArrayList<>());
                        values = entities.get(fullName);
                        loadBalancers.put(fullName, loadBalancerHelper.getLoadBalancer());
                    }
                    if (log.isInfoEnabled()) {
                        log.info(
                                "Update the "
                                        + nodeHostEntity.getHostInfo()
                                        + " node of the "
                                        + fullName
                                        + ".");
                    }
                    int idx = findNodeHostEntity(values, nodeHostEntity);
                    if (idx == -1) {
                        if (log.isWarnEnabled()) {
                            log.warn(
                                    "Not found the "
                                            + nodeHostEntity.getHostInfo()
                                            + " node of the "
                                            + fullName
                                            + ", will add it.");
                        }
                        values.add(nodeHostEntity);
                    } else {
                        intervalUpdateNodeInfos(values.get(idx), nodeHostEntity);
                    }
                });
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
        Map<String, RegisterBean.RegisterBeanMethod> methodMap = nodeHostEntity.getMethodMap();
        methodMap.forEach(
                (key, value) -> {
                    String fullName = getKey(className, value.toString());
                    List<NodeHostEntity> values = entities.get(fullName);
                    if (null == values) {
                        if (log.isWarnEnabled()) {
                            log.warn(fullName + " configuration does not exist.");
                        }
                    }
                    if (log.isInfoEnabled()) {
                        log.info(
                                "Delete the "
                                        + nodeHostEntity.getHostInfo()
                                        + " node of the "
                                        + fullName
                                        + ".");
                    }
                    int idx = findNodeHostEntity(values, nodeHostEntity);
                    if (idx == -1) {
                        if (log.isWarnEnabled()) {
                            log.warn(
                                    "Not found the "
                                            + nodeHostEntity.getHostInfo()
                                            + " node of the "
                                            + fullName
                                            + ".");
                        }
                    } else {
                        values.remove(idx);
                    }
                    if (values.isEmpty()) {
                        entities.remove(fullName);
                        loadBalancers.remove(fullName);
                    }
                });
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

    private String getKey(String className, String methodFullName) {
        return className + "#" + methodFullName;
    }
}
