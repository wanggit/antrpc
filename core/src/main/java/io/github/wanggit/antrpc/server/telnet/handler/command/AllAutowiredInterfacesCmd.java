package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;

import java.util.Map;
import java.util.Set;

/** 显示本服务引用所有远程接口 */
@CmdDesc(
        value = "ref",
        desc =
                "displays all dependent interfaces. "
                        + "\r\n\t\t\tExample: ref"
                        + "\r\n\t\t\tExample: ref HelloService \tdisplays the interface contains with \"HelloService\"")
public class AllAutowiredInterfacesCmd extends AbsRemoteInterfacesCmd {

    private Set<String> autowiredClassNames;

    public AllAutowiredInterfacesCmd(
            IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
        this.autowiredClassNames = getAntrpcContext().getRpcAutowiredProcessor().snapshot();
    }

    @Override
    boolean checkCondition(String className, String methodName) {
        return this.autowiredClassNames.contains(className);
    }
}
