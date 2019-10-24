package io.github.wanggit.antrpc.client.zk.zknode;

import java.util.List;
import java.util.Map;

public interface INodeHostContainer {
    Map<String, List<NodeHostEntity>> snapshot();

    List<NodeHostEntity> getHostEntities(String className);

    NodeHostEntity choose(String className);

    void add(String className, NodeHostEntity nodeHostEntity);

    void update(String className, NodeHostEntity nodeHostEntity);

    void delete(String className, NodeHostEntity nodeHostEntity);

    int findNodeHostEntity(List<NodeHostEntity> values, NodeHostEntity nodeHostEntity);
}
