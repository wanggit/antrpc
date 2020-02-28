package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;

import java.util.Map;

@CmdDesc(value = "h", desc = "show the help informations.")
public class HelpCmd extends AbsCmd {

    public HelpCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        Map<String, CmdInfoBean> telnetCmds = getTelnetCmds();
        StringBuilder builder = new StringBuilder("AntRpc TelnetServer Command Help\r\n");
        telnetCmds.forEach(
                (key, value) -> {
                    builder.append(key).append("\t\t").append(value.getDesc()).append("\r\n");
                });
        return builder.toString();
    }
}
