package io.github.wanggit.antrpc.server.telnet.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TelnetClientReadHandler extends SimpleChannelInboundHandler<String> {

    private ServerResponseListener<String> listener;

    public TelnetClientReadHandler(ServerResponseListener<String> listener) {
        this.listener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        this.listener.listen(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
