package antrpc.client.connections;

import antrpc.commons.bean.RpcProtocol;

public interface Connection {
    void send(RpcProtocol rpcProtocol);
}
