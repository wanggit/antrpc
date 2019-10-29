package io.github.wanggit.antrpc.client;

import io.github.wanggit.antrpc.commons.IRpcClients;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodecHolder;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.config.CodecConfig;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.config.RpcClientsConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RpcClients implements IRpcClients {

    private ConcurrentHashMap<String, RpcClient> clients = new ConcurrentHashMap<>();
    private IConfiguration configuration;
    private ICodecHolder codecHolder;
    private ISerializerHolder serializerHolder;

    public RpcClients(
            IConfiguration configuration,
            ICodecHolder codecHolder,
            ISerializerHolder serializerHolder) {
        this.configuration = configuration;
        this.codecHolder = codecHolder;
        this.serializerHolder = serializerHolder;
    }

    @Override
    public RpcClient getRpcClient(Host host) {
        String key = host.getIp() + ":" + host.getPort();
        if (!clients.containsKey(key)) {
            synchronized (key.intern()) {
                if (!clients.containsKey(key)) {
                    RpcClientsConfig rpcClientsConfig = configuration.getRpcClientsConfig();
                    CodecConfig codecConfig = configuration.getCodecConfig();
                    RpcClient rpcClient =
                            new RpcClient(
                                    host,
                                    codecHolder.getCodec(),
                                    codecConfig,
                                    rpcClientsConfig,
                                    serializerHolder);
                    rpcClient.open(rpcClientsConfig.getConnectionTimeoutSeconds());
                    clients.put(key, rpcClient);
                }
            }
        }
        return clients.get(key);
    }
}
