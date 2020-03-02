package io.github.wanggit.antrpc.server.telnet.client.handler;

import io.netty.channel.ChannelHandlerContext;

public abstract class AbsServerResponseListener implements ServerResponseListener<String> {

    private StringBuffer buffer = new StringBuffer();

    @Override
    public void listen(ChannelHandlerContext ctx, String content) {
        buffer.append(content + "\r\n");
    }

    @Override
    public void checkBuffer() {
        System.out.println(buffer.toString());
        internalCheckBuffer(buffer.toString());
    }

    protected abstract void internalCheckBuffer(String content);

    public String getBuffer() {
        return buffer.toString();
    }
}
