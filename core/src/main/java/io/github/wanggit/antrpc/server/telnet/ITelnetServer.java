package io.github.wanggit.antrpc.server.telnet;

import io.github.wanggit.antrpc.commons.config.TelnetConfig;

import java.io.IOException;

public interface ITelnetServer {

    void start(TelnetConfig telnetConfig)
            throws InterruptedException, IOException, ClassNotFoundException;

    void close();
}
