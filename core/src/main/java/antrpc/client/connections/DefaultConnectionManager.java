package antrpc.client.connections;

import antrpc.client.Host;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultConnectionManager implements ConnectionManager {

    private Bootstrap client;

    public DefaultConnectionManager(Bootstrap client) {
        this.client = client;
    }

    @Override
    public Connection getConnection(Host host) {
        try {
            ChannelFuture future = client.connect(host.getIp(), host.getPort()).sync();
            future.channel()
                    .closeFuture()
                    .addListener(
                            new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture channelFuture)
                                        throws Exception {
                                    channelFuture.channel().close();
                                }
                            });
            return new DefaultConnection(future.channel());
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to create Netty connection.", e);
            }
            return null;
        }
    }
}
