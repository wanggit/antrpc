package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;

import java.util.Map;

@CmdDesc(
        value = "unreg",
        desc =
                "unregister interface from zookeeper."
                        + "\r\n\t\t\tExample: unreg"
                        + "\r\n\t\t\tExample: unreg io.github.wanggit.antrpc.demo.telnet.api.HelloService "
                        + "\tunregister io.github.wanggit.antrpc.demo.telnet.api.HelloService from zookeeper."
                        + "\r\n\t\t\tExample: unreg io.github.wanggit.antrpc.demo.telnet.api.HelloService io.github.wanggit.antrpc.demo.telnet.api.TelnetService"
                        + "\r\n\t\t\t\tunregister the two interfaces from zookeeper.")
public class UnregisterCmd extends AbsAboutRegisterCmd {
    public UnregisterCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        IRegister register = getAntrpcContext().getRegister();
        if (arguments.length == 0) {
            register.pauseAllRegister(
                    getAntrpcContext().getConfiguration(), getAntrpcContext().getZkNodeBuilder());
        } else {
            for (String arg : arguments) {
                register.pauseRegister(
                        getAntrpcContext().getConfiguration(),
                        getAntrpcContext().getZkNodeBuilder(),
                        arg);
            }
        }
        return registerStatus(register, arguments);
    }
}
