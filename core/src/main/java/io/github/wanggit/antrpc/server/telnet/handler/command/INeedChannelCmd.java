package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.netty.channel.ChannelHandlerContext;

public interface INeedChannelCmd {

    void setChannelHandlerContext(ChannelHandlerContext context);
}
