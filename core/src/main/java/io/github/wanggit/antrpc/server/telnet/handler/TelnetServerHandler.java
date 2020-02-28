package io.github.wanggit.antrpc.server.telnet.handler;

import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.commons.config.TelnetConfig;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.command.CmdDispatcher;
import io.github.wanggit.antrpc.server.telnet.handler.command.ExitCmd;
import io.github.wanggit.antrpc.server.telnet.handler.command.ICmd;
import io.github.wanggit.antrpc.server.telnet.handler.command.INeedChannelCmd;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
public class TelnetServerHandler extends SimpleChannelInboundHandler<String> {
    private TelnetConfig telnetConfig;
    private ICmdDispatcher cmdDispatcher;
    private boolean hasPassword;
    private boolean logined;

    public TelnetServerHandler(
            TelnetConfig telnetConfig,
            IAntrpcContext antrpcContext,
            Map<String, CmdInfoBean> telnetCmds) {
        this.telnetConfig = telnetConfig;
        this.hasPassword = StringUtils.hasText(telnetConfig.getPassword());
        this.logined = !this.hasPassword;
        this.cmdDispatcher = new CmdDispatcher(antrpcContext, telnetCmds);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("Welcome to the TelnetServer.\r\n");
        if (hasPassword && !logined) {
            ctx.write("Enter the password:\r\n>>");
        }
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (!StringUtils.hasText(msg)) {
            ctx.writeAndFlush("\r\n>>");
            return;
        }
        msg = msg.trim();
        if (!logined && hasPassword && !telnetConfig.getPassword().equals(msg)) {
            ctx.writeAndFlush("Password is wrong! Enter the password:\r\n>>");
            return;
        }
        if (!logined && hasPassword && telnetConfig.getPassword().equals(msg)) {
            logined = true;
            ctx.write(
                    "Welcome!! The TelnetServer time is "
                            + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                            + "\r\n>>");
            ctx.flush();
            return;
        }
        if (logined) {
            ICmd cmd = this.cmdDispatcher.dispatcher(msg);
            if (null == cmd) {
                ctx.writeAndFlush(
                        "Unknown command, please enter 'h' for more help information.\r\n>>");
                return;
            }
            if (cmd instanceof INeedChannelCmd) {
                ((INeedChannelCmd) cmd).setChannelHandlerContext(ctx);
                cmd.doCmd(msg);
            } else {
                String output = cmd.doCmd(msg);
                ctx.writeAndFlush(output + "\r\n>>");
            }
            if (cmd instanceof ExitCmd) {
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (log.isErrorEnabled()) {
            log.error(cause.getMessage());
        }
        ctx.close();
    }
}
