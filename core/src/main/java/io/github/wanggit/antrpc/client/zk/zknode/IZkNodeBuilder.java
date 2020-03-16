package io.github.wanggit.antrpc.client.zk.zknode;

import org.apache.curator.framework.recipes.cache.ChildData;

public interface IZkNodeBuilder extends IZkNodeOperator {
    ZkNode build(ZkNodeType.Type type, ChildData childData);
}
