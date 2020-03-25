package io.github.wanggit.antrpc.commons.generic.handler;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.future.ReadClientFutureHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientReadHandler extends SimpleChannelInboundHandler<RpcProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol msg) throws Exception {
        ReadClientFutureHolder.receive(msg);
    }
}
