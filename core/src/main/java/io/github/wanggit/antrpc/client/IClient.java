package io.github.wanggit.antrpc.client;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.future.ReadClientFuture;

public interface IClient {

    ReadClientFuture send(RpcProtocol rpcProtocol);

    /**
     * only request not response.
     *
     * @param rpcProtocol
     */
    void oneway(RpcProtocol rpcProtocol);

    void close();
}
