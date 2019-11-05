package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.client.Host;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultConnectionManager implements ConnectionManager {

    private Bootstrap client;

    private ConcurrentHashMap<Host, ConnectionPool> connectionPoolMap = new ConcurrentHashMap<>();

    public DefaultConnectionManager(Bootstrap client) {
        this.client = client;
    }

    @Override
    public Connection getConnection(Host host) {
        try {
            ChannelFuture future = client.connect(host.getIp(), host.getPort()).sync();
            future.channel()
                    .closeFuture()
                    .addListener(
                            new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture channelFuture)
                                        throws Exception {
                                    channelFuture.channel().close();
                                }
                            });
            if (!connectionPoolMap.containsKey(host)) {
                if (log.isErrorEnabled()) {
                    log.error(
                            "The connection pool for "
                                    + host.getHostInfo()
                                    + " host was not found.");
                }
                return null;
            }
            return new DefaultConnection(future.channel(), connectionPoolMap.get(host));
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to create Netty connection.", e);
            }
            return null;
        }
    }

    @Override
    public void addConnectionPool(Host host, ConnectionPool connectionPool) {
        this.connectionPoolMap.put(host, connectionPool);
    }
}
