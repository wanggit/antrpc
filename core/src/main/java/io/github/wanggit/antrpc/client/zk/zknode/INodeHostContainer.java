package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.client.zk.lb.ILoadBalancer;

import java.util.List;
import java.util.Map;

public interface INodeHostContainer {

    Map<String, List<NodeHostEntity>> entitiesSnapshot();

    Map<String, ILoadBalancer<NodeHostEntity>> loadBalancersSnapshot();

    List<NodeHostEntity> getHostEntities(String className, String methodFullName);

    NodeHostEntity choose(String className, String methodFullName);

    void add(String className, NodeHostEntity nodeHostEntity);

    void update(String className, NodeHostEntity nodeHostEntity);

    void delete(String className, NodeHostEntity nodeHostEntity);

    int findNodeHostEntity(List<NodeHostEntity> values, NodeHostEntity nodeHostEntity);
}
