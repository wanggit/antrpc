package io.github.wanggit.antrpc.server.telnet.client;

import io.github.wanggit.antrpc.commons.utils.EpollUtil;
import io.github.wanggit.antrpc.server.telnet.client.handler.ServerResponseListener;
import io.github.wanggit.antrpc.server.telnet.client.handler.TelnetClientReadHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class TelnetClient implements ITelnetClient {

    private Channel channel;
    private Bootstrap bootstrap;
    private MultithreadEventLoopGroup loopGroup;

    public TelnetClient(String ip, int port, ServerResponseListener<String> listener)
            throws InterruptedException {
        bootstrap = new Bootstrap();
        loopGroup = EpollUtil.newEventLoopGroupInstance();
        bootstrap
                .group(loopGroup)
                .channel(EpollUtil.socketChannelClass())
                .handler(
                        new ChannelInitializer() {
                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline()
                                        .addLast(
                                                new DelimiterBasedFrameDecoder(
                                                        1024 * 100, Delimiters.lineDelimiter()))
                                        .addLast(new StringDecoder())
                                        .addLast(new StringEncoder())
                                        .addLast(new TelnetClientReadHandler(listener));
                            }
                        })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        channel = bootstrap.connect(ip, port).sync().channel();
    }

    @Override
    public void send(String cmd) {
        channel.writeAndFlush(cmd + "\r\n");
    }

    @Override
    public void close() {
        try {
            channel.close();
            loopGroup.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
