package antrpc.client.monitor;

import antrpc.commons.IRpcClients;
import antrpc.commons.bean.RpcCallLog;
import antrpc.commons.config.IConfiguration;

public interface IRpcCallLogHolder {

    void log(RpcCallLog rpcCallLog);

    void init(IConfiguration configuration, IRpcClients rpcClients);
}
