package io.github.wanggit.antrpc.commons.zookeeper;

public interface Node<T> {

    T getNodeData();

    void refresh(OpType opType);

    enum OpType {
        ADD,
        UPDATE,
        REMOVE
    }
}
