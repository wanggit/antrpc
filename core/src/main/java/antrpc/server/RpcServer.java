package antrpc.server;

import antrpc.IAntrpcContext;
import antrpc.commons.codec.RpcProtocolDecoder;
import antrpc.commons.codec.RpcProtocolEncoder;
import antrpc.commons.constants.ConstantValues;
import antrpc.commons.utils.EpollUtil;
import antrpc.server.handler.ServerReadHandler;
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
    private IAntrpcContext antrpcContext;
    private AtomicBoolean activeAtomicBoolean = new AtomicBoolean(false);

    public RpcServer(IAntrpcContext antrpcContext) {
        this.antrpcContext = antrpcContext;
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
                                                new RpcProtocolDecoder(),
                                                new ServerReadHandler(
                                                        antrpcContext.getRpcRequestBeanInvoker()),
                                                new RpcProtocolEncoder());
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
        antrpcContext = null;
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
