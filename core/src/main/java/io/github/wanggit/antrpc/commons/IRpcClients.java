package io.github.wanggit.antrpc.commons;

import io.github.wanggit.antrpc.client.Host;
import io.github.wanggit.antrpc.client.RpcClient;

public interface IRpcClients {
    RpcClient getRpcClient(Host host);
}
