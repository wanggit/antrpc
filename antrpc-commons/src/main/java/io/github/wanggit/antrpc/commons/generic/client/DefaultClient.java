package io.github.wanggit.antrpc.commons.generic.client;

import io.github.wanggit.antrpc.commons.bean.Host;
import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.codec.cryption.ICodec;
import io.github.wanggit.antrpc.commons.codec.serialize.ISerializer;
import io.github.wanggit.antrpc.commons.future.ReadClientFuture;
import io.github.wanggit.antrpc.commons.future.ReadClientFutureHolder;
import io.github.wanggit.antrpc.commons.generic.handler.ClientReadHandler;
import io.github.wanggit.antrpc.commons.generic.handler.ProtocolDecoder;
import io.github.wanggit.antrpc.commons.generic.handler.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class DefaultClient implements IClient {

    private Channel channel;
    private ISerializer serializer;
    private NioEventLoopGroup bossGroup = new NioEventLoopGroup();

    public DefaultClient(Host host, ICodec codec, ISerializer serializer)
            throws InterruptedException {
        this.serializer = serializer;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap
                .group(bossGroup)
                .channel(NioSocketChannel.class)
                .handler(
                        new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(
                                                new ProtocolDecoder(codec),
                                                new ClientReadHandler(),
                                                new ProtocolEncoder(codec, serializer));
                            }
                        });
        bootstrap.option(ChannelOption.SO_KEEPALIVE, false);
        channel = bootstrap.connect(host.getIp(), host.getPort()).sync().channel();
    }

    @Override
    public ReadClientFuture send(RpcProtocol protocol) {
        ReadClientFuture future =
                ReadClientFutureHolder.createFuture(protocol.getCmdId(), serializer);
        channel.writeAndFlush(protocol);
        return future;
    }

    @Override
    public void close() {
        try {
            channel.close();
            bossGroup.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
