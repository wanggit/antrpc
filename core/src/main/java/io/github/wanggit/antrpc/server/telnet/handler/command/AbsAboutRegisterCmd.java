package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;

import java.util.List;
import java.util.Map;

abstract class AbsAboutRegisterCmd extends AbsCmd {
    AbsAboutRegisterCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    String registerStatus(IRegister register, String[] arguments) {
        List<RegisterBean> beans = register.snapshot();
        StringBuilder builder = new StringBuilder("--- Result ---\r\n");
        for (RegisterBean bean : beans) {
            String className = bean.getClassName();
            if (arguments.length == 0) {
                intervalAppend(builder, bean);
            } else {
                for (String arg : arguments) {
                    if (className.equals(arg)) {
                        intervalAppend(builder, bean);
                    }
                }
            }
        }
        return builder.toString();
    }

    private void intervalAppend(StringBuilder builder, RegisterBean bean) {
        builder.append(
                "\t"
                        + bean.getClassName()
                        + " Suspend registration ***"
                        + bean.isPause()
                        + "***\r\n");
    }
}
