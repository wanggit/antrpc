package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;

import java.util.Map;

public abstract class AbsCmd implements ICmd {

    private IAntrpcContext antrpcContext;
    private Map<String, CmdInfoBean> telnetCmds;

    AbsCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        this.antrpcContext = antrpcContext;
        this.telnetCmds = telnetCmds;
    }

    protected IAntrpcContext getAntrpcContext() {
        return antrpcContext;
    }

    protected Map<String, CmdInfoBean> getTelnetCmds() {
        return telnetCmds;
    }

    @Override
    public String doCmd(String input) {
        return intervalDoCmd(getArguments(input));
    }

    protected abstract String intervalDoCmd(String[] arguments);

    protected String[] getArguments(String input) {
        String[] tmps = input.split("\\s+");
        String[] args = new String[tmps.length - 1];
        for (int i = 1; i < tmps.length; i++) {
            args[i - 1] = tmps[i];
        }
        return args;
    }
}
