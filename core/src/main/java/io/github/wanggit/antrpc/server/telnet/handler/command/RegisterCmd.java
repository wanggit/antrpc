package io.github.wanggit.antrpc.server.telnet.handler.command;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;

import java.util.Map;

@CmdDesc(
        value = "reg",
        desc =
                "register interface to zookeeper."
                        + "\r\n\t\t\tExample: reg\tregister all interface to zookeeper."
                        + "\r\n\t\t\tExample: reg io.github.wanggit.antrpc.demo.telnet.api.HelloService"
                        + "\tregister io.github.wanggit.antrpc.demo.telnet.api.HelloService to zookeeper"
                        + "\r\n\t\t\tExample: reg io.github.wanggit.antrpc.demo.telnet.api.HelloService io.github.wanggit.antrpc.demo.telnet.api.TelnetService"
                        + "\r\n\t\t\t\tregister the two interfaces to zookeeper.")
public class RegisterCmd extends AbsAboutRegisterCmd {
    public RegisterCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        IRegister register = getAntrpcContext().getRegister();
        if (arguments.length == 0) {
            register.playAllRegister();
        } else {
            for (String argument : arguments) {
                register.playRegister(argument);
            }
        }
        return registerStatus(register, arguments);
    }
}
