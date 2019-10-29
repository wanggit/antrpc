package io.github.wanggit.antrpc.server;

import io.github.wanggit.antrpc.commons.codec.RpcProtocolDecoder;
import io.github.wanggit.antrpc.commons.codec.RpcProtocolEncoder;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodecHolder;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializerHolder;
import io.github.wanggit.antrpc.commons.config.IConfiguration;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.github.wanggit.antrpc.commons.utils.EpollUtil;
import io.github.wanggit.antrpc.server.handler.ServerReadHandler;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RpcServer implements IServer {

    private MultithreadEventLoopGroup bossGroup = EpollUtil.newEventLoopGroupInstance();
    private MultithreadEventLoopGroup workerGroup = EpollUtil.newEventLoopGroupInstance();
    private Channel channel;
    private AtomicBoolean activeAtomicBoolean = new AtomicBoolean(false);
    private final IConfiguration configuration;
    private final ICodecHolder codecHolder;
    private final IRpcRequestBeanInvoker rpcRequestBeanInvoker;
    private final ISerializerHolder serializerHolder;

    public RpcServer(
            IConfiguration configuration,
            ICodecHolder codecHolder,
            IRpcRequestBeanInvoker rpcRequestBeanInvoker,
            ISerializerHolder serializerHolder) {
        this.configuration = configuration;
        this.codecHolder = codecHolder;
        this.rpcRequestBeanInvoker = rpcRequestBeanInvoker;
        this.serializerHolder = serializerHolder;
    }

    @Override
    public void open(Integer port) throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Initialize the server and use port " + port);
        }
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap
                .group(bossGroup, workerGroup)
                .channel(EpollUtil.serverSocketChannelClass())
                .childHandler(
                        new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(
                                                new RpcProtocolDecoder(
                                                        configuration.getCodecConfig(),
                                                        codecHolder.getCodec()),
                                                new ServerReadHandler(
                                                        rpcRequestBeanInvoker, serializerHolder),
                                                new RpcProtocolEncoder(
                                                        configuration.getCodecConfig(),
                                                        codecHolder.getCodec()));
                            }
                        })
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        channel = bootstrap.bind(port).sync().channel();
        if (log.isInfoEnabled()) {
            log.info("Server initialization complete, listen on port " + port);
        }
    }

    @Override
    public void open() throws InterruptedException {
        open(ConstantValues.RPC_DEFAULT_PORT);
    }

    @Override
    public void close() {
        channel.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    public void setActive() {
        activeAtomicBoolean.compareAndSet(false, true);
    }

    @Override
    public boolean isActive() {
        return activeAtomicBoolean.get();
    }
}
