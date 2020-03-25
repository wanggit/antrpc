package io.github.wanggit.antrpc.commons.generic.client;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.future.ReadClientFuture;

public interface IClient {

    ReadClientFuture send(RpcProtocol protocol);

    void close();
}
