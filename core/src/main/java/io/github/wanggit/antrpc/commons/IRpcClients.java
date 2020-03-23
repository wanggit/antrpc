package io.github.wanggit.antrpc.commons;

import io.github.wanggit.antrpc.client.RpcClient;
import io.github.wanggit.antrpc.commons.bean.Host;

public interface IRpcClients {
    RpcClient getRpcClient(Host host);
}
