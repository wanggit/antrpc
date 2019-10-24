package io.github.wanggit.antrpc.client.monitor;

import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.commons.config.IConfiguration;

public interface IRpcCallLogHolder {

    void log(RpcCallLog rpcCallLog);

    void init(IConfiguration configuration, IRpcClients rpcClients);
}
