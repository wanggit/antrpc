package io.github.wanggit.antrpc.client.handler;

import io.github.wanggit.antrpc.client.connections.Connection;
import io.github.wanggit.antrpc.client.connections.DefaultConnection;
import io.github.wanggit.antrpc.client.future.ReadClientFutureHolder;
import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.github.wanggit.antrpc.commons.constants.ConstantValues;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientReadHandler extends SimpleChannelInboundHandler<RpcProtocol> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcProtocol msg) throws Exception {
        if (msg.getType() == ConstantValues.HB_TYPE) {
            Connection connection =
                    ctx.channel().attr(DefaultConnection.CONNECTION_ATTRIBUTE_KEY).get();
            connection.reportHeartBeat(false, msg.getCmdId());
        } else {
            ReadClientFutureHolder.receive(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (log.isErrorEnabled()) {
            log.error(cause.getMessage());
        }
        ctx.close();
    }
}
