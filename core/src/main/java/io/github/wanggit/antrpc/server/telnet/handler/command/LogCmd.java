package io.github.wanggit.antrpc.server.telnet.handler.command;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.monitor.IRpcCallLogHolder;
import io.github.wanggit.antrpc.client.monitor.IRpcCallLogListener;
import io.github.wanggit.antrpc.commons.bean.RpcCallLog;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

/**
 * 在服务消费者端，查到某一个接口方法的调用情况。 log
 * io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String) 5
 * 表示打印出这个方法接下来5次调用的日志
 */
@CmdDesc(
        value = "log",
        desc =
                "log the interface call logs."
                        + "\r\n\t\t\tExample: log io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String) 5")
public class LogCmd extends AbsCmd implements INeedChannelCmd {

    private ChannelHandlerContext context;

    public LogCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
        super(antrpcContext, telnetCmds);
    }

    @Override
    protected String intervalDoCmd(String[] arguments) {
        if (arguments.length != 2) {
            return "command format error.";
        }
        String target = arguments[0].trim();
        int times = 0;
        try {
            times = Integer.parseInt(arguments[1].trim());
        } catch (NumberFormatException e) {
            return "command format error.";
        }
        IRpcCallLogHolder rpcCallLogHolder = getAntrpcContext().getRpcCallLogHolder();
        int max = times;
        rpcCallLogHolder.addListener(
                target,
                new IRpcCallLogListener() {
                    private int count;

                    @Override
                    public void listen(RpcCallLog rpcCallLog) {
                        if (context.isRemoved()) {
                            rpcCallLogHolder.removeListener(target, this);
                        }
                        count++;
                        context.writeAndFlush(JSONObject.toJSONString(rpcCallLog) + "\r\n");
                        if (count >= max) {
                            rpcCallLogHolder.removeListener(target, this);
                        }
                    }
                });

        return null;
    }

    @Override
    public void setChannelHandlerContext(ChannelHandlerContext context) {
        this.context = context;
    }
}
