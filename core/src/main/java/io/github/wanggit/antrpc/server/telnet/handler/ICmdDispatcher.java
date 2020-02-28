package io.github.wanggit.antrpc.server.telnet.handler;

import io.github.wanggit.antrpc.server.telnet.handler.command.ICmd;

public interface ICmdDispatcher {

    ICmd dispatcher(String cmd);
}
