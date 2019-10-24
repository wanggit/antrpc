package io.github.wanggit.antrpc.client;

import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.config.RpcClientsConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClients implements IRpcClients {

    private ConcurrentHashMap<String, RpcClient> clients = new ConcurrentHashMap<>();
    private IConfiguration configuration;

    public RpcClients(IConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public RpcClient getRpcClient(Host host) {
        String key = host.getIp() + ":" + host.getPort();
        if (!clients.containsKey(key)) {
            synchronized (key.intern()) {
                if (!clients.containsKey(key)) {
                    RpcClientsConfig rpcClientsConfig = configuration.getRpcClientsConfig();
                    RpcClient rpcClient =
                            new RpcClient(
                                    host,
                                    rpcClientsConfig.getMaxTotal(),
                                    rpcClientsConfig.getMinIdle(),
                                    rpcClientsConfig.getMaxIdle(),
                                    rpcClientsConfig.getMinEvictableIdleTimeMillis());
                    rpcClient.open(rpcClientsConfig.getConnectionTimeoutSeconds());
                    clients.put(key, rpcClient);
                }
            }
        }
        return clients.get(key);
    }
}
