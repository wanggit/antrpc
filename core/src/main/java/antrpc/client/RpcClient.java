package antrpc.client;

import antrpc.client.connections.*;
import antrpc.client.future.ReadClientFuture;
import antrpc.client.future.ReadClientFutureHolder;
import antrpc.client.handler.ClientIdleHandler;
import antrpc.client.handler.ClientReadHandler;
import antrpc.commons.bean.RpcProtocol;
import antrpc.commons.codec.RpcProtocolDecoder;
import antrpc.commons.codec.RpcProtocolEncoder;
import antrpc.commons.utils.EpollUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

@Slf4j
public class RpcClient implements IClient {

    private MultithreadEventLoopGroup loopGroup = EpollUtil.newEventLoopGroupInstance();
    private Bootstrap bootstrap = new Bootstrap();
    private ConnectionManager connectionManager;
    private Host host;
    private GenericObjectPoolConfig<Connection> config;
    private ConnectionPool connectionPool;

    RpcClient(Host host, int maxTotal, int minIdle, int maxIdle, int minEvictableIdleTimeMillis) {
        this.host = host;
        config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);
        config.setMaxIdle(maxIdle);
        config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        if (log.isInfoEnabled()) {
            log.info(
                    "Prepare to connect to "
                            + host.getIp()
                            + ":"
                            + host.getPort()
                            + ", maxTotal="
                            + maxTotal
                            + ", maxIdle="
                            + maxIdle
                            + ", minIdle="
                            + minIdle
                            + " for connection pool configuration.");
        }
    }

    void open(int connectTimeout) {
        bootstrap
                .group(loopGroup)
                .channel(EpollUtil.socketChannelClass())
                .handler(
                        new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(
                                                new RpcProtocolDecoder(),
                                                new ClientReadHandler(),
                                                new RpcProtocolEncoder(),
                                                new IdleStateHandler(3, 3, 0),
                                                new ClientIdleHandler());
                            }
                        })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000);
        connectionManager = new DefaultConnectionManager(bootstrap);
        connectionPool = ConnectionPools.getOrCreateConnectionPool(host, connectionManager, config);
    }

    @Override
    public ReadClientFuture send(RpcProtocol rpcProtocol) {
        Connection connection = null;
        try {
            ReadClientFuture future = ReadClientFutureHolder.createFuture(rpcProtocol.getCmdId());
            connection = connectionPool.borrow();
            connection.send(rpcProtocol);
            return future;
        } catch (ConnectionNotActiveException ce) {
            if (log.isErrorEnabled()) {
                log.error("Connection not alive, send error." + ce.getChannel().toString());
            }
            ReadClientFutureHolder.removeFuture(rpcProtocol.getCmdId());
            throw ce;
        } finally {
            if (null != connection) {
                try {
                    connectionPool.returnObject(connection);
                } catch (IllegalStateException e) {
                    if (log.isWarnEnabled()) {
                        log.warn(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void oneway(RpcProtocol rpcProtocol) {
        Connection connection = null;
        try {
            connection = connectionPool.borrow();
            connection.send(rpcProtocol);
        } catch (ConnectionNotActiveException ce) {
            if (log.isErrorEnabled()) {
                log.error("Connection not alive, send error.");
            }
            connectionPool.invalidateObject(connection);
            throw ce;
        } finally {
            if (null != connection) {
                try {
                    connectionPool.returnObject(connection);
                } catch (IllegalStateException e) {
                    if (log.isWarnEnabled()) {
                        log.warn(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        loopGroup.shutdownGracefully();
    }
}
