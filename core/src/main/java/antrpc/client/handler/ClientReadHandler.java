package antrpc.client.handler;

import antrpc.client.future.ReadClientFutureHolder;
import antrpc.commons.bean.RpcProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientReadHandler extends SimpleChannelInboundHandler<RpcProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol msg) throws Exception {
        ReadClientFutureHolder.receive(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (log.isErrorEnabled()) {
            log.error(cause.getMessage());
        }
        ctx.close();
    }
}
