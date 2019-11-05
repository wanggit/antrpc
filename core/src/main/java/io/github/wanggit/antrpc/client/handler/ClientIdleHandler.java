package io.github.wanggit.antrpc.client.handler;

import io.github.wanggit.antrpc.client.connections.DefaultConnection;
import io.github.wanggit.antrpc.commons.bean.HeartBeatCreator;
import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientIdleHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                RpcProtocol rpcProtocol = HeartBeatCreator.create();
                ctx.channel().writeAndFlush(rpcProtocol);
                ctx.channel()
                        .attr(DefaultConnection.CONNECTION_ATTRIBUTE_KEY)
                        .get()
                        .reportHeartBeat(true, rpcProtocol.getCmdId());
            } else {
                super.userEventTriggered(ctx, evt);
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
