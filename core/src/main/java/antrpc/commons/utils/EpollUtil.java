package antrpc.commons.utils;

import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Locale;

public class EpollUtil {

    private static boolean isLinux = false;

    static {
        String osName = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
        if (osName.startsWith("linux")) {
            isLinux = true;
        }
    }

    public static MultithreadEventLoopGroup newEventLoopGroupInstance() {
        if (isLinux) {
            return new EpollEventLoopGroup();
        } else {
            return new NioEventLoopGroup();
        }
    }

    public static Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        if (isLinux) {
            return EpollServerSocketChannel.class;
        } else {
            return NioServerSocketChannel.class;
        }
    }

    public static Class<? extends SocketChannel> socketChannelClass() {
        if (isLinux) {
            return EpollSocketChannel.class;
        } else {
            return NioSocketChannel.class;
        }
    }
}
