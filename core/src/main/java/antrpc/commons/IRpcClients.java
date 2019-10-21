package antrpc.commons;

import antrpc.client.Host;
import antrpc.client.RpcClient;

public interface IRpcClients {
    RpcClient getRpcClient(Host host);
}
