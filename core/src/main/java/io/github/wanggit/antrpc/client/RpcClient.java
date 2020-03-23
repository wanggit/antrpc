package io.github.wanggit.antrpc.client;

import io.github.wanggit.antrpc.client.connections.*;
import io.github.wanggit.antrpc.client.handler.ClientIdleHandler;
import io.github.wanggit.antrpc.client.handler.ClientReadHandler;
import io.github.wanggit.antrpc.commons.bean.Host;
import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.RpcProtocolDecoder;
import io.github.wanggit.antrpc.commons.codec.RpcProtocolEncoder;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.config.CircuitBreakerConfig;
import io.github.wanggit.antrpc.commons.config.CodecConfig;
import io.github.wanggit.antrpc.commons.config.RpcClientsConfig;
import io.github.wanggit.antrpc.commons.future.ReadClientFuture;
import io.github.wanggit.antrpc.commons.future.ReadClientFutureHolder;
import io.github.wanggit.antrpc.commons.org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import io.github.wanggit.antrpc.commons.utils.EpollUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class RpcClient implements IClient {

    private MultithreadEventLoopGroup loopGroup = EpollUtil.newEventLoopGroupInstance();
    private Bootstrap bootstrap = new Bootstrap();
    private ConnectionManager connectionManager;
    private Host host;
    private GenericObjectPoolConfig<Connection> config;
    private ConnectionPool connectionPool;
    private CodecConfig codecConfig;
    private ICodec codec;
    private ISerializerHolder serializerHolder;
    private EventCountCircuitBreaker eventCountCircuitBreaker;
    private CircuitBreakerConfig circuitBreakerConfig;

    RpcClient(
            Host host,
            ICodec codec,
            CodecConfig codecConfig,
            RpcClientsConfig rpcClientsConfig,
            ISerializerHolder serializerHolder,
            CircuitBreakerConfig circuitBreakerConfig) {
        this.host = host;
        this.codec = codec;
        this.codecConfig = codecConfig;
        this.serializerHolder = serializerHolder;
        this.circuitBreakerConfig = circuitBreakerConfig;
        this.eventCountCircuitBreaker =
                new EventCountCircuitBreaker(
                        circuitBreakerConfig.getThreshold(),
                        circuitBreakerConfig.getCheckIntervalSeconds(),
                        TimeUnit.SECONDS);
        config = new GenericObjectPoolConfig<>();
        config.setMaxTotal(rpcClientsConfig.getMaxTotal());
        config.setMinIdle(rpcClientsConfig.getMinIdle());
        config.setMaxIdle(rpcClientsConfig.getMaxIdle());
        config.setMinEvictableIdleTimeMillis(rpcClientsConfig.getMinEvictableIdleTimeMillis());
        if (log.isInfoEnabled()) {
            log.info(
                    "Prepare to connect to "
                            + host.getIp()
                            + ":"
                            + host.getPort()
                            + ", maxTotal="
                            + rpcClientsConfig.getMaxTotal()
                            + ", maxIdle="
                            + rpcClientsConfig.getMaxIdle()
                            + ", minIdle="
                            + rpcClientsConfig.getMinIdle()
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
                                                new RpcProtocolDecoder(codecConfig, codec),
                                                new ClientReadHandler(),
                                                new RpcProtocolEncoder(codecConfig, codec),
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
            connection = getConnection();
            ReadClientFuture future =
                    ReadClientFutureHolder.createFuture(
                            rpcProtocol.getCmdId(), serializerHolder.getSerializer());
            connection.send(rpcProtocol);
            return future;
        } catch (ConnectionNotBorrowedException be) {
            if (log.isErrorEnabled()) {
                log.error("Unable to establish connection. Remote server may not be started.", be);
            }
            throw new ConnectionNotActiveException("Unable to establish connection.", be);
        } catch (ConnectionNotActiveException ce) {
            if (log.isErrorEnabled()) {
                log.error("Connection not alive, send error." + ce.getChannel().toString());
            }
            connectionPool.invalidateObject(connection);
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
            connection = getConnection();
            connection.send(rpcProtocol);
        } catch (ConnectionNotBorrowedException be) {
            if (log.isErrorEnabled()) {
                log.error("Unable to establish connection. Remote server may not be started.");
            }
            throw new ConnectionNotActiveException("Unable to establish connection.", be);
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

    private void printEventCountCircuitBreakerState() {
        AtomicReference<EventCountCircuitBreaker.CheckIntervalData> checkIntervalDataRef =
                eventCountCircuitBreaker.getCheckIntervalData();
        EventCountCircuitBreaker.CheckIntervalData intervalData = checkIntervalDataRef.get();
        if (log.isInfoEnabled()) {
            log.info(
                    "EventCountCircuitBreaker.CheckIntervalData eventCount="
                            + intervalData.getEventCount()
                            + ", checkIntervalStart="
                            + intervalData.getCheckIntervalStart());
        }
    }

    private Connection getConnection() {
        Connection connection = null;
        if (eventCountCircuitBreaker.checkState()) {
            try {
                connection = connectionPool.borrow();
            } catch (Exception e) {
                eventCountCircuitBreaker.incrementAndCheckState();
                throw e;
            }
        }
        if (null == connection) {
            if (log.isErrorEnabled()) {
                log.error(
                        "Connection Circuit Breaker was opened, error "
                                + this.circuitBreakerConfig.getThreshold()
                                + " times in "
                                + this.circuitBreakerConfig.getCheckIntervalSeconds()
                                + " seconds");
            }
            // Try to recover when the state is near
            if (eventCountCircuitBreaker.checkNearBy()) {
                if (log.isInfoEnabled()) {
                    log.info("The Circuit Breaker has reached the near state. Attempt to restore.");
                }
                try {
                    connection = connectionPool.borrow();
                    if (log.isInfoEnabled()) {
                        log.info("We've recovered. The Circuit Breaker is off.");
                    }
                    eventCountCircuitBreaker.close();
                } catch (Exception e) {
                    if (log.isInfoEnabled()) {
                        log.info("Recovery failed, The Circuit Breaker restarted.");
                    }
                    eventCountCircuitBreaker.open();
                    throw e;
                }
            } else {
                throw new ConnectionNotActiveException("Connection Circuit Breaker was opened.");
            }
        }
        return connection;
    }

    @Override
    public void close() {
        loopGroup.shutdownGracefully();
    }
}
