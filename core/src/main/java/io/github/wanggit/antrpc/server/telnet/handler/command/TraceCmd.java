package io.github.wanggit.antrpc.server.telnet.handler.command;

import com.alibaba.fastjson.JSONObject;
import io.github.wanggit.antrpc.IAntrpcContext;
import io.github.wanggit.antrpc.client.zk.register.IRegister;
import io.github.wanggit.antrpc.client.zk.register.RegisterBean;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvokeListener;
import io.github.wanggit.antrpc.server.invoker.IRpcRequestBeanInvoker;
import io.github.wanggit.antrpc.server.telnet.CmdInfoBean;
import io.github.wanggit.antrpc.server.telnet.handler.CmdDesc;
import io.github.wanggit.antrpc.server.telnet.handler.command.util.ArrayClassNameUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;

/** 用于服务提供者端，查看本服务提供的此接口最近N次调用 */
@CmdDesc(
        value = "trace",
        desc =
                "trace the interface."
                        + "\r\n\t\t\tExample: log io.github.wanggit.antrpc.demo.telnet.api.HelloService#sayHello(java.lang.String) 5")
public class TraceCmd extends AbsCmd implements INeedChannelCmd {

    private ChannelHandlerContext context;

    public TraceCmd(IAntrpcContext antrpcContext, Map<String, CmdInfoBean> telnetCmds) {
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
        IRegister register = getAntrpcContext().getRegister();
        int idx = target.indexOf("#");
        String className = idx == -1 ? target : target.substring(0, idx);
        String methodName = idx == -1 ? "" : target.substring(idx + 1);
        // test(int  , java.lang.Integer) -> test(int,java.lang.Integer)
        methodName = methodName.replaceAll("\\s*,\\s*", ",");
        // testIntArray(java.lang.String, int[]) -> testIntArray(java.lang.String, [I)
        // testIntArray(java.lang.String[], int[]) -> testIntArray([Ljava.lang.String;, [I)
        methodName = ArrayClassNameUtil.replaceArrayClassName(methodName);
        RegisterBean registerBean = register.findRegisterBeanByClassName(className);
        if (null == registerBean) {
            return "this service is not registered with the " + className + " interface";
        }
        List<RegisterBean.RegisterBeanMethod> methods = registerBean.getMethods();
        RegisterBean.RegisterBeanMethod targetMethod = null;
        for (RegisterBean.RegisterBeanMethod method : methods) {
            if (method.toString().equals(methodName)) {
                targetMethod = method;
                break;
            }
        }
        if (null == targetMethod) {
            return "the [" + methodName + "] not found in the " + className + " interface.";
        }
        IRpcRequestBeanInvoker rpcRequestBeanInvoker =
                getAntrpcContext().getRpcRequestBeanInvoker();
        int max = times;
        rpcRequestBeanInvoker.addListener(
                target,
                new IRpcRequestBeanInvokeListener() {
                    private int count;

                    @Override
                    public void listen(Object result, Object[] argumentValues) {
                        if (context.isRemoved()) {
                            rpcRequestBeanInvoker.removeListener(target, this);
                        }
                        count++;
                        context.writeAndFlush(
                                "argumentValues="
                                        + JSONObject.toJSONString(argumentValues)
                                        + "\r\nresult="
                                        + JSONObject.toJSONString(result)
                                        + "\r\n");
                        if (count >= max) {
                            rpcRequestBeanInvoker.removeListener(target, this);
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
