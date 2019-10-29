package io.github.wanggit.antrpc.client.monitor;

import io.github.wanggit.antrpc.commons.bean.RpcCallLog;

public interface IRpcCallLogHolder {

    void log(RpcCallLog rpcCallLog);
}
