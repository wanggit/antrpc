package antrpc.client;

import antrpc.client.future.ReadClientFuture;
import antrpc.commons.bean.RpcProtocol;

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
