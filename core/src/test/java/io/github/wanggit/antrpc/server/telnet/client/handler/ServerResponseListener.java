package io.github.wanggit.antrpc.server.telnet.client.handler;

import io.netty.channel.ChannelHandlerContext;

public interface ServerResponseListener<T> {

    void listen(ChannelHandlerContext ctx, T content);

    void checkBuffer();
}
