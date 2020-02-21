package io.github.wanggit.antrpc.client.zk.zknode;

import java.util.List;

public interface INodeHostContainer {

    List<NodeHostEntity> getHostEntities(String className, String methodFullName);

    NodeHostEntity choose(String className, String methodFullName);

    void add(String className, NodeHostEntity nodeHostEntity);

    void update(String className, NodeHostEntity nodeHostEntity);

    void delete(String className, NodeHostEntity nodeHostEntity);

    int findNodeHostEntity(List<NodeHostEntity> values, NodeHostEntity nodeHostEntity);
}
