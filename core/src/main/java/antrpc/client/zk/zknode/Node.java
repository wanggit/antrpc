package antrpc.client.zk.zknode;

public interface Node<T> {

    T getNodeData();

    void refresh(OpType opType);

    enum OpType {
        ADD,
        UPDATE,
        REMOVE
    }
}
