package antrpc.client.connections;

import io.netty.channel.Channel;

public class ConnectionNotActiveException extends RuntimeException {

    private static final long serialVersionUID = -1823350881797927678L;
    private Channel channel;

    public ConnectionNotActiveException(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public ConnectionNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
