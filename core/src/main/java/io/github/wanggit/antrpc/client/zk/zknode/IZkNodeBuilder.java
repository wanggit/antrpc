package io.github.wanggit.antrpc.client.zk.zknode;

import io.github.wanggit.antrpc.commons.zookeeper.ZkNodeType;
import org.apache.curator.framework.recipes.cache.ChildData;

public interface IZkNodeBuilder extends IZkNodeOperator {
    ZkNode build(ZkNodeType.Type type, ChildData childData);
}
