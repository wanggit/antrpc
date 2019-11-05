package io.github.wanggit.antrpc.client.connections;

import io.github.wanggit.antrpc.commons.bean.RpcProtocol;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConnection implements Connection {

    private static final String ATTRIBUTE_KEY_NAME = "channel_to_connection";

    public static final AttributeKey<Connection> CONNECTION_ATTRIBUTE_KEY =
            AttributeKey.valueOf(ATTRIBUTE_KEY_NAME);

    private Channel channel;
    private ConnectionPool connectionPool;
    private HeartBeatCounter heartBeatCounter = new HeartBeatCounter();

    DefaultConnection(Channel channel, ConnectionPool connectionPool) {
        this.channel = channel;
        this.connectionPool = connectionPool;
        this.channel.attr(CONNECTION_ATTRIBUTE_KEY).set(this);
    }

    @Override
    public void send(RpcProtocol rpcProtocol) {
        if (channel.isActive()) {
            channel.writeAndFlush(rpcProtocol);
        } else {
            throw new ConnectionNotActiveException(channel);
        }
    }

    @Override
    public void reportHeartBeat(boolean send, int cmdId) {
        if (log.isInfoEnabled()) {
            log.info((send ? "Send" : "Receive") + " heartbeat packet, cmdId = " + cmdId + ".");
        }
        if (send) {
            heartBeatCounter.send(cmdId);
        } else {
            heartBeatCounter.receive(cmdId);
        }
        if (heartBeatCounter.heartBeatWasContinuousLoss()) {
            if (log.isInfoEnabled()) {
                log.info(
                        "Heartbeat packets lost more than 5 times, this connection will be closed.");
            }
            connectionPool.invalidateObject(this);
            this.heartBeatCounter = null;
            this.connectionPool = null;
            this.channel.close();
            this.channel = null;
        }
    }
}
