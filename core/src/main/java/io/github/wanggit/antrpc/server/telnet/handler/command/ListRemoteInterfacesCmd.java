package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;

import java.util.Map;

/** 显示所有服务对外提供的接口汇总 */
@CmdDesc(
        value = "ls",
        desc =
                "list all remote interfaces."
                        + "\r\n\t\t\tExample: ls"
                        + "\r\n\t\t\tExample: ls HelloService \tdisplays the interface contains with \"HelloService\"")
public class ListRemoteInterfacesCmd extends AbsRemoteInterfacesCmd {

    public ListRemoteInterfacesCmd(
            IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    boolean checkCondition(String className, String methodName) {
        return true;
    }
}
