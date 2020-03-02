package io.github.wanggit.antrpc.server.telnet.client;

public interface ITelnetClient {

    void send(String cmd);

    void close();
}
