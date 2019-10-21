package antrpc.client.handler;

import antrpc.commons.bean.HeartBeatCreator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class ClientIdleHandler extends ChannelDuplexHandler {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                ctx.channel()
                        .writeAndFlush(HeartBeatCreator.create())
                        .addListener(
                                (ChannelFutureListener)
                                        future -> {
                                            if (!future.isSuccess()) {
                                                Channel channel = future.channel();
                                                channel.close();
                                            }
                                        });
            } else {
                super.userEventTriggered(ctx, evt);
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
