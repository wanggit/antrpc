package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.netty.channel.Channel;

public class DefaultConnection implements Connection {

    private Channel channel;

    public DefaultConnection(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void send(RpcProtocol rpcProtocol) {
        if (channel.isActive()) {
            channel.writeAndFlush(rpcProtocol);
        } else {
            throw new ConnectionNotActiveException(channel);
        }
    }
}
