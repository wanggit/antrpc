package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.ICmdDispatcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class CmdDispatcher implements ICmdDispatcher {

    private IAntrpcContext antrpcContext;
    private Map<String, CmdInfoBean> telnetCmds;

    public CmdDispatcher(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        this.antrpcContext = antrpcContext;
        this.telnetCmds = telnetCmds;
    }

    @Override
    public ICmd dispatcher(String cmd) {
        int idx = cmd.trim().indexOf(" ");
        String cmdName = idx == -1 ? cmd.trim() : cmd.substring(0, idx);
        CmdInfoBean cmdInfoBean = telnetCmds.get(cmdName);
        if (null == cmdInfoBean) {
            return null;
        }
        Class<? extends ICmd> clazz = cmdInfoBean.getAClass();
        if (null == clazz) {
            return null;
        }
        try {
            return clazz.getConstructor(IAntrpcContext.class, Map.class)
                    .newInstance(antrpcContext, telnetCmds);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Can not instance the " + clazz.getName(), e);
            }
        }
        return null;
    }
}
