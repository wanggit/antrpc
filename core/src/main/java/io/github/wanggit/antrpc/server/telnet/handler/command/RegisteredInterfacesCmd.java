package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;

import java.util.List;
import java.util.Map;

/** 显示出本服务提供的所有接口 */
@CmdDesc(
        value = "lsp",
        desc =
                "list all provider interface."
                        + "\r\n\t\t\tExample: lsp"
                        + "\r\n\t\t\tExample: lsp HelloService\tdisplays the interface contains with \"HelloService\"")
public class RegisteredInterfacesCmd extends AbsCmd {
    public RegisteredInterfacesCmd(
            IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        String pattern = null;
        if (arguments.length > 0) {
            pattern = arguments[0];
        }
        IRegister register = getAntrpcContext().getRegister();
        List<RegisterBean> beans = register.snapshot();
        StringBuilder builder =
                new StringBuilder("--- all provided interfaces of the current service ---\r\n");
        for (RegisterBean bean : beans) {
            if (null != pattern && !bean.getClassName().contains(pattern)) {
                continue;
            }
            builder.append(
                    "\t"
                            + bean.getClassName()
                            + "\tSuspend Registration ***"
                            + bean.isPause()
                            + "***\r\n");
        }
        beans.clear();
        return builder.toString();
    }
}
