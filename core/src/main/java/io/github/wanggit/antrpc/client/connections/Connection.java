package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;

public interface Connection {
    void send(RpcProtocol rpcProtocol);

    void reportHeartBeat(boolean send, int cmdId);
}
